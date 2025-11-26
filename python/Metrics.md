## But what does all of this mean?

### Correlation

In statistics, **correlation** is used to understand the relationship between two variables. The **correlation coefficient** is a signed value that lies between `-1` and `+1`, often referred to as the **correlation spectrum**. It quantifies the strength and direction of a relationship between variables.  

- Values close to **+1** indicate a strong positive correlation: as one variable increases, the other tends to increase.  
- Values close to **-1** indicate a strong negative correlation: as one variable increases, the other tends to decrease.  
- Values near **0** suggest little or no linear relationship between the variables.  

Two variables are considered **perfectly correlated** if the correlation coefficient is approximately `1` or *`-1`.

![correlation_spectrum.png](correlation_spectrum.png)


### Correlation Matrix

A **correlation matrix** is a table that displays the correlation coefficients between multiple sets of variables. It is a convenient way to visualize the relationships among many variables simultaneously.  

Example of a correlation matrix:

|          | arg1 | arg2 | arg3 |
|----------|------|------|------|
| **arg1** | 1    | 0    | 0    |
| **arg2** | 0    | 1    | 0    |
| **arg3** | 0    | 0    | 1    |


In this particular experiment, the correlation matrix was used to **map and analyze the multitude of measurements** and understand the relationships among them. The correlation coefficients were calculated using the `.corr()` function in Python’s `pandas` library.

### Interpretation of Results

#### Priority Scheduling
The Priority algorithm shows distinct correlation patterns:

![correlation_priority.png](cm_priority.pn)

Key observations:
- Turnaround, waiting, and response times show perfect correlation (1.00)
- Burst time has minimal correlation with other metrics
- Arrival time shows slight negative correlation with time metrics

Both a task's burst time and arrival time have virtually no effect on the impact on its runtime nor access time. Priority dictates the play. Consiquently, this algorithm has the
potential to be most unfair and least efficient algorithm if priorities are poorly assigned to their tasks.


#### Round Robin Scheduling
Round Robin exhibits different behavior:

![correlation_roundrobin.png](cm_rr.png)

Notable patterns:
- Burst time shows strong positive correlation (0.78) with turnaround time
- Response and arrival times are perfectly correlated (1.00)
- More moderate correlations overall compared to other algorithms

Longer processes wait longer overall, but response time doesn't depend heavily on burst time of tasks and processes. This ensures that the initial execution time alloted to a task (`response time`) is allocated in a generally fair `First-Come-First-Serve` manner.However, waiting time still accumulates for longer jobs 

#### Shortest Job First (SJF)
SJF demonstrates the strongest correlations:

![correlation_sjf.png](cm_sjf.png)

Key findings:
- Near-perfect correlation (0.94-0.95) between burst time and all time metrics
- Arrival time consistently shows negative correlation (-0.13)
- Highest overall correlation strength among all algorithms

Longer processes are starved and shorter process are considered first class citizens. Extrememly efficient but extremely unfair.

# Limitations

Although on paper the SJF algorithm may appear to be the most efficient scheduling method for job systems,
scheduling systems factors related to user experience and responsiveness in GUI environments were not included in our test cases.
As a result, there is a lack of substantial evidence needed to conclusively determine the best algorithm based solely on run-time execution 
for these specific tasks.