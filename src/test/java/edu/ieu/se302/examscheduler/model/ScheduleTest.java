package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.ExamPeriod.SlotHours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleTest {

    private static final Classroom M302 = new Classroom("M302", 45);
    private static final Classroom M303 = new Classroom("M303", 45);
    private static final Classroom C104 = new Classroom("C104", 120);

    private static final Course SE302 = new Course("SE302", List.of("A1", "A2", "A3"));
    private static final Course SE115 = new Course("SE115", List.of("A2", "A4"));
    private static final Course MATH153 = new Course("MATH153", List.of("A1", "A4", "A5"));
    private static final Course CE215 = new Course("CE215", List.of("A5"));

    private ExamPeriod period;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        period = new ExamPeriod(LocalDate.of(2026, 11, 9), 3, List.of(
                new SlotHours(LocalTime.of(9, 0), LocalTime.of(11, 0)),
                new SlotHours(LocalTime.of(13, 0), LocalTime.of(15, 0))));
        schedule = new Schedule(period);
    }

    private ExamAssignment exam(Course course, int day, int slot, Classroom... rooms) {
        return new ExamAssignment(course, period.getTimeSlot(day, slot), List.of(rooms), false);
    }

    @Test
    void newScheduleIsEmpty() {
        assertEquals(0, schedule.size());
        assertTrue(schedule.getAssignments().isEmpty());
        assertEquals(period, schedule.getPeriod());
    }

    @Test
    void examsAreSortedByDateAndTime() {
        schedule.add(exam(SE115, 1, 0, M302));
        schedule.add(exam(SE302, 0, 1, M302));
        schedule.add(exam(MATH153, 0, 0, C104));

        List<String> order = schedule.getAssignments().stream().map(a -> a.getCourse().getCode()).toList();

        assertEquals(List.of("MATH153", "SE302", "SE115"), order);
    }

    @Test
    void courseView() {
        schedule.add(exam(SE302, 0, 0, M302));

        assertTrue(schedule.findByCourse("SE302").isPresent());
        assertFalse(schedule.findByCourse("SE115").isPresent());
    }

    @Test
    void classroomView() {
        schedule.add(exam(SE302, 0, 0, M302));
        schedule.add(exam(SE115, 0, 1, M302, M303));
        schedule.add(exam(MATH153, 1, 0, C104));

        assertEquals(2, schedule.getByClassroom("M302").size());
        assertEquals(1, schedule.getByClassroom("M303").size());
        assertTrue(schedule.getByClassroom("E101").isEmpty());
    }

    @Test
    void studentView() {
        schedule.add(exam(SE302, 0, 0, M302));
        schedule.add(exam(SE115, 1, 0, M302));
        schedule.add(exam(MATH153, 2, 1, C104));

        List<String> a1 = schedule.getByStudent("A1").stream().map(a -> a.getCourse().getCode()).toList();
        List<String> a4 = schedule.getByStudent("A4").stream().map(a -> a.getCourse().getCode()).toList();

        assertEquals(List.of("SE302", "MATH153"), a1);
        assertEquals(List.of("SE115", "MATH153"), a4);
        assertTrue(schedule.getByStudent("NOBODY").isEmpty());
    }

    @Test
    void dayView() {
        schedule.add(exam(SE302, 0, 1, M302));
        schedule.add(exam(SE115, 0, 0, M303));
        schedule.add(exam(MATH153, 2, 0, C104));

        List<ExamAssignment> monday = schedule.getByDate(LocalDate.of(2026, 11, 9));

        assertEquals(2, monday.size());
        assertEquals("SE115", monday.getFirst().getCourse().getCode());
        assertTrue(schedule.getByDate(LocalDate.of(2026, 11, 10)).isEmpty());
    }

    @Test
    void twoExamsInDifferentRoomsAtTheSameTimeAreAllowed() {
        schedule.add(exam(SE302, 0, 0, M302));
        schedule.add(exam(SE115, 0, 0, M303));

        assertEquals(2, schedule.size());
    }

    @Test
    void sameClassroomAtTheSameTimeIsNotAllowed() {
        schedule.add(exam(SE302, 0, 0, M302));

        assertThrows(IllegalArgumentException.class, () -> schedule.add(exam(SE115, 0, 0, M303, M302)));
        assertEquals(1, schedule.size());
    }

    @Test
    void examEnteredByUserThatOverlapsARoomIsNotAllowed() {
        schedule.add(exam(SE302, 0, 0, M302));
        TimeSlot longExam = new TimeSlot(LocalDate.of(2026, 11, 9), 0, 0, LocalTime.of(10, 0), LocalTime.of(13, 0));

        assertThrows(IllegalArgumentException.class,
                () -> schedule.add(new ExamAssignment(CE215, longExam, List.of(M302), true)));
    }

    @Test
    void sameCourseTwiceIsNotAllowed() {
        schedule.add(exam(SE302, 0, 0, M302));

        assertThrows(IllegalArgumentException.class, () -> schedule.add(exam(SE302, 1, 0, M303)));
    }

    @Test
    void removeAndEnteredByUser() {
        schedule.add(exam(SE302, 0, 0, M302));
        schedule.add(new ExamAssignment(CE215, period.getTimeSlot(1, 1), List.of(M303), true));

        assertEquals(List.of("CE215"),
                schedule.getEnteredByUser().stream().map(a -> a.getCourse().getCode()).toList());
        assertTrue(schedule.remove("SE302"));
        assertFalse(schedule.remove("SE302"));
        assertEquals(1, schedule.size());
    }
}
