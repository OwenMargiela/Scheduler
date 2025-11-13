package src.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import src.algorithms.MultiLevelQueue;
import src.algorithms.OrderedQueue;
import src.algorithms.RoundRobin;
import src.interfaces.Queue;

/**
 * The central controller class that manages job scheduling, execution order,
 * and time progression within the system.
 * <p>
 * A {@code Scheduler} acts as the runtime interface for interacting with one
 * or more {@link Queue} implementations.
 * 
 * This class can be constructed with a single policy, multiple policies (for
 * a multi-level queue), or a preconfigured {@link Queue} instance.
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * Scheduler scheduler = new Scheduler(Policies.ROUND_ROBIN);
 *
 * scheduler.open();
 * scheduler.push(new Job(3, 10));
 * scheduler.push(new Job(1, 5));
 *
 * List<Optional<Job>> results = scheduler.run();
 * scheduler.close();
 * }</pre>
 *
 * @see Queue
 * @see MultiLevelQueue
 * @see MultiLevelQueueBuilder
 * @see Job
 * @see Ticker
 */
public class Scheduler {

    /** The underlying scheduling queue implementation. */
    private final Queue queue;

    /** Keeps track of simulated time progression for scheduling events. */
    private final Ticker ticker;

    /** Thread-safe counter for generating job identifiers or tracking order. */
    private final AtomicInteger counter = new AtomicInteger(0);

    /** Utility class for generating random or preset job batches for testing. */
    public TaskGenerator generator = new TaskGenerator();

    /**
     * Constructs a {@code Scheduler} using a specific {@link Queue} instance.
     *
     * @param queue the queue implementation that defines the scheduling policy;
     *              must not be {@code null}.
     */
    public Scheduler(Queue queue) {
        this.queue = queue;
        this.ticker = new Ticker();
    }

    /**
     * Constructs a {@code Scheduler} backed by a {@link MultiLevelQueue}
     * using multiple {@link Policies}. Policies are assumed to be inserted
     * in priority order.
     *
     * @param policies one or more scheduling policies used to configure
     *                 the multi-level queue.
     */
    public Scheduler(Policies... policies) {
        MultiLevelQueueBuilder builder = new MultiLevelQueueBuilder();
        for (Policies policy : policies) {
            builder.addPolicy(policy);
        }
        this.queue = builder.build();
        this.ticker = new Ticker();
    }

    /**
     * Constructs a {@code Scheduler} configured with a single scheduling policy.
     * <p>
     *
     * 
     * @param policy the scheduling policy to use.
     */
    public Scheduler(Policies policy) {
        switch (policy) {
            case PRIORITY_SCHEDULING:
                this.queue = new OrderedQueue(Comparators.PRIORITY_SCHEDULING);
                break;
            case SJF:
                this.queue = new OrderedQueue(Comparators.SJF);
                break;
            default:
                this.queue = new RoundRobin();
                break;
        }
        this.ticker = new Ticker();
    }

    /**
     * Thread-safe method that enqueues a single {@link Job} into the scheduler.
     * <p>
     *
     * 
     * @param process the job to enqueue; must not be {@code null}.
     */
    public void push(Job process) {
        int arrivalTime = this.ticker.getTime();
        process.setPid(this.counter.getAndIncrement());
        // Record arrival time for metrics
        process.setAttribute(JobAttribute.ARRIVAL_TIME, arrivalTime);
        // Ensure remaining time is initialized (use burst time if not set)
        if (process.getAttribute(JobAttribute.REMAINING_TIME) == null) {
            Object bt = process.getAttribute(JobAttribute.BURST_TIME);
            if (bt instanceof Integer) {
                process.setAttribute(JobAttribute.REMAINING_TIME, (Integer) bt);
            }
        }
        queue.enqueue(process, arrivalTime);
        this.ticker.tick();
    }

    /**
     * Enqueues a list of {@link Job} instances into the scheduler, assigning
     * them all the same arrival time.
     * <p>
     *
     * 
     * @param processes the list of jobs to enqueue; must not be {@code null} or
     *                  empty.
     */
    public void append(List<Job> processes) {
        int arrivalTime = this.ticker.getTime();
        for (Job job : processes) {
            job.setPid(this.counter.getAndIncrement());
            // Record arrival time for metrics
            job.setAttribute(JobAttribute.ARRIVAL_TIME, arrivalTime);
            // Ensure remaining time is initialized (use burst time if not set)
            if (job.getAttribute(JobAttribute.REMAINING_TIME) == null) {
                Object bt = job.getAttribute(JobAttribute.BURST_TIME);
                if (bt instanceof Integer) {
                    job.setAttribute(JobAttribute.REMAINING_TIME, (Integer) bt);
                }
            }
        }
        queue.enqueueList(processes, arrivalTime);
        this.ticker.tick();
    }

    /**
     * Executes the full scheduling algorithm until all queues are empty.
     * <p>
     *
     * 
     * @return a list of {@link Optional} jobs that were executed or completed.
     */
    public List<Optional<Job>> run() {
        List<Optional<Job>> list = new ArrayList<>();

        while (!queue.isEmpty()) {

            Optional<Job> job = queue.dequeue();

            if (job.isEmpty())
                continue;

            Job j = job.get();

            // Record start/response time if this is the job's first execution
            Integer startTime = j.getAttribute(JobAttribute.START_TIME, Integer.class);
            if (startTime == null) {
                j.setAttribute(JobAttribute.START_TIME, this.ticker.getTime());
            }

            int excTime = (int) j.getAttribute(JobAttribute.LAST_EXECUTION_TIME, Integer.class);
            this.ticker.advance(excTime);

            if (j.isCompleted()) {
                // Record completion time (consistent across queue implementations)
                int completion = this.ticker.getTime();
                j.setCompletionTime(completion);
                j.setAttribute(JobAttribute.COMPLETION_TIME, completion);
                list.add(Optional.of(j));
            }
        }

        return list;
    }

    /**
     * Returns the current simulated time from the scheduler's ticker.
     */
    public int getTime() {
        return this.ticker.getTime();
    }

    /**
     * Executes the scheduling algorithm for a fixed number of steps.
     * <p>
     * Each step dequeues and executes one job for a simulated time slice.
     *
     * @param steps the number of scheduling iterations to perform.
     * @return a list of {@link Optional} jobs that were executed during the run.
     */
    public List<Optional<Job>> run(int steps) {
        List<Optional<Job>> list = new ArrayList<>();

        for (int i = 0; i < steps; i++) {
            Optional<Job> job = queue.dequeue();
            int excTime = 5; // Example time slice
            this.ticker.advance(excTime);
            list.add(job);
        }

        return list;
    }

    /**
     * Checks whether all jobs have been processed.
     *
     * @return {@code true} if all queues are empty; {@code false} otherwise.
     */
    public Boolean isEmpty() {
        return this.queue.isEmpty();
    }

    /**
     * Opens the scheduler and underlying queues for processing.
     */
    public void open() {
        this.queue.open();
    }

    /**
     * Closes the scheduler and all associated queues.
     */
    public void close() {
        this.queue.close(); // (Fixed: previously called open())
    }

    /**
     * Returns the number of jobs currently waiting across all queues.
     *
     * @return the number of enqueued jobs.
     */
    public int size() {
        return this.queue.size();
    }

    // Helper functions to make testin easier

    public Job make(TaskLoad taskLoad) {

        return generator.make(taskLoad, this.counter.getAndIncrement());

    }

    public List<Job> makeList(TaskLoad taskLoad, int size) {
        return generator.makeList(taskLoad, size, this.counter);

    }

}
