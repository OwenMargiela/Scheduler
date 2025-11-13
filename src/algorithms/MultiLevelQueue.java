package src.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.ConcurrentHashMap;

import src.core.Comparators;
import src.core.Job;
import src.core.JobAttribute;
import src.core.Policies;
import src.interfaces.Queue;

public class MultiLevelQueue implements Queue {
    public ConcurrentHashMap<Integer, Queue> priorityLevels;
    private volatile boolean on;

    /**
     * Constructs a new {@code MultiLevelQueue} composed of multiple scheduling
     * queues,
     * each representing a distinct priority level.
     * <p>
     * The queues are assumed to be provided in order of descending priority —
     * that is, the first queue has the highest priority, and the last queue has the
     * lowest.
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * MultiLevelQueue scheduler = new MultiLevelQueue(
     *         new RoundRobin(),
     *         new OrderedQueue(Comparators.PRIORITY_SCHEDULING),
     *         new OrderedQueue(Comparators.SJF));
     * }</pre>
     *
     * @param queues one or more scheduling queues to include in this multi-level
     *               structure.
     *               The order of insertion determines the priority of each level.
     */

    public MultiLevelQueue(Queue... queues) {

        ConcurrentHashMap<Integer, Queue> priorityLevels = new ConcurrentHashMap<>();
        Integer level = 1;

        for (Queue queue : queues) {
            priorityLevels.put(level, queue);

            level++;
        }

        this.priorityLevels = priorityLevels;
        System.out.println("Queues" + priorityLevels);
    }

    /**
     * Constructs a new {@code MultiLevelQueue} composed of multiple scheduling
     * policies,
     * each representing a distinct priority level.
     * <p>
     * The policies are assumed to be provided in order of descending priority —
     * that is, the first queue has the highest priority, and the last queue has the
     * lowest.
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * MultiLevelQueue scheduler = new MultiLevelQueue(
     *         Policies.SJF,
     *         Policies.ROUND_ROBIN,
     *         Policies.SJF);
     * }</pre>
     *
     * @param queues one or more scheduling policies to include in this multi-level
     *               structure.
     *               The order of insertion determines the priority of each level.
     */
    public MultiLevelQueue(Policies... policies) {
        ConcurrentHashMap<Integer, Queue> priorityLevels = new ConcurrentHashMap<>();
        Integer level = 1;

        for (Policies policy : policies) {

            switch (policy) {
                case SJF:

                    Queue sjf = new OrderedQueue(Comparators.SJF);
                    priorityLevels.put(level, sjf);

                    break;

                case ROUND_ROBIN:
                    Queue roundRobin = new RoundRobin();
                    priorityLevels.put(level, roundRobin);

                    break;

                case PRIORITY_SCHEDULING:
                    Queue priorityQueue = new OrderedQueue(Comparators.PRIORITY_SCHEDULING);
                    priorityLevels.put(level, priorityQueue);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown policy: " + policy);
            }

            level++;
        }
        this.priorityLevels = priorityLevels;

    }

    /**
     * Inserts a {@link Job} into the appropriate scheduling queue within the
     * {@code MultiLevelQueue} based on its priority.
     * <p>
     * This method associates the job with its arrival time in the system
     * <p>
     *
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * Job job = new Job(3, 10); // Priority 3, Burst time 10
     * scheduler.enqueue(job, 5); // Job arrives at time 5
     * }</pre>
     *
     * @param job         the {@link Job} instance to be enqueued.
     * @param arrivalTime the time (in system ticks or units) when the job arrives
     */

    @Override
    public void enqueue(Job job, int arrival_time) {

        Integer priority = (int) job.getAttribute(JobAttribute.PRIORITY, Integer.class);

        Queue priorityLevel = priorityLevels.get(priority);

        if (this.on) {
            if (priorityLevel == null) {
                if (priority < priorityLevels.size()) {
                    priorityLevel = priorityLevels.get(1);

                } else if (priority > priorityLevels.size()) {
                    priorityLevel = priorityLevels.get(priorityLevels.size());

                }

            }
            priorityLevel.enqueue(job, arrival_time);
        }

    }

    /**
     * Performs a single scheduling step by selecting the next {@link Job} to
     * execute
     * from the highest-priority non-empty queue in the {@code MultiLevelQueue}.
     * <p>
     * This method represents one iteration of the scheduling process — it retrieves
     * (and potentially updates) a job according to the scheduling policy of the
     * selected queue. Depending on the algorithm (e.g., Round Robin, Priority
     * Scheduling),
     * the job may be partially executed or fully completed before being returned.
     * <p>
     * If all queues are empty, this method returns an empty {@link Optional},
     * indicating
     * that there are no jobs left to schedule.
     *
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * Optional<Job> next = scheduler.dequeue();
     * next.ifPresent(job -> System.out.println("Executing job: " + job.getPid()));
     * }</pre>
     *
     * @return an {@link Optional} containing the next job to execute, or
     *         {@link Optional#empty()} if no jobs are available in any queue.
     */

    @Override
    public Optional<Job> dequeue() {

        if (priorityLevels.size() == 0 || !this.on) {
            return Optional.empty();
        }

        for (Queue priorityLevel : priorityLevels.values()) {

            if (priorityLevel.isEmpty()) {
                continue;
            } else {
                return priorityLevel.dequeue();
            }

        }

        return Optional.empty();
    }

    /**
     * Enqueues a batch of {@link Job} instances into the {@code MultiLevelQueue},
     * assigning them all the same arrival time.
     * <p>
     * This method is useful for simulating scenarios where multiple jobs enter
     * the system simultaneously (e.g., batch submissions or workload bursts).
     * Each job is inserted into the appropriate internal scheduling queue based
     * on its attributes such as priority or scheduling policy.
     *
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * List<Job> batch = List.of(
     *         new Job(1, 5),
     *         new Job(2, 10),
     *         new Job(3, 8));
     *
     * scheduler.enqueueList(batch, 0); // All jobs arrive at time 0
     * }</pre>
     *
     * @param jobs        the list of {@link Job} instances to enqueue. Must not be
     *                    {@code null}
     *                    or empty.
     * @param arrivalTime the time (in system units or ticks) at which all jobs in
     *                    the list are considered to have arrived.
     * 
     */

    @Override
    public void enqueueList(List<Job> jobs, int arrivalTime) {

        jobs.forEach((n) -> {
            enqueue(n, arrivalTime);
        });

    }

    public void enqueueAtLevel(ArrayList<Job> jobs, int arrivalTime, int priority) {

        Queue priorityLevel = priorityLevels.get(priority);

        if (this.on) {
            priorityLevel.enqueueList(jobs, arrivalTime);

        }
    }

    /**
     * Returns the total number of {@link Job} instances currently waiting
     * across all queues in this {@code MultiLevelQueue}.
     * <p>
     * This count includes jobs from every priority level or scheduling queue
     * managed by the multi-level structure.
     *
     * <h3>Example:</h3>
     * 
     * <pre>{@code
     * int pending = scheduler.size();
     * System.out.println("Jobs remaining: " + pending);
     * }</pre>
     *
     * @return the total number of jobs currently enqueued in all queues.
     */

    @Override
    public int size() {

        if (priorityLevels.size() == 0 || !this.on) {
            return 0;
        }

        int size = 0;
        for (Queue priorityLevel : priorityLevels.values()) {

            size += priorityLevel.size();
        }

        return size;
    }

    /**
     * Activates the {@code MultiLevelQueue} and all of its underlying
     * priority-level queues, preparing them to accept and process jobs.
     * 
     * <p>
     * 
     */

    @Override
    public void open() {
        this.on = true;

        if (priorityLevels.size() == 0 || !this.on) {
            return;
        }

        for (Queue priorityLevel : priorityLevels.values()) {

            priorityLevel.open();
        }

        return;

    }

    /**
     * De-activates the {@code MultiLevelQueue} and all of its underlying
     * priority-level queues, preparing them to accept and process jobs.
     * 
     * <p>
     * 
     */

    @Override
    public void close() {
        this.on = true;

        if (priorityLevels.size() == 0 || !this.on) {
            return;
        }

        for (Queue priorityLevel : priorityLevels.values()) {

            priorityLevel.close();
        }

        return;

    }

    /**
     * Checks whether all priority-level queues in this {@code MultiLevelQueue}
     * are empty.
     * <p>
     *
     * 
     * @return {@code true} if all queues are empty; {@code false} otherwise.
     */
    @Override
    public Boolean isEmpty() {
        for (Queue priorityLevel : priorityLevels.values()) {
            if (priorityLevel.isEmpty()) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }
}
