STRUCTURE:

- client : clients
- updater : Goblin updater
  - api : API for the updater
  - impl : implementation of the API
  - helpers : helpers using the API
- util : utilities that are not specific to Goblin updater

GENERATE JAR:
mvn clean package

RUN JAR:
java -DweaverUrl="http://localhost:8080" -DprojectPath="C:\Users\I542791\Desktop\expUpdate\goblinWeaver" -DconfFile="C:\Users\I542791\Desktop\expUpdate\gUpdaterConfig.yml" -jar .\goblinUpdater-1.0.0-jar-with-dependencies.jar