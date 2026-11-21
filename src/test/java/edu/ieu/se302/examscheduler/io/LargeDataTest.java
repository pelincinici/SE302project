package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import edu.ieu.se302.examscheduler.model.Classroom;
import edu.ieu.se302.examscheduler.model.Course;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Much bigger files than the sample data, made inside the test.
 * Checks that reading stays correct and fast (a whole university, not one department).
 */
class LargeDataTest {

    @TempDir
    Path tempDir;

    @Test
    void readsThousandClassroomsWithRandomCapacities() throws Exception {
        Random random = new Random(42);
        List<String> lines = new ArrayList<>();
        lines.add("ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM");
        int totalSeats = 0;
        for (int i = 1; i <= 1000; i++) {
            int capacity = 10 + random.nextInt(291);   // 10 .. 300
            totalSeats += capacity;
            lines.add("Room_" + i + ";" + capacity);
        }
        Path file = tempDir.resolve("classrooms_big.csv");
        Files.write(file, lines, StandardCharsets.UTF_8);

        List<Classroom> rooms = assertTimeout(Duration.ofSeconds(2), () -> new ClassroomReader().read(file));

        assertEquals(1000, rooms.size());
        assertEquals("Room_1000", rooms.get(999).getName());
        assertEquals(totalSeats, rooms.stream().mapToInt(Classroom::getCapacity).sum());
    }

    @Test
    void readsFiveHundredCoursesFromTwentyThousandStudents() throws Exception {
        Random random = new Random(7);
        List<String> pool = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            pool.add(String.format("%d%04d%04d", 2020 + i % 6, 600 + i % 40, i));
        }

        List<String> lines = new ArrayList<>();
        int totalEnrollment = 0;
        int biggest = 0;
        for (int c = 1; c <= 500; c++) {
            int size = 1 + random.nextInt(400);   // 1 .. 400 students
            totalEnrollment += size;
            biggest = Math.max(biggest, size);
            Collections.shuffle(pool, random);
            List<String> quoted = new ArrayList<>();
            for (String id : pool.subList(0, size)) {
                quoted.add("'" + id + "'");
            }
            lines.add("COURSE_" + c);
            lines.add("[" + String.join(", ", quoted) + "]");
            lines.add("");
        }
        Path file = tempDir.resolve("courses_big.csv");
        Files.write(file, lines, StandardCharsets.UTF_8);

        List<Course> courses = assertTimeout(Duration.ofSeconds(5), () -> new CourseStudentReader().read(file));

        assertEquals(500, courses.size());
        assertEquals("COURSE_500", courses.get(499).getCode());
        assertEquals(totalEnrollment, courses.stream().mapToInt(Course::getStudentCount).sum());
        assertEquals(biggest, courses.stream().mapToInt(Course::getStudentCount).max().orElseThrow());
    }

    @Test
    void errorNearTheEndOfABigFileHasTheRightLineNumber() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int c = 1; c <= 3000; c++) {
            lines.add("COURSE_" + c);
            lines.add("['S" + c + "a', 'S" + c + "b']");
        }
        lines.add("COURSE_3001");
        lines.add("['S1', 'S2'");   // missing ] on line 6002
        Path file = tempDir.resolve("courses_big_broken.csv");
        Files.write(file, lines, StandardCharsets.UTF_8);

        FileFormatException e = org.junit.jupiter.api.Assertions.assertThrows(FileFormatException.class,
                () -> new CourseStudentReader().read(file));

        assertEquals(6002, e.getLineNumber());
    }
}
