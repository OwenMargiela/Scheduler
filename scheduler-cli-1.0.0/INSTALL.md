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

## Uninstallation

Simply delete the extracted `scheduler-cli-1.0.0` folder.

