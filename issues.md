# Issues

## Bugs

- we should multiply the constraints limits by k
- in the computation of the change edges, we use a copy of the graph in reading while we write in the orginal graph.
  We should rather generated a list of change edges and AT THE END add these in the graph.

## Ideas

- replace constraints CVE_LIMIT and COST_LIMIT by LIMIT with an added metric
  
    before:  
    ```
    constraints:
    - constraint: CVE_LIMIT
      value: 0.0
    ```

    after: 
    ```
    constraints: 
    - constraint: LIMIT
      metric: CVE
      value: 0.0
    ```

- add parameter for LOG information (LOW, INFO, ...)
  