mvn install:install-file -Dfile=C:\Users\I542791\Documents\Recherche\Projects\Goblin-Ecosystem\goblinUpdater\libs\goblinWeaver-1.0.0.jar -DgroupId='com.cifre.sap.su' -DartifactId=goblinWeaver -Dversion='1.0.0' -Dpackaging=jar

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile='./libs/goblinWeaver-1.0.0.jar'


mvn clean install exec:java