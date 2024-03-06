package util;

import addedvalue.AddedValueEnum;
import org.apache.maven.model.Dependency;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class GoblinWeaverHelpers {
    private static final String API_URL = "http://localhost:8080";

    private static JSONObject executeQuery(JSONObject bodyJsonObject, String apiRoute){
        try {
            URL url = new URL(API_URL+apiRoute);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json; utf-8");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);

            byte[] out = bodyJsonObject.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            if(http.getResponseCode() == 200){
                JSONParser jsonParser = new JSONParser();
                return (JSONObject)jsonParser.parse(
                        new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8));
            }
            http.disconnect();
        } catch (IOException | org.json.simple.parser.ParseException e) {
            System.out.println("Unable to connect to API:\n" + e);
        }
        return null;
    }

    public static JSONObject getAllPossibilitiesRootedGraph(List<Dependency> directDependencies, List<AddedValueEnum> addedValues){
        String apiRoute = "/graph/allPossibilitiesRooted";

        JSONObject bodyJsonObject = new JSONObject();
        JSONArray releasesArray = new JSONArray();
        for(Dependency directDependency : directDependencies) {
            JSONObject releaseJsonObject = new JSONObject();
            releaseJsonObject.put("groupId", directDependency.getGroupId());
            releaseJsonObject.put("artifactId", directDependency.getArtifactId());
            releaseJsonObject.put("version", directDependency.getVersion());
            releasesArray.add(releaseJsonObject);
        }
        bodyJsonObject.put("releases", releasesArray);
        JSONArray addedValuesArray = new JSONArray();
        addedValuesArray.addAll(addedValues.stream().map(AddedValueEnum::toString).collect(Collectors.toList()));
        bodyJsonObject.put("addedValues", addedValuesArray);
        return executeQuery(bodyJsonObject, apiRoute);
    }

    public static JSONObject getDirectPossibilitiesRootedGraph(List<Dependency> directDependencies, List<AddedValueEnum> addedValues){
        String apiRoute = "/graph/directPossibilitiesRooted";

        JSONObject bodyJsonObject = new JSONObject();
        JSONArray releasesArray = new JSONArray();
        for(Dependency directDependency : directDependencies) {
            JSONObject releaseJsonObject = new JSONObject();
            releaseJsonObject.put("groupId", directDependency.getGroupId());
            releaseJsonObject.put("artifactId", directDependency.getArtifactId());
            releaseJsonObject.put("version", directDependency.getVersion());
            releasesArray.add(releaseJsonObject);
        }
        bodyJsonObject.put("releases", releasesArray);
        JSONArray addedValuesArray = new JSONArray();
        addedValuesArray.addAll(addedValues.stream().map(AddedValueEnum::toString).collect(Collectors.toList()));
        bodyJsonObject.put("addedValues", addedValuesArray);
        return executeQuery(bodyJsonObject, apiRoute);
    }
}
