package edu.ieu.se302.examscheduler.model;

/**
 * A classroom where exams can be held.
 * Read from the classroom file, one line per classroom: name;capacity
 */
public class Classroom {

    private final String name;
    private final int capacity;

    public Classroom(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    /** Number of students that can take an exam in this room at the same time. */
    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return name + " (" + capacity + ")";
    }
}