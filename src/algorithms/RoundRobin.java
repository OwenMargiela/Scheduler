package src.algorithms;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;


import src.core.Job;
import src.core.JobAttribute;
import src.interfaces.Queue;

public class RoundRobin implements Queue {

    private ConcurrentLinkedQueue<src.core.Job> queue;
    private volatile boolean on;

    private int TIME_SLICE = 3;

    private volatile boolean readyToRun = false;

    public RoundRobin() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void enqueue(Job job, int arrivalTime) {

        if (this.on) {
            this.queue.add(job);

        } else {
            throw new IllegalStateException("Queue is closed");
        }

    }

    public Optional<Job> dequeue() {

        Job job = queue.poll();
        if (this.on) {

            if (job == null)
                return Optional.empty();

            int remaining = job.getAttribute(JobAttribute.REMAINING_TIME, Integer.class);

            int executionTime = Math.min(TIME_SLICE, remaining);
            job.setAttribute(JobAttribute.LAST_EXECUTION_TIME, executionTime);

            remaining -= executionTime;

            // Decrement remaining time
            job.setAttribute(JobAttribute.REMAINING_TIME, remaining);

            if (remaining > 0) {
                queue.add(job); // not finished, back to queue

            } else {

                job.completed(true);

                return Optional.ofNullable(job);
            }

        } else {
            throw new IllegalStateException("Queue is closed");
        }

        return Optional.ofNullable(job);

    }

    @Override
    public void enqueueList(List<Job> jobs, int arrivalTime) {

        if (this.on) {
            this.queue.addAll(jobs);

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

    public void setQuanta(int quantum) {
        this.TIME_SLICE = quantum;
    }

    public void setReady(boolean ready) {
        this.readyToRun = ready;
    }

    public boolean isReady() {
        return this.readyToRun;
    }
}
