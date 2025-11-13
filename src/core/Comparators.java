package src.core;

/**
 * A utility class that provides commonly used {@link AttributeComparator}
 * instances for popular CPU scheduling policies.
 * <p>
 * These comparators can be directly passed into {@link OrderedQueue} or
 * {@link MultiLevelQueueBuilder} to configure behavior.
 *
 * <h3>Available Comparators:</h3>
 * <ul>
 * <li>{@link #SJF} — Sorts by {@link JobAttribute#BURST_TIME} (Shortest Job
 * First)</li>
 * <li>{@link #PRIORITY_SCHEDULING} — Sorts by
 * {@link JobAttribute#PRIORITY}</li>
 * <li>{@link #GRANULAR_SCHEDULING} — Sorts by
 * {@link JobAttribute#SCHEDULED_PRIORITY},
 * suitable for fine-grained dynamic priority models</li>
 * </ul>
 *
 * <h3>Example:</h3>
 * 
 * <pre>{@code
 * Queue sjfQueue = new OrderedQueue(Comparators.SJF);
 * }</pre>
 */

public class Comparators {

    // This comparator allows an ordered queue to implement the sjf algorithm
    public static final AttributeComparator SJF = new AttributeComparator(JobAttribute.BURST_TIME);

    // This comparator allows an ordered queue to implement the priority queue
    // algorithm

    // 1 - 3
    public static final AttributeComparator PRIORITY_SCHEDULING = new AttributeComparator(JobAttribute.PRIORITY);

    // - infinity to + infinity
    public static final AttributeComparator GRANULAR_SCHEDULING = new AttributeComparator(
            JobAttribute.SCHEDULED_PRIORITY);

}
