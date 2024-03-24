package util.helpers.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SystemHelpers {

    private SystemHelpers() {
    }

    public static List<String> execCommand(ProcessBuilder processBuilder) {
        List<String> output = new ArrayList<>();
        try {
            Process process = processBuilder.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = stdInput.lines().collect(Collectors.toList());
            // If no error
            if (!output.isEmpty()) {
                stdInput.close();
            } else {
                // Log command error
                errorHandling(process, String.join(" ", processBuilder.command()));
            }
        } catch (IOException e) {
            LoggerHelpers.instance().error(
                    "Unable to run command: " + String.join(" ", processBuilder.command()) + "\n" + e.getMessage());
        }
        return output;
    }

    private static void errorHandling(Process process, String command) {
        try {
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorSb = new StringBuilder();
            String line = stdError.readLine();
            if (line != null) {
                // Get error msg
                errorSb.append(line).append("\n");
                while ((line = stdError.readLine()) != null) {
                    errorSb.append(line).append("\n");
                }
                LoggerHelpers.instance().error("Error on command: " + command + "\n Error output: \n" + errorSb);
            }
            stdError.close();
        } catch (IOException e) {
            LoggerHelpers.instance().error(e.getMessage());
        }
    }
}
