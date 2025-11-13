package src.algorithms;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import src.core.AttributeComparator;
import src.core.Job;
import src.core.JobAttribute;
import src.interfaces.Queue;

/**
 * A thread-safe, programmable scheduling queue that orders {@link Job}
 * instances
 * based on a user-specified {@link AttributeComparator}.
 * <p>
 * This class provides a generic implementation of a priority-based queue that
 * can adapt to different CPU scheduling algorithms such as Shortest Job First
 * (SJF),
 * Priority Scheduling, or custom attribute-driven orderings.
 * <p>

 *
 * <h3>Example Usage:</h3>
 * 
 * <pre>{@code
 * Queue queue = new OrderedQueue(Comparators.SJF);
 * queue.open();
 *
 * Job job1 = new Job(3, 10);
 * Job job2 = new Job(1, 4);
 *
 * queue.enqueue(job1, 0);
 * queue.enqueue(job2, 1);
 *
 * Optional<Job> next = queue.dequeue(); // returns job2 (shorter burst time)
 * }</pre>
 *
 * @see AttributeComparator
 * @see Job
 * @see Comparators
 */

public class OrderedQueue implements Queue {
    private PriorityBlockingQueue<src.core.Job> queue;
    private volatile boolean on;

    final int INITIAL_VAL = 11;

    private final AtomicInteger current_t = new AtomicInteger();

    public OrderedQueue(AttributeComparator cmp) {
        this.queue = new PriorityBlockingQueue<>(this.INITIAL_VAL, cmp);
    }

    @Override
    public void enqueue(Job job, int arrivalTime) {

        if (current_t.get() < arrivalTime)
            current_t.set(arrivalTime);

        if (this.on) {
            this.queue.add(job);

        } else {
            throw new IllegalStateException("Queue is closed");
        }

    }

    @Override
    public Optional<Job> dequeue() {

        Optional<Job> res = Optional.empty();
        if (this.on) {

            try {

                res = Optional.of(this.queue.take());

                res.get().setAttribute(JobAttribute.LAST_EXECUTION_TIME,
                        (int) res.get().getAttribute(JobAttribute.BURST_TIME, Integer.class));

            } catch (InterruptedException e) {

                System.out.println("Exception caught: " + e);
            }
        } else {
            throw new IllegalStateException("Queue is closed");
        }

        res.get().completed(true);
        return res;

    }

    @Override
    public void enqueueList(List<Job> jobs, int arrivalTime) {

        if (current_t.get() < arrivalTime)
            current_t.set(arrivalTime);

        if (on) {

            queue.addAll(jobs);
        } else {
            throw new IllegalStateException("Queue is closed");
        }

    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public void open() {
        this.on = true;

    }

    @Override
    public void close() {
        this.on = false;

    }

    @Override
    public Boolean isEmpty() {
        return this.queue.isEmpty();
    }

}
