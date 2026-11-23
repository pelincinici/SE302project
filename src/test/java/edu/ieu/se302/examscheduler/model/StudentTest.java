package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StudentTest {

    @Test
    void keepsIdAndCourses() {
        Student student = new Student("20230602001");
        student.addCourse("SE302");
        student.addCourse("MATH153");

        assertEquals("20230602001", student.getId());
        assertEquals(List.of("SE302", "MATH153"), student.getCourseCodes());
        assertTrue(student.takes("SE302"));
        assertFalse(student.takes("SE115"));
    }

    @Test
    void sameCourseIsAddedOnce() {
        Student student = new Student("A1");
        student.addCourse("SE302");
        student.addCourse("SE302");

        assertEquals(1, student.getCourseCount());
    }

    @Test
    void courseListCannotBeChangedFromOutside() {
        Student student = new Student("A1");

        assertThrows(UnsupportedOperationException.class, () -> student.getCourseCodes().add("SE302"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void emptyIdIsNotAllowed(String id) {
        assertThrows(IllegalArgumentException.class, () -> new Student(id));
    }

    @Test
    void studentsWithSameIdAreEqual() {
        assertEquals(new Student("A1"), new Student("A1"));
        assertEquals(new Student("A1").hashCode(), new Student("A1").hashCode());
        assertNotEquals(new Student("A1"), new Student("A2"));
    }

    @Test
    void buildsStudentsFromCourses() {
        List<Course> courses = List.of(
                new Course("SE302", List.of("A1", "A2", "A3")),
                new Course("MATH153", List.of("A2", "A4")),
                new Course("SE115", List.of("A2", "A3")),
                new Course("SE499", List.of()));

        List<Student> students = Student.fromCourses(courses);

        assertEquals(4, students.size());
        assertEquals("A1", students.getFirst().getId());
        Student a2 = students.get(1);
        assertEquals(List.of("SE302", "MATH153", "SE115"), a2.getCourseCodes());
        assertEquals(List.of("MATH153"), students.get(3).getCourseCodes());
    }
}
