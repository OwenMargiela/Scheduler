package src.core;

import java.util.ArrayList;

import java.util.List;

import src.algorithms.MultiLevelQueue;

import src.interfaces.Queue;

/**
 * A builder class for constructing instances of {@link MultiLevelQueue}
 * with customizable scheduling policies.
 * <p>
 *
 *
 * 
 * 
 * <p>
 * This class follows the Builder design pattern, allowing easy and readable
 * configuration of complex scheduler hierarchies.
 */
public class MultiLevelQueueBuilder {

    /** Internal collection of scheduling policies defining the queue hierarchy. */
    private List<Policies> set = new ArrayList<>();

    /**
     * Adds a scheduling {@link Policies} instance to the builder configuration.
     * <p>
     * Each added policy corresponds to a priority level in the resulting
     * {@link MultiLevelQueue}. The first added policy has the highest priority,
     * and subsequent policies have progressively lower priority.
     *
     * @param policy the scheduling policy to include in the multi-level queue; must
     *               not be {@code null}.
     * @return this {@code MultiLevelQueueBuilder} instance, allowing method
     *         chaining.
     * @throws IllegalArgumentException if {@code policy} is {@code null}.
     */
    public MultiLevelQueueBuilder addPolicy(Policies policy) {
        set.add(policy);
        return this;
    }

    /**
     * Builds and returns a new {@link MultiLevelQueue} using the configured
     * list of scheduling policies.
     * <p>
     * The resulting queue hierarchy reflects the order in which policies
     * were added via {@link #addPolicy(Policies)}.
     *
     * @return a fully configured {@link MultiLevelQueue} instance.
     * @throws IllegalStateException if no policies have been added prior to
     *                               building.
     */
    public Queue build() {
        if (set.isEmpty()) {
            throw new IllegalStateException("Cannot build MultiLevelQueue without any policies.");
        }
        return new MultiLevelQueue(set.toArray(new Policies[set.size()]));
    }
}
