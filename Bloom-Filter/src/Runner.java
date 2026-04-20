import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import  java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
//import edu.princeton.cs.randomhash.*;
import com.google.common.hash.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Runner {
    public static void main(String[] args) throws IOException {

        HashSet<String> set = readFile("Bloom-Filter/test-inputs/dummy-input.txt");
        BloomFilter bloomFilter = new BloomFilter(set.size(), 0.05);
        set.forEach(bloomFilter::add);

        System.out.printf("n %d\n", set.size());
        System.out.printf("m %d\n", bloomFilter.m);
        System.out.printf("k %d", bloomFilter.k);
        // done building, now testing query-ing

        queryFile("Bloom-Filter/test-inputs/dummy-query.txt", bloomFilter);
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
}