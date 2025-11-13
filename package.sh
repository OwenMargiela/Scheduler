#!/bin/bash
# Package the application into a lightweight JAR with external dependencies

set -e

echo "Building lightweight JAR..."

# Compile first
./build.sh

# Create manifest file with Class-Path
mkdir -p build
cat > build/Manifest.txt << EOF
Manifest-Version: 1.0
Main-Class: src.cmd.CommandInterface
Implementation-Title: Scheduler CLI
Implementation-Version: 1.0
Class-Path: lib/commons-lang3-3.19.0.jar lib/junit-platform-console-standalone-1.13.0-M3.jar lib/opencsv-5.12.0.jar lib/picocli-4.7.7.jar
EOF

# Create the lightweight JAR
jar cfm scheduler-cli.jar build/Manifest.txt -C bin .

# Cleanup
rm -rf build

echo "✓ Lightweight JAR created: scheduler-cli.jar"
echo "ℹ️  Make sure lib/ folder is in the same directory as the JAR"
echo "Run with: java -jar scheduler-cli.jar [options]"
