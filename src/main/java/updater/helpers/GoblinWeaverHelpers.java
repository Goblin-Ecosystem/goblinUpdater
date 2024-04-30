package updater.helpers;

import updater.api.metrics.MetricType;
import updater.api.preferences.Constraint;
import updater.api.preferences.Preferences;
import updater.api.preferences.Preferences.Focus;
import updater.api.preferences.Preferences.Selector;
import updater.api.project.Dependency;
import util.helpers.system.LoggerHelpers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// FIXME: take preferences into account
// releases:
//  - focuses: either NONE, ROOT, CONSTRAINTS, or ALL
//  - selection: combination of MORE_RECENT, NO_PATCHES (can be empty meaning NO FILTER)
public class GoblinWeaverHelpers {

    private GoblinWeaverHelpers() {
    }

    public static JSONObject getSuperGraph(Set<Dependency> directDependencies, Set<MetricType> metrics, Preferences preferences) {
        Focus releaseFocus = preferences.releaseFocus();
        Set<String> libToExpendsGa = new HashSet<>();
        switch (releaseFocus) {
            case NONE:
                // Rooted graph case, no expends
                break;
            case ALL:
                // All possibilities case, expends all
                libToExpendsGa.add("all");
                break;
            case CONSTRAINTS:
                // constraints case, expends desired libs
                List<Constraint<String>> constraints = preferences.constraints();
                for (Constraint<String> constraint : constraints){
                    if (constraint.code().equals("ABSENCE")){
                        String gavId = constraint.value();
                        libToExpendsGa.add(gavId.substring(0, gavId.lastIndexOf(':')));
                    }
                }
                break;
            default:
                // Direct possibilities case, expends direct dependencies libs
                libToExpendsGa.addAll(directDependencies.stream().map(Dependency::getGa).collect(Collectors.toSet()));
                break;
        }
        return graphTraversing(directDependencies.stream().map(Dependency::getGav).collect(Collectors.toSet()), libToExpendsGa, preferences.releaseSelectors(), metrics);
    }

    private static final String API_URL = System.getProperty("weaverUrl");

    private static JSONObject executeQuery(JSONObject bodyJsonObject, String apiRoute) {
        try {
            URL url = new URL(API_URL + apiRoute);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json; utf-8");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);

            byte[] out = bodyJsonObject.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            if (http.getResponseCode() == 200) {
                JSONParser jsonParser = new JSONParser();
                return (JSONObject) jsonParser.parse(
                        new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8));
            } else {
                LoggerHelpers.instance().error("API error code: " + http.getResponseCode() + "\n");
            }
            http.disconnect();
        } catch (IOException | org.json.simple.parser.ParseException e) {
            LoggerHelpers.instance().fatal("Unable to connect to API:\n" + e.getMessage());
        }
        return null;
    }

    private static JSONObject graphTraversing(Set<String> startReleasesGav, Set<String> libToExpendsGa,
                                              Set<Selector> filters, Set<MetricType> metrics) {
        LoggerHelpers.instance().info("Get graph from Goblin Weaver");
        String apiRoute = "/graph/traversing";

        JSONObject bodyJsonObject = new JSONObject();

        JSONArray startReleasesArray = new JSONArray();
        startReleasesArray.addAll(startReleasesGav);
        bodyJsonObject.put("startReleasesGav", startReleasesArray);

        JSONArray libToExpendsGaArray = new JSONArray();
        libToExpendsGaArray.addAll(libToExpendsGa);
        bodyJsonObject.put("libToExpendsGa", libToExpendsGaArray);

        JSONArray filtersArray = new JSONArray();
        filtersArray.addAll(filters.stream().map(Selector::toString).collect(Collectors.toList()));
        bodyJsonObject.put("filters", filtersArray);

        JSONArray metricsArray = new JSONArray();
        metricsArray.addAll(metrics.stream().map(MetricType::toString).collect(Collectors.toList()));
        bodyJsonObject.put("addedValues", metricsArray);

        return executeQuery(bodyJsonObject, apiRoute);
    }

    // LPLA TODO: delete ?
    public static JSONObject getDirectPossibilitiesRootedGraph(Set<Dependency> directDependencies,
            Set<MetricType> metrics) {
        LoggerHelpers.instance().info("Get direct all possibilities graph");
        String apiRoute = "/graph/directPossibilitiesRooted";

        JSONObject bodyJsonObject = new JSONObject();
        JSONArray releasesArray = new JSONArray();
        directDependencies.forEach(d -> releasesArray.add(getReleaseJsonObject(d)));
        bodyJsonObject.put("releases", releasesArray);
        JSONArray metricsArray = new JSONArray();
        metricsArray.addAll(metrics.stream().map(MetricType::toString).collect(Collectors.toList()));
        bodyJsonObject.put("addedValues", metricsArray);
        return executeQuery(bodyJsonObject, apiRoute);
    }

    private static JSONObject getReleaseJsonObject(Dependency directDependency) {
        JSONObject releaseJsonObject = new JSONObject();
        releaseJsonObject.put("groupId", directDependency.groupId());
        releaseJsonObject.put("artifactId", directDependency.artifactId());
        releaseJsonObject.put("version", directDependency.version());
        return releaseJsonObject;
    }
}
