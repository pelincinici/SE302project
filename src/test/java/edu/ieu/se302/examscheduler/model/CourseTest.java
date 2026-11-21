package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class CourseTest {

    @Test
    void keepsCodeAndStudentsInOrder() {
        Course course = new Course("SE302", List.of("20230602001", "20220601002", "20240201003"));

        assertEquals("SE302", course.getCode());
        assertEquals(3, course.getStudentCount());
        assertEquals("20220601002", course.getStudentIds().get(1));
        assertEquals("SE302 (3 students)", course.toString());
    }

    @Test
    void changingTheOriginalListDoesNotChangeTheCourse() {
        List<String> ids = new ArrayList<>(List.of("A1", "A2"));
        Course course = new Course("SE115", ids);

        ids.add("A3");
        ids.set(0, "CHANGED");

        assertEquals(List.of("A1", "A2"), course.getStudentIds());
    }

    @Test
    void studentListCannotBeChangedFromOutside() {
        Course course = new Course("SE115", List.of("A1"));

        assertThrows(UnsupportedOperationException.class, () -> course.getStudentIds().add("A2"));
        assertThrows(UnsupportedOperationException.class, () -> course.getStudentIds().clear());
    }

    @Test
    void courseWithoutStudentsIsAllowed() {
        Course course = new Course("SE499", List.of());

        assertEquals(0, course.getStudentCount());
        assertTrue(course.getStudentIds().isEmpty());
    }
}
