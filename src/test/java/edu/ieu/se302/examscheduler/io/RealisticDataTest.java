package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.Classroom;
import edu.ieu.se302.examscheduler.model.Course;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests with a realistic data set (src/test/resources/realistic):
 * 12 classrooms with mixed capacities (25 to 200) and 31 courses with real course codes
 * and 0 to 240 students. Student IDs are 11 digits and many students take several
 * courses, so there are many conflicts between courses.
 */
class RealisticDataTest {

    private static List<Classroom> classrooms;
    private static List<Course> courses;

    @BeforeAll
    static void readFiles() throws Exception {
        classrooms = new ClassroomReader().read(resource("/realistic/classrooms_realistic.csv"));
        courses = new CourseStudentReader().read(resource("/realistic/courses_realistic.csv"));
    }

    private static Path resource(String name) throws Exception {
        URL url = RealisticDataTest.class.getResource(name);
        assertNotNull(url, "test file is missing: " + name);
        return Path.of(url.toURI());
    }

    private static Classroom classroom(String name) {
        return classrooms.stream().filter(c -> c.getName().equals(name)).findFirst()
                .orElseThrow(() -> new AssertionError("classroom not found: " + name));
    }

    private static Course course(String code) {
        return courses.stream().filter(c -> c.getCode().equals(code)).findFirst()
                .orElseThrow(() -> new AssertionError("course not found: " + code));
    }

    // ---------- classrooms ----------

    @Test
    void readsAllClassrooms() {
        assertEquals(12, classrooms.size());
    }

    @ParameterizedTest(name = "{0} has {1} seats")
    @CsvSource({"M302, 45", "D104, 120", "D105, 80", "SB-Z10, 25", "AMFI-1, 200", "E101, 35"})
    void classroomHasExpectedCapacity(String name, int capacity) {
        assertEquals(capacity, classroom(name).getCapacity());
    }

    @Test
    void capacitiesAreMixed() {
        int min = classrooms.stream().mapToInt(Classroom::getCapacity).min().orElseThrow();
        int max = classrooms.stream().mapToInt(Classroom::getCapacity).max().orElseThrow();
        int total = classrooms.stream().mapToInt(Classroom::getCapacity).sum();

        assertEquals(25, min);
        assertEquals(200, max);
        assertEquals(755, total);
    }

    // ---------- courses ----------

    @Test
    void readsAllCoursesInFileOrder() {
        assertEquals(31, courses.size());
        assertEquals("MATH153", courses.get(0).getCode());
        assertEquals("SE499", courses.get(courses.size() - 1).getCode());
    }

    @ParameterizedTest(name = "{0} has {1} students")
    @CsvSource({
            "MATH153, 240", "PHYS101, 180", "SE115, 120", "SE302, 52",
            "CE342, 12", "SE375, 8", "SE490, 3", "SE499, 0"
    })
    void courseHasExpectedStudentCount(String code, int count) {
        assertEquals(count, course(code).getStudentCount());
    }

    @Test
    void totalEnrollmentIsCorrect() {
        int total = courses.stream().mapToInt(Course::getStudentCount).sum();

        assertEquals(1904, total);
    }

    @Test
    void studentIdsHaveElevenDigits() {
        for (Course course : courses) {
            for (String id : course.getStudentIds()) {
                assertTrue(id.matches("\\d{11}"), course.getCode() + " has a bad ID: " + id);
            }
        }
    }

    @Test
    void manyStudentsTakeSeveralCourses() {
        Map<String, Integer> coursesPerStudent = new HashMap<>();
        for (Course course : courses) {
            for (String id : course.getStudentIds()) {
                coursesPerStudent.merge(id, 1, Integer::sum);
            }
        }
        int busiest = coursesPerStudent.values().stream().mapToInt(Integer::intValue).max().orElseThrow();
        long withFiveOrMore = coursesPerStudent.values().stream().filter(n -> n >= 5).count();

        assertTrue(busiest >= 5, "busiest student has only " + busiest + " courses");
        assertTrue(withFiveOrMore > 10, "only " + withFiveOrMore + " students take 5 or more courses");
    }

    @Test
    void bigCoursesShareStudents() {
        Set<String> math = new HashSet<>(course("MATH153").getStudentIds());
        Set<String> shared = new HashSet<>(course("SE115").getStudentIds());
        shared.retainAll(math);

        assertFalse(shared.isEmpty(), "MATH153 and SE115 should have common students (a conflict)");
    }

    // ---------- both files together ----------

    @Test
    void onlyMath153IsBiggerThanEveryClassroom() {
        int biggestRoom = classrooms.stream().mapToInt(Classroom::getCapacity).max().orElseThrow();
        List<String> tooBig = courses.stream()
                .filter(c -> c.getStudentCount() > biggestRoom)
                .map(Course::getCode)
                .toList();

        // MATH153 (240) must be split over more than one classroom (FR-14)
        assertEquals(List.of("MATH153"), tooBig);
    }

    @Test
    void biggestCourseFitsWhenAllRoomsAreUsedTogether() {
        int totalSeats = classrooms.stream().mapToInt(Classroom::getCapacity).sum();
        int biggestCourse = courses.stream().mapToInt(Course::getStudentCount).max().orElseThrow();

        assertTrue(biggestCourse <= totalSeats);
    }
}
