package src.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskGenerator {
    public Job make(TaskLoad taskLoad, Integer pid) {

        int burstTime = taskLoad.randomBurstTime();
        int priority = determinePriority(taskLoad);

        Job job = new Job();

        job.setAttribute(JobAttribute.BURST_TIME, burstTime);
        job.setAttribute(JobAttribute.PRIORITY, priority);
        job.setAttribute(JobAttribute.SCHEDULED_PRIORITY, (int) (Math.random() * Short.MAX_VALUE + 1));
        job.setAttribute(JobAttribute.REMAINING_TIME, burstTime);
        job.setAttribute(JobAttribute.ARRIVAL_TIME, 0);
        job.setPid(pid);

        return job;
    }

    public List<Job> makeList(TaskLoad taskLoad, int size, AtomicInteger global) {

        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int pid = global.getAndIncrement();

            int burstTime = taskLoad.randomBurstTime();
            int priority = determinePriority(taskLoad);

            Job job = new Job();

            job.setAttribute(JobAttribute.BURST_TIME, burstTime);
            job.setAttribute(JobAttribute.PRIORITY, priority);
            job.setAttribute(JobAttribute.REMAINING_TIME, burstTime);
            job.setAttribute(JobAttribute.SCHEDULED_PRIORITY, (int) (Math.random() * Short.MAX_VALUE + 1));
            job.setAttribute(JobAttribute.ARRIVAL_TIME, 0);
            job.setPid(pid);

            jobs.add(job);
        }

        return jobs;

    }

    private int determinePriority(TaskLoad taskLoad) {
        return switch (taskLoad) {
            case LONG -> 1; // Lowest priority
            case MODERATE -> 2;
            case SHORT -> 3;
        };
    }

}
