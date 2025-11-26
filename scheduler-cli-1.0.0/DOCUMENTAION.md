# Used Packages

## Package java.util.concurrent

Utility classes commonly useful in concurrent programming.

### java.util.concurrent.PriorityBlockingQueue

A concurrent blocking queue  implementation of the priority queue structure. The elements of the priority queue are ordered according to their natural ordering, or by a Comparator provided at queue construction time, depending on which constructor is used. Because of its ordered nature, they were used to implement scheduling algorithms that schedule jobs based on the ordering of specific attributes such as priority queues and SJF.

## Comparator Implementation
```java
public class Comparators {
    // This comparator allows an ordered queue to implement the sjf algorithm
    public static final AttributeComparator SJF = new AttributeComparator(JobAttribute.BURST_TIME);
    
    // This comparator allows an ordered queue to implement the priority queue algorithm
    
    // 1 - 3
    public static final AttributeComparator PRIORITY_SCHEDULING = new AttributeComparator(JobAttribute.PRIORITY);
    
    // -infinity to +infinity
    public static final AttributeComparator GRANULAR_SCHEDULING = new AttributeComparator(JobAttribute.SCHEDULED_PRIORITY);
}
```

### AttributeComparator

The attribute comparator is a flexible comparator for comparing `Job` objects based on a specified `JobAttribute`. They are implementation of the provided `Comparator` class.

Comparisons are primarily performed on the given key attribute. If two jobs have equal values for that key and the key is `JobAttribute.PRIORITY`, a secondary comparison is performed using `JobAttribute.SCHEDULED_PRIORITY` to break ties.

**Example:**
```java
AttributeComparator cmp = new AttributeComparator(JobAttribute.PRIORITY);
OrderedQueue q = new OrderedQueue(cmp);
```



## Class ConcurrentLinkedQueue

A non-ordered, thread-safe queue based on linked nodes. This structure supports both append to end and append to front operations, making it perfect for the round robin scheduling policies.

Multiple classes from Java's concurrent library were utilized to create a concurrent scheduler class that can be easily shared and sent between threads without the threat of race conditions, lock contentions and other anomalies and grievances that may arise during multi-threading.

## java.util.concurrent.atomic

Provides a collection of atomic operations used to create non-blocking algorithms for concurrent environments using low-level atomic machine instructions such as compare-and-swap.

When multiple threads attempt to update the same value through CAS, one of them wins and updates the value. However, unlike in the case of locks, no other thread gets suspended.

**Code Example:**
```java
// A counter that continually creates new pids
AtomicInteger counter;
process.setPid(this.counter.getAndIncrement());
```

Multiple threads may own the scheduler class, therefore multiple threads can create processes. However, these processes need their own unique process ID. Using the compare-and-swap mechanism, only one thread will be able to increment this counter at a time. The other thread is simply informed that they did not manage to update the value. Of course, in the event that these CAS operations didn't work, the thread can try again until it succeeds. The threads can then proceed to do further work and context switches are completely avoided.

## ConcurrentHashMap

A hash table supporting full concurrency of retrievals and high expected concurrency for updates. The Multi-level queue employs the use of a hashmap that maps a priority level to a specific queue implementation. This allows many threads to access different priority levels of the Multi-level queue.


**Code Example:**
```java
  ConcurrentHashMap<Integer, Queue> priorityLevels = new ConcurrentHashMap<>();

    priorityLevels.put(1, RoundRobin()); // level one
    priorityLevels.put(2, new OrderedQueue(Comparators.SJF) ); // level two
    priorityLevels.put(3, new OrderedQueue(Comparators.PRIORITY_SCHEDULING)); // level three

```


`Comparators` is a helper enum used to quickly defined the attribute by which a job is to be ordered.