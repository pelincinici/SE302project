package edu.ieu.se302.examscheduler.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * One exam time: a real date and a start and end time,
 * for example "9 November 2026 (Monday), 09:00–11:00" (FR-17).
 * <p>
 * dayIndex is the day of the exam period (0 = first day) and slotIndex is the slot
 * of that day (0 = first slot). Slots are made by {@link ExamPeriod#getTimeSlot(int, int)}.
 * For an exam that is entered by the user (FR-18) the times may be different from
 * the normal slot times.
 */
public class TimeSlot {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMMM yyyy (EEEE)", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;
    private final int dayIndex;
    private final int slotIndex;
    private final LocalTime start;
    private final LocalTime end;

    public TimeSlot(LocalDate date, int dayIndex, int slotIndex, LocalTime start, LocalTime end) {
        if (date == null || start == null || end == null) {
            throw new IllegalArgumentException("date and times must be given");
        }
        if (dayIndex < 0 || slotIndex < 0) {
            throw new IllegalArgumentException("day and slot numbers start from 0");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("slot must end after it starts: " + start + "-" + end);
        }
        this.date = date;
        this.dayIndex = dayIndex;
        this.slotIndex = slotIndex;
        this.start = start;
        this.end = end;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    /**
     * True if this slot comes right after the other one on the same day.
     * The last slot of a day and the first slot of the next day are not consecutive
     * (team decision).
     */
    public boolean isRightAfter(TimeSlot other) {
        return dayIndex == other.dayIndex && slotIndex == other.slotIndex + 1;
    }

    /** True if the two slots are next to each other on the same day (in any order). */
    public boolean isNextTo(TimeSlot other) {
        return isRightAfter(other) || other.isRightAfter(this);
    }

    public boolean isSameDay(TimeSlot other) {
        return date.equals(other.date);
    }

    /** True if the two times share at least one minute on the same date. */
    public boolean overlaps(TimeSlot other) {
        return isSameDay(other) && start.isBefore(other.end) && other.start.isBefore(end);
    }

    /** For example "9 November 2026 (Monday), 09:00–11:00". */
    @Override
    public String toString() {
        return date.format(DATE_FORMAT) + ", " + start.format(TIME_FORMAT) + "–" + end.format(TIME_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TimeSlot slot
                && date.equals(slot.date) && slotIndex == slot.slotIndex
                && start.equals(slot.start) && end.equals(slot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, slotIndex, start, end);
    }
}
