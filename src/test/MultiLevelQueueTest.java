package src.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import src.core.Policies;
import src.core.Scheduler;
import src.core.TaskLoad;

import src.core.Job;
import src.core.JobAttribute;

public class MultiLevelQueueTest {

    private Scheduler scheduler;

    @BeforeEach
    void setup() {

        scheduler = new Scheduler(Policies.SJF,
                Policies.ROUND_ROBIN,
                Policies.SJF);

        scheduler.open();

    }

    @Test
    public void testMLQExecutionSingleBatch() {

        List<List<Job>> batches = new ArrayList<>();
        batches.add(scheduler.makeList(TaskLoad.SHORT, 4));
        batches.add(scheduler.makeList(TaskLoad.MODERATE, 6));
        batches.add(scheduler.makeList(TaskLoad.LONG, 1));

        scheduler.append(batches.get(0));
        scheduler.append(batches.get(1));
        scheduler.append(batches.get(2));

        List<Optional<Job>> list = scheduler.run(4);

        list.forEach((l) -> {
            int time = (int) l.get().getAttribute(JobAttribute.BURST_TIME,
                    Integer.class);
            System.out.println("Burst Time" + " " + time + "\n");
        });

        scheduler.run(6);

        list.forEach((l) -> {
            int LastExecutionTime = (int) l.get().getAttribute(JobAttribute.LAST_EXECUTION_TIME,
                    Integer.class);

            int pid = (int) l.get().getPid();
            System.out.println("PID" + pid + " Last ExecutionTime" + " " +
                    LastExecutionTime + "\n");
        });

        scheduler.run(2);

        list.forEach((l) -> {

            System.out.println("Priority: " + l
                    .get()
                    .getAttribute(JobAttribute.PRIORITY,
                            Integer.class));

        });

    }

    @Test
    public void testExecutionConcurrentBatch() throws Exception {

        ConcurrentTest test = new ConcurrentTest();
        test.runConcurrentSchedulerTest(scheduler, "mlqCpuLoad.csv");

    }

}
