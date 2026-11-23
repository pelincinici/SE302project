package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class TimeSlotTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 9);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 11, 10);

    private static TimeSlot slot(LocalDate date, int day, int slot, String start, String end) {
        return new TimeSlot(date, day, slot, LocalTime.parse(start), LocalTime.parse(end));
    }

    @Test
    void showsRealDateAndTime() {
        TimeSlot slot = slot(MONDAY, 0, 0, "09:00", "11:00");

        assertEquals("9 November 2026 (Monday), 09:00–11:00", slot.toString());
    }

    @Test
    void slotsInARowOnTheSameDayAreNextToEachOther() {
        TimeSlot first = slot(MONDAY, 0, 0, "09:00", "11:00");
        TimeSlot second = slot(MONDAY, 0, 1, "11:30", "13:30");
        TimeSlot fourth = slot(MONDAY, 0, 3, "16:30", "18:30");

        assertTrue(second.isRightAfter(first));
        assertFalse(first.isRightAfter(second));
        assertTrue(first.isNextTo(second));
        assertTrue(second.isNextTo(first));
        assertFalse(first.isNextTo(fourth));
    }

    @Test
    void lastSlotOfADayAndFirstSlotOfNextDayAreNotNextToEachOther() {
        TimeSlot mondayLast = slot(MONDAY, 0, 3, "16:30", "18:30");
        TimeSlot tuesdayFirst = slot(TUESDAY, 1, 0, "09:00", "11:00");

        assertFalse(tuesdayFirst.isRightAfter(mondayLast));
        assertFalse(mondayLast.isNextTo(tuesdayFirst));
    }

    @Test
    void overlapsOnlyOnTheSameDateAndTime() {
        TimeSlot normal = slot(MONDAY, 0, 0, "09:00", "11:00");
        TimeSlot longExam = slot(MONDAY, 0, 0, "10:00", "13:00");
        TimeSlot later = slot(MONDAY, 0, 1, "11:00", "13:00");
        TimeSlot otherDay = slot(TUESDAY, 1, 0, "09:00", "11:00");

        assertTrue(normal.overlaps(longExam));
        assertTrue(longExam.overlaps(later));
        assertFalse(normal.overlaps(later), "11:00 end and 11:00 start do not overlap");
        assertFalse(normal.overlaps(otherDay));
    }

    @Test
    void endMustBeAfterStart() {
        assertThrows(IllegalArgumentException.class, () -> slot(MONDAY, 0, 0, "11:00", "11:00"));
        assertThrows(IllegalArgumentException.class, () -> slot(MONDAY, 0, 0, "11:00", "09:00"));
    }

    @Test
    void negativeNumbersAreNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> slot(MONDAY, -1, 0, "09:00", "11:00"));
        assertThrows(IllegalArgumentException.class, () -> slot(MONDAY, 0, -1, "09:00", "11:00"));
    }

    @Test
    void sameDateAndTimesAreEqual() {
        assertEquals(slot(MONDAY, 0, 0, "09:00", "11:00"), slot(MONDAY, 0, 0, "09:00", "11:00"));
        assertEquals(slot(MONDAY, 0, 0, "09:00", "11:00").hashCode(), slot(MONDAY, 0, 0, "09:00", "11:00").hashCode());
    }
}
