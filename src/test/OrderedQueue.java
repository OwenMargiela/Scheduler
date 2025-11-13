package src.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.core.Job;
import src.core.JobAttribute;
import src.core.Policies;
import src.core.Scheduler;
import src.core.TaskLoad;

public class OrderedQueue {

    private Scheduler sjf;
    private Scheduler priorityQueue;

    @BeforeEach
    void setUp() {
        // Initialize sjf
        // Queue queue = new OrderedQueue(Comparators.SJF);
        sjf = new Scheduler(Policies.SJF);
        sjf.open();

        priorityQueue = new Scheduler(Policies.PRIORITY_SCHEDULING);
        priorityQueue.open();
    }

    @Test
    public void testSJFExecutionSingleBatch() {
        int jobs = 5;

        List<Job> jobsBatchTwo = sjf.makeList(TaskLoad.MODERATE, jobs);

        sjf.append(jobsBatchTwo);

        List<Job> jobsBatchOne = sjf.makeList(TaskLoad.SHORT, jobs);

        // Append jobs to sjf
        sjf.append(jobsBatchOne);

        // Run the sjf
        List<Optional<Job>> results = sjf.run();

        for (int i = 1; i < results.size() - 1; i++) {

            int cur = (int) results.get(i).get().getAttribute(JobAttribute.BURST_TIME, Integer.class);
            int prev = (int) results.get(i - 1).get().getAttribute(JobAttribute.BURST_TIME, Integer.class);
            assertTrue(cur >= prev, "Current job should have a shorted burst time than the last");

        }

        for (Optional<Job> job : results) {
            System.out.println(job
                    .get()
                    .getAttribute(JobAttribute.BURST_TIME,
                            Integer.class));

        }

    }

    @Test
    public void testPriorityExecutionSingleBatch() {

        Job jobTwo = priorityQueue.make(TaskLoad.MODERATE);

        priorityQueue.push(jobTwo);

        Job jobThree = priorityQueue.make(TaskLoad.LONG);

        priorityQueue.push(jobThree);

        Job jobOne = priorityQueue.make(TaskLoad.SHORT);

        priorityQueue.push(jobOne);

        // Run the priorityQueue
        List<Optional<Job>> results = priorityQueue.run();

        for (int i = 1; i < results.size() - 1; i++) {

            int cur = (int) results.get(i).get().getAttribute(JobAttribute.PRIORITY, Integer.class);
            int prev = (int) results.get(i - 1).get().getAttribute(JobAttribute.PRIORITY, Integer.class);
            assertTrue(cur >= prev, "Current job should have a shorted burst time than the last");

        }

        for (Optional<Job> job : results) {
            System.out.println(job
                    .get()
                    .getAttribute(JobAttribute.PRIORITY,
                            Integer.class));

        }

    }

    @Test
    public void testExecutionSjfConcurrentBatch() throws Exception {

        ConcurrentTest test = new ConcurrentTest();
        test.runConcurrentSchedulerTest(sjf, "sjfCpuLoad.csv");

    }

    @Test
    public void testExecutionPQConcurrentBatch() throws Exception {

        ConcurrentTest test = new ConcurrentTest();
        test.runConcurrentSchedulerTest(priorityQueue, "priorityQueueCpuLoad.csv");

    }

}
