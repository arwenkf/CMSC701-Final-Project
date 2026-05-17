import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.packed.PackedInts;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class queryXor {

    static PackedInts.Mutable B = PackedInts.getMutable(0,0,0);
    static HashFunction fingerprintFunc = Hashing.crc32();
    static int seed, size, k;
    static HashFunction h0, h1, h2;
    static String fpr;
    static long queryTime = 0;

    private static void parseBuild(String inputBuild) throws Exception {
        try {
            FileInputStream file = new FileInputStream(inputBuild);
            DataInput in = new InputStreamDataInput(file);

            size = in.readInt();
            k = in.readInt();
            seed = in.readInt();

            h0 = Hashing.murmur3_32_fixed(seed);
            h1 = Hashing.murmur3_32_fixed(seed + 1);
            h2 = Hashing.murmur3_32_fixed(seed + 2);

            B = PackedInts.getMutable(size, k, 0);

            PackedInts.ReaderIterator reader = PackedInts.getReaderIteratorNoHeader(
                    in, PackedInts.Format.PACKED, PackedInts.VERSION_CURRENT, size, k, 0);

            for (int i = 0; i < size; i++) {
                B.set(i, reader.next());
            }

            file.close();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private static boolean computeQuery(String query) {
        int h0_idx = Math.abs(h0.hashString(query, StandardCharsets.UTF_8).asInt()) % size;
        int h1_idx = Math.abs(h1.hashString(query, StandardCharsets.UTF_8).asInt()) % size;
        int h2_idx = Math.abs(h2.hashString(query, StandardCharsets.UTF_8).asInt()) % size;

        long xorResult = (B.get(h0_idx) ^ B.get(h1_idx) ^ B.get(h2_idx)) & 0xFFFFFFFFL;
        int mask = (1 << k) - 1;
        return xorResult == (fingerprintFunc.hashString(query, StandardCharsets.UTF_8).asInt() & mask);
    }

    private static void readAndWriteQueries(String queries, String out) {

        try {

            Scanner scanner = new Scanner(new File(queries));
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));
            long startQuery;
            long endQuery;


            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                startQuery = System.nanoTime();
                String res = computeQuery(line)? "PROB_YES" : "NO";
                endQuery = System.nanoTime();
                queryTime += ((endQuery - startQuery));

                writer.write(line + "\t" + res + "\n");
            }
            writer.close();
            scanner.close();



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main (String[] args) {
        try {
            String inputBuild = args[0], query = args[1], out = args[2];
            fpr = args[3];

            parseBuild(inputBuild);
            readAndWriteQueries(query, out);

            try (BufferedWriter queryWriter = new BufferedWriter(new FileWriter("xor-eval-query.txt", true))) {
                queryWriter.write(query);
                queryWriter.newLine();
                queryWriter.write("fpr " + fpr.trim());
                queryWriter.newLine();
                queryWriter.write(String.valueOf(queryTime / 1_000_000_000.0));
                queryWriter.newLine();
            } catch (IOException e) {
                System.err.println("Error writing to xor-eval-query.txt: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
