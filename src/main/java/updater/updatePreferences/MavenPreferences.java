package updater.updatePreferences;

import addedvalue.AddedValueEnum;
import org.yaml.snakeyaml.Yaml;
import util.LoggerHelpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class MavenPreferences implements UpdatePreferences {
    private final Map<AddedValueEnum, Double> metricsAndCoefMap;

    public MavenPreferences(Path path) {
        Map<String, Object> confMap = getYmlMap(path);
        this.metricsAndCoefMap = generateMetricAndCoefMap(confMap);
    }

    public Set<AddedValueEnum> getAddedValueEnumSet(){
        return  metricsAndCoefMap.keySet();
    }

    @Override
    public Set<AddedValueEnum> getAggregatedAddedValueEnumSet() {
        Set<AddedValueEnum> aggregatedAddedValues = new HashSet<>();
        for(AddedValueEnum addedValueEnum : metricsAndCoefMap.keySet()){
            if(addedValueEnum.isAggregated()){
                aggregatedAddedValues.add(addedValueEnum);
            }
            else {
                aggregatedAddedValues.add(addedValueEnum.aggregatedVersion());
            }
        }
        return aggregatedAddedValues;
    }

    public double getAddedValueCoef(AddedValueEnum addedValueEnum){
        return metricsAndCoefMap.get(addedValueEnum) == null ? 0.0 : metricsAndCoefMap.get(addedValueEnum);
    }

    private Map<AddedValueEnum, Double> generateMetricAndCoefMap(Map<String, Object> confMap){
        Map<AddedValueEnum, Double> metricsAndCoefMap = new HashMap<>();
        for(Map<String, Object> map : (List<Map<String, Object>>) confMap.get("metrics")){
            metricsAndCoefMap.put(AddedValueEnum.valueOf(map.get("metric").toString().toUpperCase()), Double.parseDouble(map.get("coef").toString()));
        }
        return metricsAndCoefMap;
    }

    private Map<String, Object> getYmlMap(Path path){
        Yaml yaml = new Yaml();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            LoggerHelpers.fatal("Fail to load the conf file:\n"+e.getMessage());
        }
        return yaml.load(inputStream);
    }
}
