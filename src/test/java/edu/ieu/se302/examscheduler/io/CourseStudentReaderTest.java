package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.Course;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CourseStudentReaderTest {

    private final CourseStudentReader reader = new CourseStudentReader();

    @TempDir
    Path tempDir;

    /** Writes the given lines to a test file and returns its path. */
    private Path file(String... lines) throws Exception {
        Path path = tempDir.resolve("courses.csv");
        Files.write(path, List.of(lines), StandardCharsets.UTF_8);
        return path;
    }

    @Test
    void readsSampleFile() throws Exception {
        Path sample = Path.of(getClass()
                .getResource("/sample/sampleData_AllAttendanceLists.csv").toURI());

        List<Course> courses = reader.read(sample);

        assertEquals(20, courses.size());
        assertEquals("CourseCode_01", courses.get(0).getCode());
        assertEquals("Std_ID_170", courses.get(0).getStudentIds().get(0));
        for (Course course : courses) {
            assertEquals(40, course.getStudentCount(), course.getCode());
        }
    }

    @Test
    void readsQuotesAndEmptyLines() throws Exception {
        List<Course> courses = reader.read(file(
                "SE302",
                "['Std_ID_001', \"Std_ID_002\", Std_ID_003]",
                "",
                "",
                "M302",
                "[ 'Std_ID_004' ]"));

        assertEquals(2, courses.size());
        assertEquals(List.of("Std_ID_001", "Std_ID_002", "Std_ID_003"), courses.get(0).getStudentIds());
        assertEquals(List.of("Std_ID_004"), courses.get(1).getStudentIds());
    }

    @Test
    void emptyListMeansNoStudents() throws Exception {
        List<Course> courses = reader.read(file("SE302", "[]"));

        assertEquals(0, courses.get(0).getStudentCount());
    }

    @Test
    void listWithoutCourseCodeGivesLineNumber() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001']", "['Std_ID_002']")));

        assertEquals(3, e.getLineNumber());
    }

    @Test
    void courseWithoutListGivesLineOfTheCourse() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "", "M302", "['Std_ID_001']")));

        assertEquals(1, e.getLineNumber());
        assertTrue(e.getMessage().contains("SE302"));
    }

    @Test
    void lastCourseWithoutListIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001']", "M302")));

        assertEquals(3, e.getLineNumber());
    }

    @Test
    void listWithoutClosingBracketIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001', 'Std_ID_002'")));

        assertEquals(2, e.getLineNumber());
    }

    @Test
    void emptyStudentIdIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001', , 'Std_ID_002']")));

        assertEquals(2, e.getLineNumber());
    }

    @Test
    void sameCourseTwiceIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001']", "SE302", "['Std_ID_002']")));

        assertEquals(3, e.getLineNumber());
    }

    @Test
    void sameStudentTwiceInOneCourseIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("SE302", "['Std_ID_001', 'Std_ID_001']")));

        assertEquals(2, e.getLineNumber());
    }
}