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

```yaml
#
# update configuration file
#
metrics:
  # list of couples with metric: metric name and coef: double
  # - at least one quality metric
  # - at least one cost metric
  # - sum of coefs for quality metrics must be 1
  - metric: CVE
    coef: 0.6
  - metric: FRESHNESS
    coef: 0.2
  - metric: POPULARITY_1_YEAR
    coef: 0.2
  - metric: COST
    coef: 0.5
constraints:
  # list (possibly empty) of constraints with constraint: and value:
  # a constraint can be one of:
  # - constraint: COST_LIMIT and value: double -> maximum value of total cost of solution
  # - constraint: CVE_LIMIT and value: double -> maximum value of total CVE of solution
  # - constraint: PRESENCE and value: some id -> id must be present in solution
  # - constraint: ABSENCE and value: some id -> id must be absent in solution
  # notes:
  # - if you use both PRESENCE and ABSENCE for some same id, there is no solution
  # - if you use COST_LIMIT and no solution exist under this limit, there is no solution
  - constraint: COST_LIMIT
    value: 0.0
  - constraint: CVE_LIMIT
    value: 0.0
  - constraint: PRESENCE
    value: "group:artifact:version"
  - constraint: ABSENCE
    value: "groupe:artifact:version"
releases:
  # either NONE, ROOT (default), CONSTRAINTS (root + all libs of releases in ABSENCE), or ALL
  # (!!) by now only ROOT is supported (other -> ROOT)
  focus: ROOT
  # combination of MORE_RECENT, NO_PATCHES
  # can be empty to select all, default is [MORE_RECENT]
  # (!!) by now only [MORE_RECENT] is supported (others -> [MORE_RECENT])
  selectors: [MORE_RECENT]
costs:
   # either NONE, ROOT (default), CONSTRAINTS (root + all libs of releases in ABSENCE), or ALL
   # (!!) by now CONSTRAINTS is not supported (-> ROOT)
  focus: ROOT
  # default value for unknown costs, MAX or MIN
  default: MAX
  # tool used to compute costs at root level, either NONE or MARACAS
  # (!!) by now NONE is not supported (-> MARACAS)
  tool-direct: MARACAS
  # tool used to compute costs at non-root level, either NONE or JAPICMP
  # (!!) by now JAPICMP is not supported (-> NONE)
  tool-indirect: NONE
  ```