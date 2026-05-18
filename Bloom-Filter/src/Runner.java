import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
//import edu.princeton.cs.randomhash.*;
import com.google.common.hash.*;

public class Runner {
    public static void main(String[] args) throws IOException {

        String inputPath = args[0], fpr = args[1], queryPath = args[2], outputPath = args[3], filterMode = args[4];

        HashSet<String> set = readFile(inputPath);

        long startBuild = 0, endBuild = 0, startQuery = 0, endQuery = 0;

        if(filterMode.equals("Bloom")){
            startBuild = System.nanoTime();
            BloomFilter filter = new BloomFilter(set.size(), Double.parseDouble(fpr));
            
            set.forEach(x -> {
                        filter.add(x);
                    });
            endBuild = System.nanoTime();

            startQuery = System.nanoTime();
            queryFile(queryPath, filter, outputPath);
            endQuery = System.nanoTime();
            
        } else if(filterMode.equals("Cuckoo")){
            startBuild = System.nanoTime();
            CuckooFilter filter = new CuckooFilter(set.size(), Double.parseDouble(fpr), 0.95, 4, 1000);
            set.forEach(x -> {
                        filter.insert(x);
                    });
            endBuild = System.nanoTime();

            startQuery = System.nanoTime();
            queryFile(queryPath, filter, outputPath);
            endQuery = System.nanoTime();
        }

        double buildTimeSec = (endBuild - startBuild) / 1_000_000_000.0;
        double queryTimeSec = (endQuery - startQuery) / 1_000_000_000.0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("bloom-eval.txt", true))) {
            writer.write( "fpr:" + fpr);
            writer.newLine();
            writer.write( buildTimeSec + "\t" + queryTimeSec);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to bloom-eval.txt: " + e.getMessage());
        }
    }

    public static HashSet<String> readFile(String file) throws IOException {
        HashSet<String> set = new HashSet<>();
        try (
                Stream<String> stream = Files.lines(Paths.get(file))
                )
        {
            stream.forEach(set::add);
        }
        return set;
    }

    public static void queryFile(String file, BloomFilter bloomFilter, String out) throws IOException {
        try (
                Stream<String> stream = Files.lines(Paths.get(file))
        )
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            stream.forEach(key -> {
                try {
                    if (bloomFilter.check(key)) {
                        writer.write(key + "\t" + "PROB_YES" + "\n");
                    } else {
                        writer.write(key + "\t" + "NO" + "\n");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.close();
        }

    }

    public static void queryFile(String file, CuckooFilter cuckooFilter, String out) throws IOException {
        try (
                Stream<String> stream = Files.lines(Paths.get(file))
        )
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            stream.forEach(key -> {
                try {
                    if (cuckooFilter.check(key)) {
                        writer.write(key + "\t" + "PROB_YES" + "\n");
                    } else {
                        writer.write(key + "\t" + "NO" + "\n");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            writer.close();
        }
    }

    public static class BloomFilter {
        private int m; // size of the bit array
        private BitSet bit_array;
        private int k; // number of hash functions
        private List<HashFunction> hashFunctions = new ArrayList<>();


        public BloomFilter(int setCount, double falsePosProb) {
            this.m = getSize(setCount, falsePosProb);
            this.bit_array = new BitSet(m);
            this.k = getK(m, setCount);
            for (int i = 0; i <k; i++)  {
                hashFunctions.add(Hashing.murmur3_32_fixed(i)); // use i as the seed
            }

        }

        void add(String item) {
            for (int i = 0; i < k; i++) {
                int digest = Math.abs(hashFunctions.get(i).hashString(item, StandardCharsets.UTF_8).asInt()) % m; // mod m to make it fit in the filter
                bit_array.set(digest, true); // set that index to a 1
            }
        }

        boolean check(String item) {
            for (int i = 0; i < k; i++) {
                int digest = Math.abs(hashFunctions.get(i).hashString(item, StandardCharsets.UTF_8).asInt()) % m; // mod m to make it fit in the filter
                if (bit_array.get(digest) == false) {
                    return false;
                }
            }
            return true;
        }

        int getSize(int elems, double fp) {
            return - (int) Math.ceil(elems * Math.log(fp) / (Math.log(2)*Math.log(2))) + 1;
        }

        int getK(int m, int n) {
            return (int)Math.ceil(((double)m/(double)n) * Math.log(2));
        }
    }



    public static class CuckooFilter {
        private int m; // number of buckets
        private HashFunction hashFunc = Hashing.murmur3_32_fixed(200); // random seed
        private String[][] buckets;
        private int maxKicks;
        private int entriesPerBucket;
        private int fingerprintSize;

        public CuckooFilter(int setCount, double falsePosProb, double loadFactor, int entriesPerBucket, int maxKicks) {
            this.m = nextPowerOfTwo((int) Math.ceil(setCount / (entriesPerBucket * loadFactor)));
            buckets = new String [m][entriesPerBucket];
            this.maxKicks = maxKicks;
            this.entriesPerBucket = entriesPerBucket;

            double log2InvEpsilon = Math.log(1.0 / falsePosProb) / Math.log(2);
            double log2TwoB = Math.log(2 * entriesPerBucket) / Math.log(2);
            this.fingerprintSize = (int) Math.ceil(log2InvEpsilon + log2TwoB);
        }

        private int nextPowerOfTwo(int n) {
            return (n < 1) ? 1 : Integer.highestOneBit(n - 1) << 1;
        }

        int hash(String item) {
            return Math.abs(hashFunc.hashString(item, StandardCharsets.UTF_8).asInt()) % m;
        }

        int hasSpace(String[] entries) {
            for (int i=0; i < entries.length; i++) {
                if (entries[i] == null) {
                    return i;
                }
            }
            return -1;
        }

        String fingerprint(String item) {
            int hash = item.hashCode();
            String bitString = Integer.toBinaryString(hash);

            if (bitString.length() > fingerprintSize) {
                bitString = bitString.substring(bitString.length() - fingerprintSize); // keep last bits
            } else if (bitString.length() < fingerprintSize) {
                bitString = String.format("%" + fingerprintSize + "s", bitString).replace(' ', '0');
            }
            return bitString;
        }

        boolean insert(String item) { // returns success or not (it can be "too full" to add anymore -- currently doesn't resize)
            String f = fingerprint(item);
            int i1 = hash(item);

            int i2 = (i1 ^ hash(f)) & (m-1); // xor i1 and hash(f)

            int toPut = hasSpace(buckets[i1]);
            if (toPut != -1) {
                buckets[i1][toPut] = f; // insert f
                return true;
            }
            toPut = hasSpace(buckets[i2]);
            if (toPut != -1) {
                buckets[i2][toPut] = f; // insert in second option
                return true;
            }

            // neither bucket has room
            int i = Math.random() > 0.5? i1 : i2;
            for (int n = 0; n < maxKicks; n++) {
                int j = (int) (Math.random() * (entriesPerBucket)); // pick random bucket entry

                String newF = buckets[i][j]; // take current entry
                buckets[i][j] = f; // insert new entry where previous was
                f = newF; // reset f for next round

                i = (i ^ hash(f))& (m-1);

                int space = hasSpace(buckets[i]);
                if (space != -1) {
                    buckets[i][space] = f;

                    return true;
                }
            }

            return false; // too full to insert anything anymore
        }

        boolean check(String item) {
            String f = fingerprint(item);
            int i1 = hash(item);
            int i2 = (i1 ^ hash(f)) & (m-1);

            for (String x : buckets[i1]) {
                if (x != null && x.equals(f)) {

                    return true;
                }
            }

            for (String x : buckets[i2]) {
                if (x != null && x.equals(f)) {
                    return true;
                }
            }

            return false;
        }

        boolean delete(String item) {
            String f = fingerprint(item);
            int i1 = hash(item);
            int i2 = (i1 ^ hash(f)) & (m-1);

            for (int i = 0; i < entriesPerBucket; i++) {
                if (buckets[i1][i] != null && buckets[i1][i].equals(f)) {
                    buckets[i1][i] = null; // remove entry
                    return true;
                }
                if (buckets[i2][i] != null && buckets[i2][i].equals(f)) {
                    buckets[i2][i] = null; // remove entry
                    return true;
                }
            }

            return false;
        }

    }
}
