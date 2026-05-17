import java.util.*;
import java.io.*;


public class baselineHash {

    static HashSet<String> keys = new HashSet<>();
    static int seed, k, c;
    static long queryTime = 0;
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

    private static void readAndWriteQueries(String queries, String out) {
        try {
            Scanner scanner = new Scanner(new File(queries));
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            long startQuery, endQuery = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                startQuery = System.nanoTime();
                String res = keys.contains(line)? "PROB_YES" : "NO";
                endQuery = System.nanoTime();
                queryTime += ((endQuery - startQuery));

                writer.write(line + " " + res + "\n");
            }
            writer.close();
            scanner.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main (String[] args) {
        try {
            String inputPath = args[0], queryPath = args[1], out = args[2];

            long startBuild = System.nanoTime();
            readKeys(inputPath);
            long endBuild = System.nanoTime();

            readAndWriteQueries(queryPath, out);


            try (BufferedWriter writer = new BufferedWriter(new FileWriter("baseline-res.txt", true))) {
                writer.write(inputPath);
                writer.newLine();
                writer.write("build: " + String.valueOf((endBuild - startBuild) / 1_000_000_000.0));
                writer.newLine();
                writer.write("query: " + String.valueOf(queryTime / 1_000_000_000.0));
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error writing to baseline-res.txt: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
