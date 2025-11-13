package src.interfaces;

import java.util.List;

// Interface for

// Round Robin
// Priority Scheduling
// Shortest Job first
// Multi-level queue

import java.util.Optional;

import src.core.Job;

/**
 * A generic interface representing a scheduling queue within a process
 * scheduling system.
 * <p>
 * Each implementation of {@code Queue} defines its own scheduling policy
 * (e.g., First-Come-First-Served, Round Robin, Priority Scheduling, etc.)
 * for managing and executing {@link Job} instances.
 * <p>
 * 
 * <p>
 * This interface defines the core operations common to all queue types,
 * enabling the construction of more complex schedulers such as
 * {@link MultiLevelQueue}.
 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * Queue queue = new RoundRobin();
 * queue.open();
 *
 * queue.enqueue(new Job(1, 5), 0);
 * queue.enqueue(new Job(2, 10), 2);
 *
 * while (!queue.isEmpty()) {
 *     Optional<Job> next = queue.dequeue();
 *     next.ifPresent(job -> System.out.println("Executing Job " + job.getPid()));
 * }
 *
 * queue.close();
 * }</pre>
 */
public interface Queue {

    /**
     * Inserts a single {@link Job} into the queue with the specified arrival time.
     *
     * @param job         the job to be enqueued; must not be {@code null}.
     * @param arrivalTime the time (in system ticks or units) when the job arrives.
     * 
     */
    void enqueue(Job job, int arrivalTime);

    /**
     * Inserts a list of {@link Job} instances into the queue,
     * assigning the same arrival time to all of them.
     *
     * @param processes    the list of jobs to enqueue; must not be {@code null} or
     *                     empty.
     * @param arrival_time the arrival time (in system ticks or units) for all jobs.
     * 
     */
    void enqueueList(List<Job> processes, int arrival_time);

    /**
     * Performs one step of the scheduling process by retrieving the next
     * {@link Job} to execute according to the queue’s scheduling policy.
     * <p>
     * The returned job may represent a partially executed task
     * (for preemptive policies like Round Robin) or a completed task
     * (for non-preemptive ones like FCFS or SJF).
     *
     * @return an {@link Optional} containing the next job to execute,
     *         or {@link Optional#empty()} if the queue is empty.
     */
    Optional<Job> dequeue();

    /**
     * Opens or activates the queue, allowing jobs to be enqueued and dequeued.
     * <p>
     * This is typically used to initialize any internal state before scheduling
     * begins.
     */
    void open();

    /**
     * Closes or deactivates the queue, preventing further scheduling operations.
     * <p>
     * Implementations may use this to release resources or finalize state
     * once all jobs have been processed.
     */
    void close();

    /**
     * Checks whether the queue currently contains any jobs awaiting execution.
     *
     * @return {@code true} if the queue is empty; {@code false} otherwise.
     */
    Boolean isEmpty();

    /**
     * Returns the number of {@link Job} instances currently stored in the queue.
     *
     * @return the total count of jobs waiting in the queue.
     */
    int size();
}
