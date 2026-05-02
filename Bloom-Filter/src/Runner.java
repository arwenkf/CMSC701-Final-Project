import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
//import edu.princeton.cs.randomhash.*;
import com.google.common.hash.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Runner {
    public static void main(String[] args) throws IOException {

        HashSet<String> set = readFile("Bloom-Filter/test-inputs/dummy-input.txt");
//        HashSet<String> set = readFile("Bloom-Filter/test-inputs/debugging.txt");
        BloomFilter bloomFilter = new BloomFilter(set.size(), 0.05);

//
        System.out.printf("n %d\n", set.size());
//        System.out.printf("m %d\n", bloomFilter.m);
//        System.out.printf("k %d", bloomFilter.k);
//        // done building, now testing query-ing
//
        set.forEach(x -> {
            bloomFilter.add(x);
        });
        queryFile("Bloom-Filter/test-inputs/dummy-query.txt", bloomFilter);



        //baby sanity test

//        CuckooFilter cuckooFilter = new CuckooFilter(set.size(), .05, .95, 4, 500);

//        set.stream().sorted().forEach(x -> {
//            if (!cuckooFilter.insert(x)) {
//                System.out.printf("too full to insert!");
//            }
////            System.out.printf("A:SLDKJ %s \n", cuckooFilter.fingerprint(x));
//        });
//        queryFile("Bloom-Filter/test-inputs/dummy-query.txt", cuckooFilter);

//        cuckooFilter.insert("holospondaic");
//        System.out.printf("%b \n", cuckooFilter.check("holospondaic"));
//        for (String[] bucket : cuckooFilter.buckets) {
//            for (String key: bucket) {
//                if (key != null) {
//                    System.out.printf("%s\n", key);
//                } else {
//                    System.out.printf("empty space");
//                }
//            }
//        }

//        System.out.printf("%d %d\n", cuckooFilter.buckets.length, cuckooFilter.buckets[0].length);
//
//        cuckooFilter.insert("Hello World");
//        System.out.printf("%b\n", cuckooFilter.check("Hello World"));
//        cuckooFilter.delete("Hello World");
//        System.out.printf("%b\n", cuckooFilter.check("Meow"));
//        System.out.printf("%b\n", cuckooFilter.check("Hello World"));
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

    public static void queryFile(String file, BloomFilter bloomFilter) throws IOException {
        try (
                Stream<String> stream = Files.lines(Paths.get(file))
        )
        {
            stream.forEach(key -> {
                if (bloomFilter.check(key)) {
                    System.out.printf("%s\t PROB_YES\n", key);
                } else {
                    System.out.printf("%s\t NO\n", key);
                }
            });
        }
    }

    public static void queryFile(String file, CuckooFilter cuckooFilter) throws IOException {
        try (
                Stream<String> stream = Files.lines(Paths.get(file))
        )
        {
            stream.forEach(key -> {
                if (cuckooFilter.check(key)) {
                    System.out.printf("%s\t PROB_YES\n", key);
                } else {
                    System.out.printf("%s\t NO\n", key);
                }
            });
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

        void add(String item) { // i know the items aren't ints can change soon
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
            return - (int) Math.ceil(elems * Math.log(fp) / (Math.log(2)*Math.log(2))) + 1; // check why the math is being funny
        }

        int getK(int m, int n) {
            return (int)Math.ceil(((double)m/(double)n) * Math.log(2));
        }
    }


    public static class CuckooFilter {
        private int m; // number of buckets
        private HashFunction hashFunc = Hashing.murmur3_32_fixed(200); // some random seed idk
        private String[][] buckets;
        private int maxKicks;
        private int entriesPerBucket;
        private int fingerprintSize;

        public CuckooFilter(int setCount, double falsePosProb, double loadFactor, int entriesPerBucket, int maxKicks) { // what it will be once i get the math
//        public CuckooFilter(int m, int entriesPerBucket, int maxKicks) {

            this.m = (int) Math.ceil(setCount / (entriesPerBucket * loadFactor));
            buckets = new String [m][entriesPerBucket];
            this.maxKicks = maxKicks;
            this.entriesPerBucket = entriesPerBucket;

            this.fingerprintSize = (int) Math.ceil((Math.log(2*entriesPerBucket) / Math.log(2)) / falsePosProb);
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
//            System.out.println(entries.length);
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

            int i2 = (i1 ^ hash(f)) % m ; // xor i1 and hash(f)
            if (i1 == 2531) {
                System.out.printf("%s \n", item);
            }

            int toPut = hasSpace(buckets[i1]);
            if (toPut != -1) {
                buckets[i1][toPut] = f; // insert f
                System.out.println();
                System.out.println(i2);
                System.out.println(toPut);
                return true;
            }
            toPut = hasSpace(buckets[i2]);
            if (toPut != -1) {
                buckets[i2][toPut] = f; // insert in second option
                System.out.println(i2);
                System.out.println(toPut);
                return true;
            }

            // neither bucket has room
            int i = Math.random() > 0.5? i1 : i2;
            for (int n = 0; n < maxKicks; n++) {
                int j = (int) (Math.random() * (entriesPerBucket - 1)); // pick random bucket entry

                String newF = buckets[i][j]; // take current entry
                buckets[i][j] = f; // insert new entry where previous was


                System.out.println(i);
                System.out.println(j);

                i = (i ^ hash(newF)) % m;

                int space = hasSpace(buckets[i]);
                if (space != -1) {
                    buckets[i][space] = newF;

                    System.out.println(i);
                    System.out.println(space);
                    System.out.println(newF);
                    return true;
                }
            }

            return false; // too full to insert anything anymore
        }

        boolean check(String item) {
            String f = fingerprint(item);
            int i1 = hash(item);
            int i2 = (i1 ^ hash(f)) % m;
            System.out.printf("bucket1 %d buket2 %d \n", i1, i2);
            System.out.printf("%s \n", f);

            for (String x : buckets[i1]) {
                System.out.println(x);
                if (x != null && x.equals(f)) {

                    return true;
                }
            }

            for (String x : buckets[i2]) {
                System.out.println(x);
                if (x != null && x.equals(f)) {
                    System.out.println(x);
                    return true;
                }
            }

            return false;
        }

        boolean delete(String item) {
            String f = fingerprint(item);
            int i1 = hash(item);
            int i2 = (i1 ^ hash(f)) % m;

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