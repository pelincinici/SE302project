package edu.ieu.se302.examscheduler.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The exam schedule: all exams of one exam period.
 * <p>
 * It gives the four views of the program: exams of a classroom, of a student,
 * of a course and of a day. Every list is sorted by date and time.
 * <p>
 * This class only keeps the exams. It stops two exams in the same classroom at the
 * same time and a second exam for the same course, but it does not check the student
 * rules (no exams in back-to-back slots, at most 2 exams a day); the scheduler does that.
 */
public class Schedule {

    private static final Comparator<ExamAssignment> BY_TIME = Comparator
            .comparing((ExamAssignment a) -> a.getTimeSlot().getDate())
            .thenComparing(a -> a.getTimeSlot().getStart())
            .thenComparing(a -> a.getCourse().getCode());

    private final ExamPeriod period;
    private final List<ExamAssignment> assignments = new ArrayList<>();

    public Schedule(ExamPeriod period) {
        if (period == null) {
            throw new IllegalArgumentException("exam period is missing");
        }
        this.period = period;
    }

    public ExamPeriod getPeriod() {
        return period;
    }

    /**
     * Adds an exam.
     *
     * @throws IllegalArgumentException if the course already has an exam, or if one of
     *                                  the classrooms already has an exam at that time
     */
    public void add(ExamAssignment exam) {
        String code = exam.getCourse().getCode();
        if (findByCourse(code).isPresent()) {
            throw new IllegalArgumentException("course " + code + " already has an exam");
        }
        for (ExamAssignment other : assignments) {
            if (!other.getTimeSlot().overlaps(exam.getTimeSlot())) {
                continue;
            }
            for (Classroom room : exam.getClassrooms()) {
                if (other.usesClassroom(room.getName())) {
                    throw new IllegalArgumentException("classroom " + room.getName() + " already has the exam of "
                            + other.getCourse().getCode() + " at " + other.getTimeSlot());
                }
            }
        }
        assignments.add(exam);
    }

    /** Removes the exam of a course. Returns false if the course had no exam. */
    public boolean remove(String courseCode) {
        return assignments.removeIf(a -> a.getCourse().getCode().equals(courseCode));
    }

    public int size() {
        return assignments.size();
    }

    /** All exams, sorted by date and time. */
    public List<ExamAssignment> getAssignments() {
        return sorted(assignments);
    }

    /** Course view: the exam of one course, if it has one. */
    public Optional<ExamAssignment> findByCourse(String courseCode) {
        return assignments.stream().filter(a -> a.getCourse().getCode().equals(courseCode)).findFirst();
    }

    /** Classroom view: all exams held in one classroom. */
    public List<ExamAssignment> getByClassroom(String classroomName) {
        return sorted(assignments.stream().filter(a -> a.usesClassroom(classroomName)).toList());
    }

    /** Student view: all exams of one student. */
    public List<ExamAssignment> getByStudent(String studentId) {
        return sorted(assignments.stream().filter(a -> a.getCourse().getStudentIds().contains(studentId)).toList());
    }

    /** Day view: all exams on one date. */
    public List<ExamAssignment> getByDate(LocalDate date) {
        return sorted(assignments.stream().filter(a -> a.getTimeSlot().getDate().equals(date)).toList());
    }

    /** Exams that the user entered by hand (FR-18). */
    public List<ExamAssignment> getEnteredByUser() {
        return sorted(assignments.stream().filter(ExamAssignment::isEnteredByUser).toList());
    }

    private static List<ExamAssignment> sorted(List<ExamAssignment> list) {
        List<ExamAssignment> copy = new ArrayList<>(list);
        copy.sort(BY_TIME);
        return copy;
    }
}
