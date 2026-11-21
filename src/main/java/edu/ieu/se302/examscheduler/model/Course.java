package edu.ieu.se302.examscheduler.model;

import java.util.List;

/**
 * A course and the IDs of the students enrolled in it.
 * Read from the course-student (attendance list) file.
 */
public class Course {

    private final String code;
    private final List<String> studentIds;

    public Course(String code, List<String> studentIds) {
        this.code = code;
        this.studentIds = List.copyOf(studentIds);
    }

    public String getCode() {
        return code;
    }

    /** Student IDs in the order they appear in the file. The list cannot be changed. */
    public List<String> getStudentIds() {
        return studentIds;
    }

    public int getStudentCount() {
        return studentIds.size();
    }

    @Override
    public String toString() {
        return code + " (" + studentIds.size() + " students)";
    }
}