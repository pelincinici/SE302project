/**
 * The scheduling algorithm.
 * <p>
 * It keeps the exams entered by the user fixed (FR-18), then places every other
 * course into a time slot and one or more classrooms (FR-14) so that no student has
 * exams in back-to-back slots and no student has more than 2 exams a day.
 * If some courses cannot be placed, it reports them as "no solution" (FR-15).
 * The search has a limit on tries or time, so it always finishes (NFR-3).
 * <p>
 * It works only with the {@code model} classes; it does not read files or show screens.
 */
package edu.ieu.se302.examscheduler.scheduler;
