package edu.ieu.se302.examscheduler.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The exam period set by the user (FR-3): first day, number of days and the
 * start and end time of each slot in a day. Every day has the same slots.
 * <p>
 * Days are counted one after another from the start date, weekends included.
 */
public class ExamPeriod {

    /** Start and end time of one slot in a day, for example 09:00–11:00. */
    public record SlotHours(LocalTime start, LocalTime end) {
        public SlotHours {
            if (start == null || end == null) {
                throw new IllegalArgumentException("slot time is missing");
            }
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("slot must end after it starts: " + start + "-" + end);
            }
        }
    }

    private final LocalDate startDate;
    private final int numberOfDays;
    private final List<SlotHours> slotHours;

    public ExamPeriod(LocalDate startDate, int numberOfDays, List<SlotHours> slotHours) {
        if (startDate == null) {
            throw new IllegalArgumentException("start date is missing");
        }
        if (numberOfDays < 1) {
            throw new IllegalArgumentException("exam period must have at least 1 day");
        }
        if (slotHours == null || slotHours.isEmpty()) {
            throw new IllegalArgumentException("a day must have at least 1 slot");
        }
        for (int i = 1; i < slotHours.size(); i++) {
            if (slotHours.get(i).start().isBefore(slotHours.get(i - 1).end())) {
                throw new IllegalArgumentException("slot " + (i + 1) + " starts before slot " + i + " ends");
            }
        }
        this.startDate = startDate;
        this.numberOfDays = numberOfDays;
        this.slotHours = List.copyOf(slotHours);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return startDate.plusDays(numberOfDays - 1L);
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public int getSlotsPerDay() {
        return slotHours.size();
    }

    public int getTotalSlots() {
        return numberOfDays * slotHours.size();
    }

    /** Slot times of a day, in order. The list cannot be changed. */
    public List<SlotHours> getSlotHours() {
        return slotHours;
    }

    /** Real date of a day of the period (0 = first day). */
    public LocalDate getDate(int dayIndex) {
        if (dayIndex < 0 || dayIndex >= numberOfDays) {
            throw new IllegalArgumentException("day " + dayIndex + " is not in the exam period");
        }
        return startDate.plusDays(dayIndex);
    }

    /** The time slot for a day and a slot number (both start from 0). */
    public TimeSlot getTimeSlot(int dayIndex, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slotHours.size()) {
            throw new IllegalArgumentException("slot " + slotIndex + " does not exist");
        }
        SlotHours hours = slotHours.get(slotIndex);
        return new TimeSlot(getDate(dayIndex), dayIndex, slotIndex, hours.start(), hours.end());
    }

    /** All time slots of the period: day by day, and slot by slot in each day. */
    public List<TimeSlot> getAllTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        for (int day = 0; day < numberOfDays; day++) {
            for (int slot = 0; slot < slotHours.size(); slot++) {
                slots.add(getTimeSlot(day, slot));
            }
        }
        return slots;
    }

    @Override
    public String toString() {
        return startDate + ", " + numberOfDays + " days, " + slotHours.size() + " slots per day";
    }
}
