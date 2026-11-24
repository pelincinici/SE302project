/**
 * The link between the screens and the rest of the program.
 * <p>
 * It loads the input files with the {@code io} package, keeps the current data
 * (classrooms, courses, exam period, manual exams), runs the {@code scheduler}
 * and gives the finished {@code Schedule} to the {@code ui} package.
 * Error messages for the user (for example a wrong line in a file) are passed on from here.
 */
package edu.ieu.se302.examscheduler.service;
