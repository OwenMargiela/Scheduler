package src.cmd;

import java.io.FileReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ArgGroup;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import src.algorithms.RoundRobin;
import src.core.Job;
import src.core.JobAttribute;
import src.core.MultiLevelQueueBuilder;
import src.core.Policies;
import src.core.Scheduler;
import src.interfaces.Queue;
import src.core.PerformanceAnalyzer;
import java.io.PrintWriter;
import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.OutputStreamWriter;
// import java.io.UnsupportedEncodingException;

@Command(name = "Scheduler", version = "1.0", mixinStandardHelpOptions = true, description = "Interactive real-time scheduler CLI", subcommands = {
        CommandInterface.RunCommand.class,
        CommandInterface.AddCommand.class,

    CommandInterface.ShowCompletedQueueCommand.class,
    CommandInterface.MetricsCommand.class,

})
public class CommandInterface implements Runnable {

    private Scheduler scheduler;

    private boolean running;

    private final ExecutorService schedulerExecutor = Executors.newSingleThreadExecutor();

    private final List<Job> completedJobs = Collections.synchronizedList(new ArrayList<>());
    // ========================
    // RUN SUBCOMMAND
    // ========================

    @Command(name = "run", mixinStandardHelpOptions = true, description = "Run the scheduler with the specified policy")
    static class RunCommand implements Runnable {

        @ParentCommand
        private CommandInterface parent; // Access to the parent's scheduler

        @Option(names = { "-p", "--policy" }, description = "Policy by which the scheduler should function")
        private String policy;

        @Option(names = { "-q", "--quantum" }, description = "Time quantum for the round robin scheduling algorithm")
        private Integer timeQuantum = 10;

        @Option(names = {
                "--queues" }, split = ",", description = "Queue policies for MLQ (comma-separated): e.g., SJF, RR, PRIORITY")
        private List<String> queuePolicies;

        @Option(names = { "-o", "-output" }, description = "Flag to Log all completed jobs to the Completed Log")
        private boolean output;

        @Override
        public void run() {
            this.parent.running = true;

            if (this.parent.running) {
                this.parent.scheduler = createScheduler();
                this.parent.scheduler.open();
                this.parent.schedulerExecutor.submit(this::backgroundScheduler);

            }

        }

        private void backgroundScheduler() {
            while (this.parent.running) {

                // Only run if scheduler has jobs AND is marked ready
                if (!this.parent.scheduler.isEmpty()) {

                    List<Optional<Job>> executed = this.parent.scheduler.run();

                    for (Optional<Job> opt : executed) {
                        Job job = opt.get();
                        if (job.isCompleted()) {
                            this.parent.completedJobs.add(job);
                        }
                    }
                }

                try {
                    Thread.sleep(50); // small tick
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        private Scheduler createScheduler() {
            return switch (this.policy.toUpperCase()) {
                case "SJF" -> new Scheduler(Policies.SJF);
                case "RR", "ROUND_ROBIN" -> {

                    RoundRobin rr = new RoundRobin();
                    rr.setQuanta(timeQuantum);
                    yield new Scheduler(rr);
                }
                case "PRIORITY" -> new Scheduler(Policies.PRIORITY_SCHEDULING);
                case "MLQ", "MULTILEVEL" -> {
                    // Example: configure a multi-level queue scheduler
                    MultiLevelQueueBuilder mlqSchedulerBuilder = new MultiLevelQueueBuilder();

                    for (String policy : queuePolicies) {

                        switch (policy) {
                            case "SJF":
                                mlqSchedulerBuilder.addPolicy(Policies.SJF);
                                break;
                            case "RR", "ROUND_ROBIN":
                                mlqSchedulerBuilder.addPolicy(Policies.ROUND_ROBIN);

                                break;
                            case "PRIORITY":
                                mlqSchedulerBuilder.addPolicy(Policies.PRIORITY_SCHEDULING);
                                break;

                            default:
                                mlqSchedulerBuilder.addPolicy(Policies.SJF);
                                break;
                        }

                    }

                    Queue mlq = mlqSchedulerBuilder.build();
                    yield new Scheduler(mlq);
                }
                default -> new Scheduler(Policies.SJF);
            };
        }

    }

    // ========================
    // ADD SUBCOMMAND
    // ========================

    @Command(name = "add", mixinStandardHelpOptions = true, description = "Add a job/task to the scheduler.")
    static class AddCommand implements Runnable {

        @ParentCommand
        private CommandInterface parent; // Access to the parent's scheduler

        @ArgGroup(exclusive = true, multiplicity = "1")
        InputOptions input;

        static class InputOptions {
            enum InputType {
                MANUAL, FILE
            }

            @ArgGroup(exclusive = false, multiplicity = "1")
            ManualInput manual;

            @ArgGroup(exclusive = false, multiplicity = "1")
            FileInput file;

            public InputType getType() {
                return manual != null ? InputType.MANUAL : InputType.FILE;
            }

        }

        static class ManualInput {
            @Option(names = { "-b", "--burst" }, required = true, description = "Burst time of the job")
            int burstTime;

            @Option(names = { "-p", "--priority" }, description = "Priority of the job")
            Integer priority;
        }

        static class FileInput {
            @Option(names = { "-f", "--file" }, required = true, description = "CSV file with jobs (burst,priority)")
            String csvFile;
        }

        @Override
        public void run() {

            switch (input.getType()) {
                case MANUAL:
                    Job job = new Job();

                    job.setAttribute(JobAttribute.BURST_TIME, input.manual.burstTime);

                    if (input.manual.priority != null) {
                        job.setAttribute(JobAttribute.SCHEDULED_PRIORITY, input.manual.priority);

                    }
                    this.parent.scheduler.push(job);
                    break;

                case FILE:
                    try {
                        FileReader fileReader = new FileReader(this.input.file.csvFile);
                        CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
                        CSVReader csvReader = new CSVReaderBuilder(fileReader)
                                .withCSVParser(parser)
                                .build();

                        List<String[]> allData = csvReader.readAll();
                        List<Job> tasks = new ArrayList<>();

                        for (String[] row : allData) {
                            // Defensive: skip empty or malformed rows
                            if (row.length < 2)
                                continue;

                            Job task = new Job();

                            int burstTime = Integer.parseInt(row[0].trim());
                            int priority = Integer.parseInt(row[1].trim());
                            int schedledPriority = Integer.parseInt(row[2].trim());

                            task.setAttribute(JobAttribute.BURST_TIME, burstTime);
                            task.setAttribute(JobAttribute.SCHEDULED_PRIORITY, schedledPriority);
                            task.setAttribute(JobAttribute.REMAINING_TIME, burstTime);
                            task.setAttribute(JobAttribute.PRIORITY, priority);

                            tasks.add(task);
                        }

                        this.parent.scheduler.append(tasks);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }

        }
    }

    // // ========================
    // // Cqueue SUBCOMMAND
    // // ========================

    // @Command(name = "cqueue ", mixinStandardHelpOptions = true, description =
    // "Prints a snapt shot of the job queue")
    // static class CheckCommand implements Runnable {

    // @ParentCommand
    // private CommandInterface parent; // Access to the parent's scheduler

    // @Override
    // public void run() {

    // // TODO Auto-generated method stub

    // }
    // }

    // ========================
    // Cmqueue SUBCOMMAND
    // ========================

    @Command(name = "cmqueue", mixinStandardHelpOptions = true, description = "Displays a list of all completed jobs")
    static class ShowCompletedQueueCommand implements Runnable {

        @ParentCommand
        private CommandInterface parent; // Access to the parent's scheduler

        @Override
        public void run() {

            for (Job job : this.parent.completedJobs) {

                System.out.printf(
                        "[✓ COMPLETED] Job PID=%4d | Priority=%3d | Scheduled Priority=%3d | Burst=%3d%n",
                        job.getPid(),
                        job.getAttribute(JobAttribute.PRIORITY, Integer.class),
                        job.getAttribute(JobAttribute.SCHEDULED_PRIORITY, Integer.class),
                        job.getAttribute(JobAttribute.BURST_TIME, Integer.class));

            }

        }
    }

    @Command(name = "metrics", mixinStandardHelpOptions = true, description = "Compute and display performance metrics for completed jobs")
    static class MetricsCommand implements Runnable {

        @ParentCommand
        private CommandInterface parent;

        @Option(names = { "-s", "--save" }, description = "Optional CSV path to save per-job metrics")
        private String savePath;

        @Override
        public void run() {
            try {
                List<Job> completed = this.parent.completedJobs;
                int totalTime = (this.parent.scheduler != null) ? this.parent.scheduler.getTime() : 0;

                PerformanceAnalyzer.Metrics m = PerformanceAnalyzer.compute(completed, totalTime);

                PrintWriter pw = new PrintWriter(System.out);
                PerformanceAnalyzer.print(m, pw);
                pw.flush();

                if (savePath != null && !savePath.isEmpty()) {
                    File outFile = new File(savePath);
                    PerformanceAnalyzer.saveCsv(completed, outFile.getAbsolutePath());
                    System.out.println("Saved CSV to: " + outFile.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Failed to compute metrics: " + e.getMessage());
            }
        }

    }

    @Override
    public void run() {

        CommandLine.usage(this, System.out);

    }

    public static void main(String[] args) {
        CommandInterface app = new CommandInterface(); // Single instance
        CommandLine cmd = new CommandLine(app); // Reuse this instance

        if (args.length == 0) {
            // Interactive mode
            Scanner scanner = new Scanner(System.in);
            System.out.println("Scheduler CLI - Type 'help' for commands, 'exit' to quit");

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty())
                    continue;

                if (line.equals("exit") || line.equals("quit")) {
                    // Graceful shutdown
                    if (app.scheduler != null) {
                        app.running = false;
                        app.schedulerExecutor.shutdown();
                        try {
                            app.schedulerExecutor.awaitTermination(2, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            app.schedulerExecutor.shutdownNow();
                        }
                        app.scheduler.close();
                    }
                    break;
                }

                try {
                    String[] cmdArgs = line.split("\\s+");
                    cmd.execute(cmdArgs); // Execute on SAME app instance
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            System.out.println("Goodbye!");
            scanner.close();
        } else {
            // Single command mode
            int exitCode = cmd.execute(args);

            System.exit(exitCode);
        }
    }
}