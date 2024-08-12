# Goblin updater
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)

The Goblin updater formulates the dependency update problem as a multi-objective optimization problem. This approach allows for the update of dependencies with a global perspective, considering all direct and indirect dependencies. It also enables developers to specify their preferences regarding the quality factors they want to maximize and the costs of updating they want to minimize. The update problem is encoded as a linear program whose solution is an optimal update strategy that aligns with developer priorities and minimizes incompatibilities.

An example of experiments with this tool is available here: https://zenodo.org/records/13285362

## Requirements
- Java 17
- An active Neo4j database containing the Maven Central dependency graph available here: https://zenodo.org/records/11104819
- A Goblin weaver API available here: https://github.com/Goblin-Ecosystem/goblinWeaver

## Source code structure

- client : clients
- updater : Goblin updater
  - api : API for the updater
  - impl : implementation of the API
  - helpers : helpers using the API
- util : utilities that are not specific to Goblin updater

## JAR generation

```sh
mvn clean package
```

## Execution

To run Goblin Updater, you have first to run the Goblin Weaver server. More information [here](https://github.com/Goblin-Ecosystem/goblinWeaver).

1. run Neo4J and launch the graph database for the ecosystem

2. run Goblin Weaver server in a first terminal
    ```sh
    java -Dneo4jUri="bolt://localhost:7687/" -Dneo4jUser="neo4j" -Dneo4jPassword="goblindb" -jar ./target/goblinWeaver-1.0.0.jar noUpdate
    ```

3. run Goblin Updater in a second terminal
    ```sh
    java -DweaverUrl=<WEAVER URL> -DprojectPath=<PROJECT TO ANALYSE> -DconfFile=<CONFIGURATION FILE> -jar <JAR FILE>
    ```

Windows example:

```sh
java -DweaverUrl="http://localhost:8080" -DprojectPath="C:\Users\Bob\MyProgram" -DconfFile=".\gUpdaterConfig.yml" -jar .\goblinUpdater-1.0.0-jar-with-dependencies.jar
```

Linux / MacOS example:

```sh
java -DweaverUrl="http://localhost:8080" -DprojectPath="$HOME/MyProgram" -DconfFile="./gUpdaterConfig.yml" -jar .\goblinUpdater-1.0.0-jar-with-dependencies.jar
```

## Configuration files

see the [template configuration file](gUpdaterConfig_TEMPLATE.yml)