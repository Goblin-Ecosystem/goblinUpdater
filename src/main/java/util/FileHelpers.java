package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileHelpers {

    public static void createDirectory(String directoryPath){
        try {
            Path path = Paths.get(directoryPath);
            Files.createDirectories(path);
        } catch (IOException e) {
            LoggerHelpers.error("Failed to create directory:\n" + e.getMessage());
        }
    }

    public static void deleteDirectoryIfExist(String directoryPath){
        Path path = Paths.get(directoryPath);
        if (Files.exists(path)) {
            try {
                Files.walk(Paths.get(directoryPath))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                LoggerHelpers.error("Unable to delete folder: " + directoryPath);
            }
        }
    }
}
