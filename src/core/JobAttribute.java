package src.core;

/**
 * Represents a set of extensible attributes that can be associated with a
 * {@link Job}
 * in a scheduling system.
 * <p>
 * These attributes allow flexible and generic handling of job metadata without
 * tightly coupling the
 * {@link Job} class to a fixed set of fields.
 * <p>
 * 
 * 
 * 
 */
public enum JobAttribute {

    /** Total CPU time required by the job. */
    BURST_TIME("burstTime"),

    /** Static or user-defined priority assigned to the job. */
    PRIORITY("priority"),

    /** Dynamic or adjusted priority used by scheduling algorithms. */
    SCHEDULED_PRIORITY("scheduledPriority"),

    /** Time (in ticks or system units) when the job arrived in the system. */
    ARRIVAL_TIME("arrivalTime"),

    /** Time (in ticks) when the job first started executing (response/start time). */
    START_TIME("startTime"),

    /** Remaining CPU time for the job to complete execution. */
    REMAINING_TIME("remainingTime"),

    /**
     * The last time the job was executed, used for aging or response-time tracking.
     */
    LAST_EXECUTION_TIME("lastExecutionTime"),

    /** The time (in ticks) when the job completed. */
    COMPLETION_TIME("completionTime");

    private final String key;

    JobAttribute(String key) {
        this.key = key;
    }

    /**
     * Returns the string key associated with this job attribute.
     *
     * @return the string identifier for this attribute.
     */
    public String key() {
        return key;
    }

    /**
     * Returns the string representation of this job attribute,
     * which is equivalent to its key.
     *
     * @return a human-readable key for this attribute.
     */
    @Override
    public String toString() {
        return key;
    }
}
