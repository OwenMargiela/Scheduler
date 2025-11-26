#!/bin/bash


PROJECT_DIR="/home/spaceriot/os_gpr/python"   # <-- Change this to your project path
APP_FILE="/home/spaceriot/os_gpr/python/analysis.py"                     # <-- Change this to your Streamlit app filename


echo "Navigating to project directory..."
cd "$PROJECT_DIR" || { echo "Project directory not found!"; exit 1; }


if [ ! -d ".venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv .venv
else
    echo "Virtual environment already exists."
fi

echo "Activating virtual environment..."
source .venv/bin/activate

echo "Upgrading pip..."
pip install --upgrade pip

echo "Installing required packages..."
pip install streamlit pandas numpy plotly


echo "Starting Streamlit app..."
streamlit run "$APP_FILE"

