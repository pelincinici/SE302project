package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.ExamPeriod.SlotHours;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExamPeriodTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 9);   // Monday

    private static SlotHours hours(String start, String end) {
        return new SlotHours(LocalTime.parse(start), LocalTime.parse(end));
    }

    /** 5 days, 4 slots a day, like the example in the project video. */
    private static ExamPeriod fiveDaysFourSlots() {
        return new ExamPeriod(START, 5, List.of(
                hours("09:00", "11:00"), hours("11:30", "13:30"),
                hours("14:00", "16:00"), hours("16:30", "18:30")));
    }

    @Test
    void countsDaysAndSlots() {
        ExamPeriod period = fiveDaysFourSlots();

        assertEquals(5, period.getNumberOfDays());
        assertEquals(4, period.getSlotsPerDay());
        assertEquals(20, period.getTotalSlots());
        assertEquals(LocalDate.of(2026, 11, 13), period.getEndDate());
    }

    @ParameterizedTest(name = "day {0} is {1}")
    @CsvSource({"0, 2026-11-09", "1, 2026-11-10", "4, 2026-11-13"})
    void givesRealDateOfEachDay(int day, LocalDate expected) {
        assertEquals(expected, fiveDaysFourSlots().getDate(day));
    }

    @Test
    void makesTimeSlotWithRealDateAndHours() {
        TimeSlot slot = fiveDaysFourSlots().getTimeSlot(4, 2);

        assertEquals(LocalDate.of(2026, 11, 13), slot.getDate());
        assertEquals(LocalTime.of(14, 0), slot.getStart());
        assertEquals(LocalTime.of(16, 0), slot.getEnd());
        assertEquals("13 November 2026 (Friday), 14:00–16:00", slot.toString());
    }

    @Test
    void listsAllSlotsInOrder() {
        List<TimeSlot> slots = fiveDaysFourSlots().getAllTimeSlots();

        assertEquals(20, slots.size());
        assertEquals("9 November 2026 (Monday), 09:00–11:00", slots.getFirst().toString());
        assertEquals("13 November 2026 (Friday), 16:30–18:30", slots.getLast().toString());
        for (int i = 1; i < slots.size(); i++) {
            TimeSlot before = slots.get(i - 1);
            TimeSlot after = slots.get(i);
            assertTrue(before.getDate().isBefore(after.getDate()) || before.getEnd().isBefore(after.getStart())
                    || before.getEnd().equals(after.getStart()));
        }
    }

    @Test
    void slotsOfDifferentLengthsAreAllowed() {
        // a 3-hour morning slot and a 1.5-hour afternoon slot
        ExamPeriod mixed = new ExamPeriod(START, 2, List.of(hours("09:30", "12:30"), hours("13:30", "15:00")));

        assertEquals(4, mixed.getTotalSlots());
        assertEquals(LocalTime.of(12, 30), mixed.getTimeSlot(0, 0).getEnd());
        assertEquals(LocalTime.of(15, 0), mixed.getTimeSlot(1, 1).getEnd());
    }

    @Test
    void wrongValuesAreNotAllowed() {
        List<SlotHours> oneSlot = List.of(hours("09:00", "11:00"));

        assertThrows(IllegalArgumentException.class, () -> new ExamPeriod(null, 5, oneSlot));
        assertThrows(IllegalArgumentException.class, () -> new ExamPeriod(START, 0, oneSlot));
        assertThrows(IllegalArgumentException.class, () -> new ExamPeriod(START, 5, List.of()));
        assertThrows(IllegalArgumentException.class, () -> hours("11:00", "09:00"));
    }

    @Test
    void slotsMustNotOverlap() {
        List<SlotHours> overlapping = List.of(hours("09:00", "11:00"), hours("10:30", "12:30"));

        assertThrows(IllegalArgumentException.class, () -> new ExamPeriod(START, 5, overlapping));
    }

    @Test
    void dayOrSlotOutsideThePeriodIsAnError() {
        ExamPeriod period = fiveDaysFourSlots();

        assertThrows(IllegalArgumentException.class, () -> period.getDate(5));
        assertThrows(IllegalArgumentException.class, () -> period.getTimeSlot(0, 4));
        assertThrows(IllegalArgumentException.class, () -> period.getTimeSlot(-1, 0));
    }
}
