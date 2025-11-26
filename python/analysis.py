import streamlit as st
import pandas as pd
import numpy as np
import plotly.express as px
import plotly.graph_objects as go
from plotly.subplots import make_subplots

import base64
from pathlib import Path

def img_to_base64(img_path):
    """Convert image to base64 string"""
    with open(img_path, "rb") as img_file:
        return base64.b64encode(img_file.read()).decode()

def process_markdown_with_images(md_content, image_mapping):
    """
    Replace markdown image syntax with base64 embedded images
    
    Args:
        md_content: Markdown content as string
        image_mapping: Dict mapping image placeholders to actual file paths
    """
    processed_content = md_content
    
    for img_name, img_path in image_mapping.items():
        if Path(img_path).exists():
            img_base64 = img_to_base64(img_path)
            img_ext = Path(img_path).suffix.lower()
            mime_type = {
                '.png': 'image/png',
                '.jpg': 'image/jpeg',
                '.jpeg': 'image/jpeg',
                '.gif': 'image/gif',
                '.svg': 'image/svg+xml'
            }.get(img_ext, 'image/png')
            

            markdown_syntax = f"![{img_name}]({img_name})"
            html_img = f'<img src="data:{mime_type};base64,{img_base64}" style="max-width:100%;" alt="{img_name}">'
            processed_content = processed_content.replace(markdown_syntax, html_img)
        else:
            st.warning(f"⚠️ Image not found: {img_path}")
    
    return processed_content

st.set_page_config(page_title="Scheduling Algorithm Analysis", layout="wide")
st.title("📊 CPU Scheduling Algorithm Performance Analysis")

# ---------------------------
# Define CSV paths for each algorithm
# ---------------------------

csv_paths = {
    'Priority': 'metrics/results_priority.csv',
    'Round Robin': 'metrics/results_rr.csv',
    'SJF': 'metrics/results_sjf.csv',
    'MLQ': '',  # optional
}

# ---------------------------
# Load CSVs and label
# ---------------------------

dfs = []
for algo, path in csv_paths.items():
    try:
        df = pd.read_csv(path)
        df['algorithm'] = algo
        dfs.append(df)
    except FileNotFoundError:
        st.warning(f"CSV for {algo} not found at {path}, skipping.")

if len(dfs) == 0:
    st.error("⚠️ No CSV files found. Please check the paths.")
    st.stop()

# Combine all dataframes
combined_df = pd.concat(dfs, ignore_index=True)

# ---------------------------
# Tabs for analysis
# ---------------------------

tab1, tab2, tab3, tab4 = st.tabs([
    "Summary Statistics",
    "📊 Comparative Analysis",
    "🔗 Statistical Insights",
    "📚 Documentation"
])

metrics = ['turnaround', 'waiting', 'response', 'burst']

# ---------------------------
# Tab 1: Summary Statistics
# ---------------------------

with tab1:
    st.header("Summary Statistics by Algorithm")
    for algo in combined_df['algorithm'].unique():
        st.subheader(f"🔹 {algo} Scheduling")
        algo_df = combined_df[combined_df['algorithm'] == algo]

        col1, col2, col3, col4 = st.columns(4)
        with col1:
            st.metric("Avg Turnaround", f"{algo_df['turnaround'].mean():.2f}")
     
        with col2:
            st.metric("Avg Waiting", f"{algo_df['waiting'].mean():.2f}")
   
        with col3:
            st.metric("Avg Response", f"{algo_df['response'].mean():.2f}")
   
        with col4:
            st.metric("Total Processes", len(algo_df))
            st.metric("Avg Burst", f"{algo_df['burst'].mean():.2f}")


# ---------------------------
# Tab 2: Comparative Analysis
# ---------------------------

with tab2:
    st.header("Algorithm Comparison")
    comparison_data = []
    for algo in combined_df['algorithm'].unique():
        algo_df = combined_df[combined_df['algorithm'] == algo]
        comparison_data.append({
            'Algorithm': algo,
            'Avg Turnaround': algo_df['turnaround'].mean(),
            'Avg Waiting': algo_df['waiting'].mean(),
            'Avg Response': algo_df['response'].mean(),
            'Processes': len(algo_df)
        })
    comparison_df = pd.DataFrame(comparison_data)

    fig = make_subplots(rows=1, cols=3, subplot_titles=("Turnaround Time", "Waiting Time", "Response Time"))
    colors = px.colors.qualitative.Set2
    for i, metric in enumerate(['Avg Turnaround', 'Avg Waiting', 'Avg Response']):
        for j, algo in enumerate(comparison_df['Algorithm']):
            fig.add_trace(
                go.Bar(
                    name=algo,
                    x=[metric.split()[1]],
                    y=[comparison_df[comparison_df['Algorithm']==algo][metric].values[0]],
                    marker_color=colors[j],
                    showlegend=(i==0)
                ), row=1, col=i+1
            )
    fig.update_layout(height=400, showlegend=True, barmode='group')
    st.plotly_chart(fig, use_container_width=True)

    st.dataframe(
        comparison_df.style.highlight_min(
            subset=['Avg Turnaround','Avg Waiting','Avg Response'], color='lightgreen'
        ).format({'Avg Turnaround':'{:.2f}','Avg Waiting':'{:.2f}','Avg Response':'{:.2f}'}),
        use_container_width=True
    )


# ---------------------------
# Tab 3: Correlation Insights
# ---------------------------

with tab3:
    st.header("Statistical Insights")
    for algo in combined_df['algorithm'].unique():
        st.subheader(f"🔹 {algo} Scheduling")
        algo_df = combined_df[combined_df['algorithm']==algo]
        corr_cols = ['burst','turnaround','waiting','response','arrival']
        corr_matrix = algo_df[corr_cols].corr()
        st.plotly_chart(px.imshow(corr_matrix, text_auto='.2f', aspect='auto', color_continuous_scale='RdBu_r', title=f'Correlation Matrix - {algo}'), use_container_width=True)

        col1, col2 = st.columns(2)
        with col1:
            st.write("**Strongest Positive Correlations:**")
            corr_flat = corr_matrix.unstack()
            corr_flat = corr_flat[corr_flat<1.0]
            top_corr = corr_flat.nlargest(3)
            for idx, val in top_corr.items():
                st.write(f"- {idx[0]} ↔ {idx[1]}: {val:.3f}")
        with col2:
            st.write("**Burst Time Impact:**")
            st.write(f"- On Turnaround: {corr_matrix.loc['burst','turnaround']:.3f}")
            st.write(f"- On Waiting: {corr_matrix.loc['burst','waiting']:.3f}")
            st.write(f"- On Response: {corr_matrix.loc['burst','response']:.3f}")
    
    try:
        with open('Metrics.md', 'r') as f:
            documentation_md = f.read()
        

        image_mapping = {
            "correlation_spectrum.png": "assets/correlation_coefficient.png",
            "cm_sjf.png": "assets/cm_sjf.png",
            "cm_rr.png": "assets/cm_rr.png",
            "cm_priority.png": "assets/cm_priority.png"
       
        }

        processed_md = process_markdown_with_images(documentation_md, image_mapping)
 
        st.markdown(processed_md, unsafe_allow_html=True)
        
    except FileNotFoundError:
        st.error("⚠️ Documentation file 'Metrics.md' not found. Please ensure it exists in the project directory.")
# ---------------------------
# Tab 4: Documentation
# ---------------------------

with tab4:
    st.header("📚 Implementation Documentation")
    
    try:
        with open('../scheduler-cli-1.0.0/DOCUMENTAION.md', 'r') as f:
            documentation_md = f.read()
        st.markdown(documentation_md, unsafe_allow_html=True)

    except FileNotFoundError:
        st.error("⚠️ Documentation file 'documentation.md' not found. Please ensure it exists in the project directory.")
        
        

# ---------------------------
# Sidebar Insights
# ---------------------------

st.sidebar.header("📋 Quick Insights")
best_turnaround = comparison_df.loc[comparison_df['Avg Turnaround'].idxmin(), 'Algorithm']
best_waiting = comparison_df.loc[comparison_df['Avg Waiting'].idxmin(), 'Algorithm']
best_response = comparison_df.loc[comparison_df['Avg Response'].idxmin(), 'Algorithm']
st.sidebar.success(f"🏆 Best Turnaround: **{best_turnaround}**")
st.sidebar.success(f"🏆 Best Waiting: **{best_waiting}**")
st.sidebar.success(f"🏆 Best Response: **{best_response}**")
st.sidebar.info(f"📊 Total Processes Analyzed: **{len(combined_df)}**")
st.sidebar.info(f"🔢 Algorithms Compared: **{len(dfs)}**")