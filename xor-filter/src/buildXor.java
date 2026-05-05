import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.packed.PackedInts;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class buildXor {

    static HashSet<String> keys = new HashSet<>();
    static PackedInts.Mutable B = PackedInts.getMutable(0,0,0);
    static HashFunction fingerprintFunc = Hashing.crc32();
    static int seed, k, c;

    public static class Pair <T, Y> {

        T car;
        Y cdr;

        public Pair(T car, Y cdr){
            this.car = car;
            this.cdr = cdr;
        }

        public T car(){
            return this.car;
        }

        public Y cdr(){
            return this.cdr;
        }
    }

    private static void readKeys(String path) {
        keys = new HashSet<>();
        try {
            Scanner scanner = new Scanner(new File(path));
            while(scanner.hasNextLine()){
                String key = scanner.nextLine();
                if(key.isEmpty()){
                    continue;
                }
                keys.add(key);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<Stack<Pair<String, Integer>>> map(HashFunction h0, HashFunction h1, HashFunction h2){
        ArrayList<ArrayList<String>> H = new ArrayList<>();
        for (int i = 0; i < c; i++) {
            H.add(new ArrayList<String>());
        }

        for(String key : keys){
            int h0_idx = Math.abs(h0.hashString(key, StandardCharsets.UTF_8).asInt()) % c;
            int h1_idx = Math.abs(h1.hashString(key, StandardCharsets.UTF_8).asInt()) % c;
            int h2_idx = Math.abs(h2.hashString(key, StandardCharsets.UTF_8).asInt()) % c;

            H.get(h0_idx).add(key);
            H.get(h1_idx).add(key);
            H.get(h2_idx).add(key);
        }

        Queue<Integer> Q = new LinkedList<>();
        for(int i = 0; i < H.size(); i++){
            if(H.get(i).size() == 1){
                Q.add(i);
            }
        }

        Stack<Pair<String, Integer>> sigma = new Stack<>();

        while(!Q.isEmpty()){
            Integer idx = Q.poll();

            if(H.get(idx).size() == 1){
                String x = H.get(idx).getFirst();
                sigma.push(new Pair<>(x, idx));

                int h0_idx = Math.abs(h0.hashString(x, StandardCharsets.UTF_8).asInt()) % c;
                int h1_idx = Math.abs(h1.hashString(x, StandardCharsets.UTF_8).asInt()) % c;
                int h2_idx = Math.abs(h2.hashString(x, StandardCharsets.UTF_8).asInt()) % c;

                ArrayList<String> h0_set = H.get(h0_idx);
                ArrayList<String> h1_set = H.get(h1_idx);
                ArrayList<String> h2_set = H.get(h2_idx);

                h0_set.remove(x);
                h1_set.remove(x);
                h2_set.remove(x);

                if(h0_set.size() == 1){
                    Q.add(h0_idx);
                }
                if(h1_set.size() == 1){
                    Q.add(h1_idx);
                }
                if(h2_set.size() == 1){
                    Q.add(h2_idx);
                }
            }
        }

        return sigma.size() == keys.size() ? Optional.of(sigma) : Optional.empty();
    }

    private static void assign(Stack<Pair<String, Integer>> sigma, HashFunction h0, HashFunction h1, HashFunction h2) {
        while(!sigma.isEmpty()){
            Pair<String, Integer> pair = sigma.pop();
            String key = pair.car();
            int idx = pair.cdr();

            B.set(idx, 0);

            int mask = (1 << k) - 1;
            int fingerprint = fingerprintFunc.hashString(key, StandardCharsets.UTF_8).asInt() & mask;
            int h0_idx = Math.abs(h0.hashString(key, StandardCharsets.UTF_8).asInt()) % c ;
            int h1_idx = Math.abs(h1.hashString(key, StandardCharsets.UTF_8).asInt()) % c;
            int h2_idx = Math.abs(h2.hashString(key, StandardCharsets.UTF_8).asInt()) % c;

            long overallRes = (fingerprint ^ (B.get(h0_idx) ^ B.get(h1_idx) ^ B.get(h2_idx))) & 0xFFFFFFFFL;
            B.set(idx, overallRes);
        }
    }

    private static void constructXor(double fpr) {
        k = (int) Math.ceil(Math.log(fpr) / Math.log(0.5));
        HashFunction h0 = null, h1 = null, h2 = null;
        Stack<Pair<String, Integer>> sigma = new Stack<>();
        Random rand = new Random();

        while (true) {
            seed = rand.nextInt();
            h0 = Hashing.murmur3_32_fixed(seed);
            h1 = Hashing.murmur3_32_fixed(seed + 1);
            h2 = Hashing.murmur3_32_fixed(seed + 2);

            Optional<Stack<Pair<String, Integer>>> mapRes = map(h0, h1, h2);
            // found valid combination, break
            if(mapRes.isPresent()){
                sigma = (Stack<Pair<String, Integer>>) mapRes.get();
                break;
            }
        }

        B = PackedInts.getMutable(c , k, 0);

        assign(sigma, h0, h1, h2);
    }

    private static void writeOut(String path) {
        try {

            FileOutputStream file = new FileOutputStream(path);
            DataOutput out = new OutputStreamDataOutput(file);

            out.writeInt(c);
            out.writeInt(k);
            out.writeInt(seed);

            PackedInts.Writer writer = PackedInts.getWriterNoHeader(out, PackedInts.Format.PACKED, c, k, 0);

            for (int i = 0; i < c; i++) {
                writer.add(B.get(i));
            }
            writer.finish();
            file.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main (String[] args) {
        try {
            String path = args[0], fpr = args[1], out = args[2];
            readKeys(path);

            c = (int) (keys.size() * 1.23) + 32;

            long startBuild = System.nanoTime();
            constructXor(Double.parseDouble(fpr));
            long endBuild = System.nanoTime();

            writeOut(out);

            try (BufferedWriter buildWriter = new BufferedWriter(new FileWriter("xor-eval-build.txt", true))) {
                String res = "built time with fpr: " + fpr.trim();
                buildWriter.write(res);
                buildWriter.newLine();
                buildWriter.write(String.valueOf((endBuild - startBuild) / 1_000_000_000.0));
                buildWriter.newLine();
            } catch (IOException e) {
                System.err.println("Error writing to xor-eval.txt: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
