package src.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.algorithms.RoundRobin;
import src.core.Job;
import src.core.JobAttribute;
import src.core.Scheduler;
import src.core.TaskLoad;
import src.interfaces.Queue;

public class RoundRobinTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        // Initialize scheduler

        Queue queue = new RoundRobin();
        scheduler = new Scheduler(queue);
        scheduler.open();
    }

    @Test
    public void testRoundRobinExecutionSingleBatch() {
        // Create 4 short jobs
        List<Job> jobsBatchOne = scheduler.makeList(TaskLoad.SHORT, 4);

        // Append jobs to scheduler
        scheduler.append(jobsBatchOne);

        // Run the scheduler
        List<Optional<Job>> results = scheduler.run();

        // Assert that all jobs are completed
        assertEquals(4, results.size(), "Should have 4 results");

        // Check that each job has 0 remaining time
        for (Optional<Job> optJob : results) {
            assertTrue(optJob.isPresent(), "Job should be present");
            Job job = optJob.get();
            Integer remaining = job.getAttribute(JobAttribute.REMAINING_TIME, Integer.class);
            assertEquals(0, remaining, "Job should have 0 remaining time");
            assertTrue(job.getCompletionTime() > 0, "Completion time should be set");

            System.out.printf(
                    " Job PID =%2d | Burst=%2d%n",
                    job.getPid(),
                    job.getAttribute(JobAttribute.BURST_TIME, Integer.class));

        }

        System.out.println("Queue Size " + scheduler.size());

    }

    @Test
    public void testExecutionConcurrentBatch() throws Exception {

        ConcurrentTest test = new ConcurrentTest();
        test.runConcurrentSchedulerTest(scheduler, "rrCpuLoad.csv");

    }

}
