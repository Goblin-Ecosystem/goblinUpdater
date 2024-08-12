package util.helpers.system;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileHelpers {

    private FileHelpers() {}

    public static void createDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
        } catch (IOException e) {
            LoggerHelpers.instance().error("Failed to create directory:\n" + e.getMessage());
        }
    }

    public static void deleteDirectoryIfExist(String directoryPath) {
        Path path = Paths.get(directoryPath);
        if (Files.exists(path)) {
            try {
                Files.walk(Paths.get(directoryPath))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                LoggerHelpers.instance().error("Unable to delete folder: " + directoryPath);
            }
        }
    }

    public static void createCsvFile(String path, String[] headers) {
        try {
            FileWriter writer = new FileWriter(path, false);
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));
            printer.close();
        } catch (IOException e) {
            LoggerHelpers.instance().error("Unable to create file: " + path);
        }
    }
}
