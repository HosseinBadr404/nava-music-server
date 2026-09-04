import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
    public static List<String> readLines(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return lines;
    }

    public static void writeLines(String filename, List<String> lines) {
        Path path = Path.of(filename);
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
        } catch (IOException e) {
            System.out.println("Error creating data directory: " + e.getMessage());
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }
}
