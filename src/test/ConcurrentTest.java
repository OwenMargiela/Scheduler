package src.test;

import com.sun.management.OperatingSystemMXBean;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.lang.management.ManagementFactory;
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
import src.core.Scheduler;
import src.core.TaskLoad;

public class ConcurrentTest {
    public void runConcurrentSchedulerTest(Scheduler scheduler, String policyFile) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<List<Optional<Job>>> batches = Collections.synchronizedList(new ArrayList<>());

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicBoolean done = new AtomicBoolean(false);

        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Csv File
        CSVWriter writer = new CSVWriter(new FileWriter(policyFile, true));

        Thread cpuMonitor = new Thread(() -> {

            try {
                while (!done.get() || !executorService.isTerminated()) {
                    Double systemLoad = osBean.getCpuLoad() * 100;
                    Double processLoad = osBean.getProcessCpuLoad() * 100;

                    System.out.printf("[CPU] System: %.2f%% | JVM: %.2f%%%n", systemLoad, processLoad);
                    writer.writeNext(new String[] { systemLoad.toString(), processLoad.toString() });
                    writer.flush();
                    Thread.sleep(50);
                }

                writer.close();

            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        // Producer thread

        TaskLoad[] loads = { TaskLoad.SHORT, TaskLoad.MODERATE, TaskLoad.LONG };
        final int JOBS = 20000;
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

                    batches.add(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        cpuMonitor.start();
        startSignal.countDown();
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);
        cpuMonitor.join();

        assertTrue(finished, "Executor did not finish in time");

        System.out.println("Queue Size " + scheduler.size());
        assertTrue(scheduler.isEmpty(), "Scheduler queue should be empty");
    }

}
