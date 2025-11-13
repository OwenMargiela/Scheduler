package src.core;

import src.algorithms.MultiLevelQueue;
import src.interfaces.Queue;

/**
 * Defines the available scheduling policies that can be used to configure
 * a {@link Queue} or a {@link MultiLevelQueue}.
 * <p>
 * Each {@code Policies} constant represents a distinct scheduling algorithm
 * with its own job selection criteria and execution behavior.
 *
 * 
 * @see MultiLevelQueue
 * @see MultiLevelQueueBuilder
 * @see Queue
 */
public enum Policies {

    /** Shortest Job First — selects the job with the smallest burst time next. */
    SJF("Shortest Job First"),

    /** Round Robin — executes jobs in cyclic order using fixed time slices. */
    ROUND_ROBIN("Round Robin"),

    /** Priority Scheduling — selects jobs based on priority levels. */
    PRIORITY_SCHEDULING("Priority Scheduling");

    private final String policy;

    Policies(String policy) {

        this.policy = policy;
    }

    /**
     * Returns the human-readable name of this scheduling policy.
     *
     * @return the descriptive name of the policy.
     */
    public String get() {
        return policy;
    }

    /**
     * Returns the string representation of this policy, which is equivalent
     * to its descriptive name.
     *
     * @return a string description of the policy.
     */
    @Override
    public String toString() {
        return policy;
    }
}
