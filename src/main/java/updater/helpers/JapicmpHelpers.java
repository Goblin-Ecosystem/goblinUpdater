package updater.helpers;

import com.github.maracas.MaracasOptions;
import com.github.maracas.delta.JApiCmpDeltaFilter;
import japicmp.cli.JApiCli;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.model.JApiChangeStatus;
import japicmp.model.JApiClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import updater.api.graph.structure.UpdateNode;
import updater.api.preferences.Preferences;
import util.helpers.maven.MavenLocalRepository;
import util.helpers.system.FileHelpers;
import util.helpers.system.LoggerHelpers;

import java.io.*;
import java.util.List;

public class JapicmpHelpers {
    private static final String[] HEADERS = {"release1", "release2", "value"};
    private static final String DATA_REPO = "goblinUpdater_data";
    private static final String CSV_FILE_PATH = DATA_REPO+"/japicmpValues.csv";

    private JapicmpHelpers(){}

    public static double computeChangeCost(UpdateNode oldRelease, UpdateNode newRelease, Preferences updatePreferences) {
        if (oldRelease.equals(newRelease)) {
            return 0.0;
        }
        try {
            Double csvValue = findValueOnCsv(oldRelease, newRelease);
            if(csvValue != null){
                return csvValue;
            }
            String[] oldReleaseSplited = oldRelease.id().split(":");
            String[] newReleaseSplited = oldRelease.id().split(":");
            if (oldReleaseSplited.length < 3 || newReleaseSplited.length < 3 ){
                return 0.0;
            }
            // Use japicmp Maracas's options and filters
            MaracasOptions opts = MaracasOptions.newDefault();
            opts.getJApiOptions().setClassPathMode(JApiCli.ClassPathMode.ONE_COMMON_CLASSPATH);
            JarArchiveComparatorOptions jApiOptions = JarArchiveComparatorOptions.of(opts.getJApiOptions());

            JarArchiveComparator jarArchiveComparator = new JarArchiveComparator(jApiOptions);
            MavenLocalRepository mavenLocalRepository = MavenLocalRepository.getInstance();

            JApiCmpArchive oldArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(oldRelease.id())), oldReleaseSplited[2]);
            JApiCmpArchive newArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(newRelease.id())), newReleaseSplited[2]);

            List<JApiClass> jApiClasses = jarArchiveComparator.compare(oldArchive, newArchive);

            JApiCmpDeltaFilter filter = new JApiCmpDeltaFilter(opts);
            filter.filter(jApiClasses);

            Double value = (double) jApiClasses.stream()
                    .filter(jApiClass -> {
                        JApiChangeStatus status = jApiClass.getChangeStatus();
                        return status == JApiChangeStatus.MODIFIED || status == JApiChangeStatus.REMOVED;
                    })
                    .count();
            appendValueToCsv(oldRelease.id(), newRelease.id(), String.valueOf(value));
            return value;
        } catch (Exception e) {
            LoggerHelpers.instance().error("Japicmp fail:\n" + e.getMessage());
            return updatePreferences.defaultCost().toDouble();
        }
    }

    private static Double findValueOnCsv(UpdateNode oldRelease, UpdateNode newRelease) {
        Double value = null;
        String version1 = oldRelease.id();
        String version2 = newRelease.id();
        if (version1.compareTo(version2) > 0) {
            String temp = version1;
            version1 = version2;
            version2 = temp;
        }

        File csvData = new File(CSV_FILE_PATH);
        if(!csvData.exists()){
            FileHelpers.createDirectory("goblinUpdater_data");
            FileHelpers.createCsvFile(CSV_FILE_PATH, HEADERS);
        }
        try {
            Reader in = new FileReader(csvData);

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(in);

            for (CSVRecord record : records) {
                if (record.get("release1").equals(version1) && record.get("release2").equals(version2)) {
                    value = Double.parseDouble(record.get("value"));
                }
            }
        } catch (IOException e) {
            LoggerHelpers.instance().warning("Unable to read Japicmp csv:\n"+e);
        }
        return value;
    }

    private static void appendValueToCsv(String version1, String version2, String value) throws IOException {
        FileWriter writer = new FileWriter(CSV_FILE_PATH, true);
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
        printer.printRecord(version1, version2, value);
        printer.close();
    }

    public static double computeChangeCostWithoutOptions(UpdateNode oldRelease, UpdateNode newRelease) {
        if (oldRelease.equals(newRelease)) {
            return 0.0;
        }
        String[] oldReleaseSplited = oldRelease.id().split(":");
        String[] newReleaseSplited = oldRelease.id().split(":");
        if (oldReleaseSplited.length < 3 || newReleaseSplited.length < 3 ){
            return 0.0;
        }
        JarArchiveComparatorOptions comparatorOptions = new JarArchiveComparatorOptions();
        JarArchiveComparator jarArchiveComparator = new JarArchiveComparator(comparatorOptions);
        MavenLocalRepository mavenLocalRepository = MavenLocalRepository.getInstance();

        JApiCmpArchive oldArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(oldRelease.id())), oldReleaseSplited[2]);
        JApiCmpArchive newArchive = new JApiCmpArchive(new File(mavenLocalRepository.downloadArtifact(newRelease.id())), newReleaseSplited[2]);

        List<JApiClass> jApiClasses = jarArchiveComparator.compare(oldArchive, newArchive);
        return jApiClasses.stream()
                .filter(jApiClass -> {
                    JApiChangeStatus status = jApiClass.getChangeStatus();
                    return status == JApiChangeStatus.MODIFIED || status == JApiChangeStatus.REMOVED;
                })
                .count();
    }
}
