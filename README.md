# Scheduler CLI v1.0.0

A command-line CPU scheduling simulator supporting multiple scheduling algorithms with performance metrics and analysis.

[Executable JAR](https://github.com/OwenMargiela/Scheduler/releases/download/v1/scheduler-cli.jar)

## Quick Start


## Interactive Mode

Start the interactive CLI to run commands sequentially:
```bash
./scheduler-cli
# Then type commands at the prompt:
> run --policy SJF
> add --file jobs.csv
> metrics --save results.csv
> cmqueue
> exit
```

## Commands

### run
Start scheduler with a specified policy
```bash
> run --policy SJF                    # Shortest Job First
> run --policy RR                     # Round Robin (default quantum: 10 ticks)
> run --policy PRIORITY               # Priority Scheduling
> run --policy MLQ --queues SJF,RR    # Multi-Level Queue with custom queues
```

### add
Add jobs to the scheduler
```bash
> add --file /path/to/jobs.csv        # Load from CSV (columns: burst,priority,scheduledPriority)
> add --burst 10 --priority 2         # Add single job manually
```

### metrics
Display and save scheduling performance metrics
```bash
> metrics                             # Print metrics to console
> metrics --save results.csv          # Save per-job metrics to CSV
```

### cmqueue
Display all completed jobs
```bash
> cmqueue
```

### help
Show all commands
```bash
> help
```

### exit
Exit the CLI
```bash
> exit
```

## Scheduling Policies

- **SJF** - Shortest Job First (non-preemptive)
- **RR** - Round Robin (preemptive, configurable quantum)
- **PRIORITY** - Priority Scheduling (non-preemptive)
- **MLQ** - Multi-Level Queue (customizable with multiple policies)

## Performance Metrics

The `metrics` command outputs:
- **Average Turnaround Time** - Time from arrival to completion
- **Average Waiting Time** - Time spent in queue (turnaround - burst)
- **Average Response Time** - Time from arrival to first execution
- **CPU Utilization** - Percentage of simulated time CPU was busy
- **Throughput** - Jobs completed per simulation tick

## Input Format (CSV)

Jobs file format (columns):
```
burst_time,priority,scheduled_priority
10,1,1
15,2,2
8,1,1
```

Where:
- `burst_time`: CPU time units needed
- `priority`: Job priority (1=highest, used for priority scheduling)
- `scheduled_priority`: Priority level for multi-level queue

## Example Workflow

1. Extract and navigate to the distribution folder
2. Create or prepare a `jobs.csv` file
3. Run interactive mode:
   ```bash
   ./scheduler-cli
   > run --policy MLQ --queues SJF,RR
   > add --file jobs.csv
   > metrics --save results_mlq.csv
   > exit
   ```
4. Analyze the generated `results_mlq.csv` file

## Troubleshooting

- **"Java not found"**: Install Java 11+ from https://adoptium.net/
- **"Command not recognized"**: Ensure you're in the scheduler-cli directory
- **Permission denied** (Linux/macOS): Run `chmod +x scheduler-cli` first
- **Metrics shows zeros**: Ensure scheduler is running and jobs have been added


