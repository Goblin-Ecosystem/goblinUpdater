package util;

import addedvalue.AddedValueEnum;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class YmlConfReader {
    private final Map<String, Object> confMap;
    private static YmlConfReader INSTANCE;

    private YmlConfReader() {
        Yaml yaml = new Yaml();
        System.out.println(System.getProperty("confFile"));
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(System.getProperty("confFile"));
        } catch (FileNotFoundException e) {
            LoggerHelpers.fatal("Fail to load the conf file:\n"+e.getMessage());
        }
        confMap = yaml.load(inputStream);
    }

    public static synchronized YmlConfReader getInstance()
    {
        if (INSTANCE == null)
        {   INSTANCE = new YmlConfReader();
        }
        return INSTANCE;
    }

    public Set<AddedValueEnum> getAddedValueEnumSet(){
        Set<AddedValueEnum> addedValueEnumSet = new HashSet<>();
        for(Map<String, Object> map : (List<Map<String, Object>>) confMap.get("metrics")){
            addedValueEnumSet.add(AddedValueEnum.valueOf(map.get("metric").toString().toUpperCase()));
        }
        return addedValueEnumSet;
    }

    public double getAddedValueCoef(AddedValueEnum addedValueEnum){
        for(Map<String, Object> map : (List<Map<String, Object>>) confMap.get("metrics")){
            if(map.get("metric").toString().toUpperCase().equals(addedValueEnum.toString())){
                return Double.parseDouble(map.get("coef").toString());
            }
        }
        return 0.0;
    }
}
