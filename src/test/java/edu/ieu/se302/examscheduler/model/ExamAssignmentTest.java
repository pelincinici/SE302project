package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExamAssignmentTest {

    private static final TimeSlot MONDAY_MORNING =
            new TimeSlot(LocalDate.of(2026, 11, 9), 0, 0, LocalTime.of(9, 0), LocalTime.of(11, 0));
    private static final Classroom M302 = new Classroom("M302", 45);
    private static final Classroom M303 = new Classroom("M303", 45);
    private static final Classroom C104 = new Classroom("C104", 120);

    private static Course courseWith(String code, int students) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= students; i++) {
            ids.add("S" + i);
        }
        return new Course(code, ids);
    }

    @Test
    void examInOneClassroom() {
        ExamAssignment exam = new ExamAssignment(courseWith("SE302", 40), MONDAY_MORNING, List.of(M302), false);

        assertFalse(exam.isSplit());
        assertFalse(exam.isEnteredByUser());
        assertEquals(45, exam.getTotalCapacity());
        assertTrue(exam.hasEnoughSeats());
        assertEquals("SE302, M302, 9 November 2026 (Monday), 09:00–11:00", exam.toString());
    }

    @Test
    void crowdedExamSplitOverClassrooms() {
        ExamAssignment exam = new ExamAssignment(courseWith("SE115", 85), MONDAY_MORNING, List.of(M302, M303), false);

        assertTrue(exam.isSplit());
        assertEquals(90, exam.getTotalCapacity());
        assertTrue(exam.hasEnoughSeats());
        assertTrue(exam.usesClassroom("M303"));
        assertFalse(exam.usesClassroom("C104"));
        assertEquals("SE115, M302 + M303, 9 November 2026 (Monday), 09:00–11:00", exam.toString());
    }

    @Test
    void notEnoughSeatsIsShown() {
        ExamAssignment exam = new ExamAssignment(courseWith("MATH153", 240), MONDAY_MORNING, List.of(C104), false);

        assertFalse(exam.hasEnoughSeats());
    }

    @Test
    void examEnteredByUserIsMarked() {
        TimeSlot longExam = new TimeSlot(LocalDate.of(2026, 11, 9), 0, 0, LocalTime.of(9, 30), LocalTime.of(12, 30));
        ExamAssignment exam = new ExamAssignment(courseWith("CE215", 30), longExam, List.of(M302), true);

        assertTrue(exam.isEnteredByUser());
        assertEquals(LocalTime.of(12, 30), exam.getTimeSlot().getEnd());
    }

    @Test
    void classroomListCannotBeChanged() {
        ExamAssignment exam = new ExamAssignment(courseWith("SE302", 10), MONDAY_MORNING, List.of(M302), false);

        assertThrows(UnsupportedOperationException.class, () -> exam.getClassrooms().add(M303));
    }

    @Test
    void wrongValuesAreNotAllowed() {
        Course course = courseWith("SE302", 10);

        assertThrows(IllegalArgumentException.class, () -> new ExamAssignment(course, MONDAY_MORNING, List.of(), false));
        assertThrows(IllegalArgumentException.class, () -> new ExamAssignment(course, null, List.of(M302), false));
        assertThrows(IllegalArgumentException.class, () -> new ExamAssignment(null, MONDAY_MORNING, List.of(M302), false));
        assertThrows(IllegalArgumentException.class,
                () -> new ExamAssignment(course, MONDAY_MORNING, List.of(M302, M302), false));
    }
}
