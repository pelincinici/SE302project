package edu.ieu.se302.examscheduler.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The exam of one course: when it is and in which classroom(s).
 * <p>
 * A crowded course can use more than one classroom at the same time (FR-14).
 * enteredByUser is true when the user typed the date and time of the exam
 * by hand (FR-18); such exams are fixed and the algorithm does not move them.
 */
public class ExamAssignment {

    private final Course course;
    private final TimeSlot timeSlot;
    private final List<Classroom> classrooms;
    private final boolean enteredByUser;

    public ExamAssignment(Course course, TimeSlot timeSlot, List<Classroom> classrooms, boolean enteredByUser) {
        if (course == null || timeSlot == null) {
            throw new IllegalArgumentException("course and time slot must be given");
        }
        if (classrooms == null || classrooms.isEmpty()) {
            throw new IllegalArgumentException("exam of " + course.getCode() + " has no classroom");
        }
        Set<String> names = new HashSet<>();
        for (Classroom room : classrooms) {
            if (!names.add(room.getName())) {
                throw new IllegalArgumentException("classroom " + room.getName() + " is given twice");
            }
        }
        this.course = course;
        this.timeSlot = timeSlot;
        this.classrooms = List.copyOf(classrooms);
        this.enteredByUser = enteredByUser;
    }

    public Course getCourse() {
        return course;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    /** Classrooms of this exam. The list cannot be changed. */
    public List<Classroom> getClassrooms() {
        return classrooms;
    }

    public boolean isEnteredByUser() {
        return enteredByUser;
    }

    /** True if the exam is held in more than one classroom. */
    public boolean isSplit() {
        return classrooms.size() > 1;
    }

    public boolean usesClassroom(String classroomName) {
        return classrooms.stream().anyMatch(room -> room.getName().equals(classroomName));
    }

    /** Total number of seats in all classrooms of this exam. */
    public int getTotalCapacity() {
        return classrooms.stream().mapToInt(Classroom::getCapacity).sum();
    }

    /** True if every student of the course has a seat. */
    public boolean hasEnoughSeats() {
        return getTotalCapacity() >= course.getStudentCount();
    }

    /** For example "SE302, M302 + M303, 9 November 2026 (Monday), 09:00–11:00". */
    @Override
    public String toString() {
        String rooms = classrooms.stream().map(Classroom::getName).collect(Collectors.joining(" + "));
        return course.getCode() + ", " + rooms + ", " + timeSlot;
    }
}
