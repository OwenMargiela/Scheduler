#!/bin/bash
# Create a distribution package

set -e

VERSION="1.0.0"
DIST_DIR="scheduler-cli-$VERSION"

echo "Creating distribution package..."

# Clean and prepare
rm -rf "$DIST_DIR" "$DIST_DIR.zip"
mkdir -p "$DIST_DIR/lib"

# Build JAR
./package.sh

#Creating the documentation file
touch $DIST_DIR/DOCUMENTAION.md

# Copy files to distribution
cp scheduler-cli.jar "$DIST_DIR/"
cp scheduler-cli "$DIST_DIR/"
cp README.md "$DIST_DIR/" 2>/dev/null || echo "# Scheduler CLI" > "$DIST_DIR/README.md"

# Create improved Windows batch launcher with pause and Java check
cat > "$DIST_DIR/scheduler-cli.bat" << 'BATEOF'
@echo off
REM Launcher script for scheduler-cli on Windows
REM Checks for Java and keeps window open to show errors

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo.
    echo Error: Java is not installed or not in PATH
    echo.
    echo Please download and install Java 11+ from:
    echo https://adoptium.net/
    echo.
    echo Make sure to check "Add to PATH" during installation.
    echo.
    pause
    exit /b 1
)

java -jar "%~dp0scheduler-cli.jar" %*
pause
BATEOF


# Copy lib folder
cp -r lib/* "$DIST_DIR/lib/" 2>/dev/null || true

# Create comprehensive README with usage instructions
cat > "$DIST_DIR/README.md" << 'EOF'
# Scheduler CLI v1.0.0

A command-line CPU scheduling simulator supporting multiple scheduling algorithms with performance metrics and analysis.

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


EOF

# Create install instructions
cat > "$DIST_DIR/INSTALL.md" << 'EOF'
# Installation Instructions

## Prerequisites
- Java Runtime Environment (JRE) 11 or higher
  - Download from: https://adoptium.net/

## Linux/macOS Installation

1. Extract the archive:
   ```bash
   unzip scheduler-cli-1.0.0.zip
   cd scheduler-cli-1.0.0
   ```

2. Make launcher executable:
   ```bash
   chmod +x scheduler-cli
   ```

3. Run:
   ```bash
   ./scheduler-cli run --policy SJF
   ```

4. (Optional) Add to PATH:
   ```bash
   sudo cp scheduler-cli /usr/local/bin/
   ```
   Then you can run `scheduler-cli` from anywhere.

## Windows Installation

1. Extract the ZIP file
2. Double-click `scheduler-cli.bat` or run in Command Prompt:
   ```cmd
   scheduler-cli.bat run --policy SJF
   ```
3. (Optional) Add folder to PATH for global access

## macOS (Homebrew)

If you have Homebrew:
```bash
brew install openjdk@11
```

## Java Installation

If Java is not installed:
- **Linux (Ubuntu/Debian)**:
  ```bash
  sudo apt-get update
  sudo apt-get install default-jre
  ```
  ```
- **macOS**:
  ```bash
  brew install openjdk@11
  ```
- **Windows**: Download installer from https://adoptium.net/

Verify installation:
```bash
java -version
```

Expected output (or similar):
```
openjdk version "11.0.x"
OpenJDK Runtime Environment
```

## Running the Application

### Interactive Mode (Recommended)
```bash
./scheduler-cli
```

### Single Command Mode
```bash
./scheduler-cli run --policy SJF
./scheduler-cli add --file jobs.csv
./scheduler-cli metrics
```

### Using JAR Directly (Any Platform)
```bash
java -jar scheduler-cli.jar run --policy SJF
```

## Uninstallation

Simply delete the extracted `scheduler-cli-1.0.0` folder.

If you added to PATH, remove the symlink/shortcut manually.
EOF

# Zip it up
zip -r "$DIST_DIR.zip" "$DIST_DIR" > /dev/null

echo "✓ Distribution package created: $DIST_DIR.zip"
echo ""
echo "Contents:"
ls -lh "$DIST_DIR/"
echo ""
echo "To share: send $DIST_DIR.zip to recipients"
echo "Recipients should extract and run: ./$DIST_DIR/scheduler-cli"
rm -f scheduler-cli-1.0.0/LICENSE
