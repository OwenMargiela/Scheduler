package src.core;

import java.util.EnumMap;

import java.util.Map;

/**
 * Represents a task to be executed in a in a scheduling system.

 * 
 * 
 * 
 */

public class Job {

    private int pid;
    private int completionTime;
    private boolean completed = false;

    public Job() {

    }

    public Job(int priority) {
        this.setAttribute(JobAttribute.PRIORITY, priority);
    }

    public Job(int priority, int burst_time) {
        this.setAttribute(JobAttribute.PRIORITY, priority);
        this.setAttribute(JobAttribute.BURST_TIME, burst_time);
    }

    // See Job attributes
    private final Map<JobAttribute, Object> attributes = new EnumMap<>(JobAttribute.class);

    public void setAttribute(JobAttribute key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(JobAttribute key) {
        return attributes.get(key);
    }

    public <T> T getAttribute(JobAttribute key, Class<T> type) {
        Object val = attributes.get(key);
        if (val == null)
            return null;
        if (!type.isInstance(val))
            throw new ClassCastException("Expected " + type);
        return type.cast(val);
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public void completed(boolean flag) {
        this.completed = flag;
    }

}
