import  java.util.BitSet;
//import edu.princeton.cs.randomhash.*;
import com.google.common.hash.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

void main() {
 //todo: test your stuff bro
}

public class BloomFilter {
    private int m; // size of the bit array
    private BitSet bit_array;
    private int k; // number of hash functions
    private List<HashFunction> hashFunctions = new ArrayList<>();


    public BloomFilter(int setCount, int falsePosProb) {
        this.m = getSize(setCount, falsePosProb);
        this.bit_array = new BitSet(m);
        this.k = getK(m, setCount);
        for (int i = 0; i <k; i++)  {
            hashFunctions.add(Hashing.murmur3_32_fixed(i)); // use i as the seed
        }

    }

    void add(int item) {
        for (int i = 0; i < k; i++) {
            int digest = hashFunctions.get(i).hashInt(i).asInt() % m; // mod m to make it fit in the filter
            bit_array.set(digest, true); // set that index to a 1
        }
    }

    boolean check(int item) {
        for (int i = 0; i < k; i++) {
            int digest = hashFunctions.get(i).hashInt(i).asInt() % m; // mod m to make it fit in the filter
            if (bit_array.get(digest) == false) {
                return false;
            }
        }
        return true;
    }

    int getSize(int elems, int fp) {
        double doubleFp = fp;
        int m = (int) Math.ceil(elems * Math.log(doubleFp) / (Math.log(2)*Math.log(2)));
        return m;
    }

    int getK(int m, int n) {
        int k = (int)Math.ceil(((double)m/(double)n) * Math.log(2));
        return k;
    }
}