#!/bin/bash
# Compile all Java source files into the bin directory

# Output directory for compiled classes
OUT_DIR="bin"

# Create the output directory if it doesn't exist
mkdir -p "$OUT_DIR"

# Clean old compiled classes (optional)
find "$OUT_DIR" -type f -name "*.class" -delete

# Compile all Java source files from src/ into the output directory
# -d specifies the output directory
# -cp includes all JARs in lib/
find src -name "*.java" | xargs javac -d "$OUT_DIR" -cp "lib/*"

# Check if compilation succeeded
if [ $? -eq 0 ]; then
    echo "Compiled successfully into $OUT_DIR!"
else
    echo "Compilation failed."
fi
