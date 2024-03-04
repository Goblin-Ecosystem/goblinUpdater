package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SystemHelpers {
    public static List<String> execCommand(String command) {
        List<String> output = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd","/c",command});
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = stdInput.lines().collect(Collectors.toList());
            // If no error
            if (output.size() != 0) {
                stdInput.close();
            } else {
                // Log command error
                errorHandling(process, command);
            }
        } catch (IOException e) {
            System.out.println("Unable to run command: " + command + "\n" + e.getMessage());
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
                System.out.println("Error on command: "+command+"\n Error output: \n"+errorSb);
            }
            stdError.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
