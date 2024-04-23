# Issues

## Bugs

- Do not use CVE_LIMIT is CVE is not part of the metrics

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
  