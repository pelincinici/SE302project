package edu.ieu.se302.examscheduler.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A student, known by the student ID (for example "Std_ID_001" or "20230602001").
 * There is no separate student file: students are found in the course-student file,
 * so the list of students is built from the courses with {@link #fromCourses(List)}.
 */
public class Student {

    private final String id;
    private final List<String> courseCodes = new ArrayList<>();

    public Student(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("student ID is empty");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /** Adds a course to this student. Adding the same course again does nothing. */
    public void addCourse(String courseCode) {
        if (!courseCodes.contains(courseCode)) {
            courseCodes.add(courseCode);
        }
    }

    /** Codes of the courses this student takes. The list cannot be changed from outside. */
    public List<String> getCourseCodes() {
        return Collections.unmodifiableList(courseCodes);
    }

    public int getCourseCount() {
        return courseCodes.size();
    }

    public boolean takes(String courseCode) {
        return courseCodes.contains(courseCode);
    }

    /**
     * Makes one Student for every student ID in the courses and gives each student
     * the courses they are enrolled in. Students are in the order they first appear.
     */
    public static List<Student> fromCourses(List<Course> courses) {
        Map<String, Student> students = new LinkedHashMap<>();
        for (Course course : courses) {
            for (String studentId : course.getStudentIds()) {
                students.computeIfAbsent(studentId, Student::new).addCourse(course.getCode());
            }
        }
        return new ArrayList<>(students.values());
    }

    /** Two students are the same if they have the same ID. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Student student && id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
