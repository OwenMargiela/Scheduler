package src.core;

import java.util.Comparator;

/**
 * A flexible comparator for comparing {@link Job} objects based on a specified
 * {@link JobAttribute}.
 * <p>
 * Comparisons are primarily performed on the given key attribute. If two jobs
 * have equal values for that key and the key is {@link JobAttribute#PRIORITY},
 * a secondary comparison is performed using
 * {@link JobAttribute#SCHEDULED_PRIORITY} to break ties.
 * <p>
 *
 * 
 * <h3>Example:</h3>
 * 
 * <pre>{@code
 * AttributeComparator cmp = new AttributeComparator(JobAttribute.PRIORITY);
 * OrderedQueue q = new OrderedQueue(cmp);
 * }</pre>
 *
 * @see JobAttribute
 * @see Job
 * @see OrderedQueue
 */

public class AttributeComparator implements Comparator<Job> {

    private final JobAttribute key;
    private final JobAttribute secondaryKey;

    public AttributeComparator(JobAttribute key) {
        this.key = key;

        if (key == JobAttribute.PRIORITY) {
            this.secondaryKey = JobAttribute.SCHEDULED_PRIORITY;
        } else {
            this.secondaryKey = null;
        }
    }

    @Override
    public int compare(Job p1, Job p2) {
        int result = compareValues(p1.getAttribute(key), p2.getAttribute(key));

        if (result == 0 && secondaryKey != null) {
            result = compareValues(p1.getAttribute(secondaryKey), p2.getAttribute(secondaryKey));
        }

        return result;
    }

    private int compareValues(Object a, Object b) {
        if (a == null || b == null)
            return 0;

        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }

        return a.toString().compareTo(b.toString());
    }
}
