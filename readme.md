TODO: Install Maracas process

mvn clean install exec:java

java -DweaverUrl="http://localhost:8080" -DprojectPath="C:\Users\I542791\Desktop\expUpdate\goblinWeaver" -DconfFile="C:\Users\I542791\Desktop\expUpdate\goblinWeaver\gUpdaterConfig.yml" -jar .\goblinUpdater-1.0.0-jar-with-dependencies.jar