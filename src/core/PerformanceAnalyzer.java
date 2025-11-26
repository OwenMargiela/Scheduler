package src.core;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class PerformanceAnalyzer {

    public static class Metrics {
        public double avgWaiting;
        public double avgTurnaround;
        public double avgResponse;
        public double cpuUtilization; // percent
        public double throughput; // jobs per unit time
        public int totalJobs;
        public int totalTime;
    }

    public static Metrics compute(List<Job> jobs, int totalTime) {
        Metrics m = new Metrics();
        if (jobs == null || jobs.isEmpty() || totalTime <= 0) {
            m.totalJobs = (jobs == null) ? 0 : jobs.size();
            m.totalTime = totalTime;
            return m;
        }

        double sumWaiting = 0;
        double sumTurnaround = 0;
        double sumResponse = 0;
        double sumBurst = 0;

        for (Job j : jobs) {
            Integer arrival = j.getAttribute(JobAttribute.ARRIVAL_TIME, Integer.class);
            Integer start = j.getAttribute(JobAttribute.START_TIME, Integer.class);
            Integer burst = j.getAttribute(JobAttribute.BURST_TIME, Integer.class);
            int completion = j.getCompletionTime();

            if (arrival == null || start == null || burst == null || completion <= 0) {

                continue;
            }

            int turnaround = completion - arrival;
            int waiting = turnaround - burst;
            int response = start - arrival;

            sumTurnaround += turnaround;
            sumWaiting += waiting;
            sumResponse += response;
            sumBurst += burst;
        }

        int n = jobs.size();
        m.totalJobs = n;
        m.totalTime = totalTime;
        m.avgWaiting = sumWaiting / n;
        m.avgTurnaround = sumTurnaround / n;
        m.avgResponse = sumResponse / n;
        m.cpuUtilization = (sumBurst / (double) totalTime) * 100.0;
        m.throughput = n / (double) totalTime;

        return m;
    }

    public static void print(Metrics m, PrintWriter out) {
        out.printf("Completed Jobs: %d\n", m.totalJobs);
        out.printf("Total simulation time: %d ticks\n", m.totalTime);
        out.printf("Average turnaround time: %.3f\n", m.avgTurnaround);
        out.printf("Average waiting time:    %.3f\n", m.avgWaiting);
        out.printf("Average response time:   %.3f\n", m.avgResponse);
        out.printf("CPU utilization:         %.2f %%\n", m.cpuUtilization);
        out.printf("Throughput:              %.5f jobs/tick\n", m.throughput);
    }

    public static void saveCsv(List<Job> jobs, String path) throws Exception {
        try (FileWriter fw = new FileWriter(path); PrintWriter pw = new PrintWriter(fw)) {
            pw.println("pid,arrival,start,completion,burst,turnaround,waiting,response");
            for (Job j : jobs) {
                Integer arrival = j.getAttribute(JobAttribute.ARRIVAL_TIME, Integer.class);
                Integer start = j.getAttribute(JobAttribute.START_TIME, Integer.class);
                Integer burst = j.getAttribute(JobAttribute.BURST_TIME, Integer.class);
                int completion = j.getCompletionTime();
                if (arrival == null || start == null || burst == null || completion <= 0)
                    continue;
                int turnaround = completion - arrival;
                int waiting = turnaround - burst;
                int response = start - arrival;
                pw.printf("%d,%d,%d,%d,%d,%d,%d,%d\n", j.getPid(), arrival, start, completion, burst, turnaround,
                        waiting, response);
            }
        }
    }

}
