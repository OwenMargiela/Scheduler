package src.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import src.core.Job;
import src.core.JobAttribute;
import src.core.Scheduler;
import src.core.TaskLoad;

public class ConcurrentTest {
    public void runConcurrentSchedulerTest(Scheduler scheduler) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<List<Optional<Job>>> batches = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicBoolean done = new AtomicBoolean(false);

        // Producer thread

        TaskLoad[] loads = { TaskLoad.SHORT, TaskLoad.MODERATE, TaskLoad.LONG };
        final int JOBS = 100;
        executorService.execute(() -> {
            try {
                startSignal.await();
                for (int i = 0; i < JOBS; i++) {

                    if (i % 2 == 0) {
                        TaskLoad randomLoad = loads[ThreadLocalRandom.current().nextInt(loads.length)];
                        scheduler.append(scheduler.makeList(randomLoad, 4));

                    }
                    scheduler.push(scheduler.make(TaskLoad.SHORT));
                }
                done.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Consumer thread
        executorService.execute(() -> {
            try {
                startSignal.await();
                while (!scheduler.isEmpty() || !done.get()) {

                    List<Optional<Job>> result = scheduler.run();
                    for (Optional<Job> job : result) {
                        System.out.println(job.get().getAttribute(JobAttribute.BURST_TIME));
                    }

                    batches.add(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        startSignal.countDown();
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);

        assertTrue(finished, "Executor did not finish in time");

        System.out.println("Queue Size " + scheduler.size());

        assertTrue(scheduler.isEmpty(), "Scheduler queue should be empty");
    }

}
