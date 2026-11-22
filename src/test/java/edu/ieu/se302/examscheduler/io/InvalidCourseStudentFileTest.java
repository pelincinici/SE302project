package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every kind of broken course-student file we could think of.
 * Each case checks the line number and a part of the message the user will see.
 */
class InvalidCourseStudentFileTest {

    private final CourseStudentReader reader = new CourseStudentReader();

    @TempDir
    Path tempDir;

    private Path write(String text) throws Exception {
        Path path = tempDir.resolve("courses.csv");
        Files.writeString(path, text, StandardCharsets.UTF_8);
        return path;
    }

    static Stream<Arguments> brokenFiles() {
        return Stream.of(
                // description, file content, expected line, part of the message
                Arguments.of("list before any course code", "['A1']", 1, "no course code"),
                Arguments.of("two lists for one course", "SE302\n['A1']\n['A2']", 3, "no course code"),
                Arguments.of("course without list in the middle", "SE302\nSE115\n['A1']", 1, "has no student list"),
                Arguments.of("course without list at the end", "SE302\n['A1']\nSE115", 3, "has no student list"),
                Arguments.of("course code with a space", "SE 302\n['A1']", 1, "expected a course code"),
                Arguments.of("course code with a comma", "SE302,SE115\n['A1']", 1, "expected a course code"),
                Arguments.of("course code with ]", "SE302]\n['A1']", 1, "expected a course code"),
                Arguments.of("list without ]", "SE302\n['A1', 'A2'", 2, "must end with ]"),
                Arguments.of("list split over two lines", "SE302\n['A1',\n'A2']", 2, "must end with ]"),
                Arguments.of("empty ID in the middle", "SE302\n['A1', , 'A2']", 2, "empty student ID"),
                Arguments.of("comma at the end of the list", "SE302\n['A1', 'A2',]", 2, "empty student ID"),
                Arguments.of("only quotes", "SE302\n['']", 2, "empty student ID"),
                Arguments.of("same student twice", "SE302\n['A1', 'A2', 'A1']", 2, "more than once"),
                Arguments.of("same student with different quotes", "SE302\n['A1', \"A1\"]", 2, "more than once"),
                Arguments.of("same course twice", "SE302\n['A1']\n\nSE302\n['A2']", 4, "more than once"),
                Arguments.of("blank lines are counted", "\n\nSE302\n['A1']\n\n\n['A2']", 7, "no course code"),
                Arguments.of("title line of the sample course list", "ALL OF THE COURSES IN THE SYSTEM\nCourseCode_01", 1,
                        "expected a course code"),
                Arguments.of("classroom file given by mistake", "ALL OF THE CLASSROOMS; AND THEIR CAPACITIES\nM302;45", 1,
                        "expected a course code"),
                Arguments.of("third course is broken",
                        "SE302\n['A1']\nSE115\n['A2']\nMATH153\n['A3', 'A4'", 6, "must end with ]")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenFiles")
    void brokenFileGivesLineNumberAndMessage(String description, String content, int line, String messagePart) {
        FileFormatException e = assertThrows(FileFormatException.class, () -> reader.read(write(content)));

        assertEquals(line, e.getLineNumber(), description);
        assertEquals("courses.csv", e.getFileName());
        assertTrue(e.getMessage().startsWith("courses.csv, line " + line + ": "), e.getMessage());
        assertTrue(e.getMessage().contains(messagePart), e.getMessage());
    }

    @Test
    void emptyFileIsAnError() {
        FileFormatException e = assertThrows(FileFormatException.class, () -> reader.read(write("")));

        assertTrue(e.getMessage().contains("no courses"));
    }

    @Test
    void missingFileGivesIOExceptionNotFormatError() {
        Path missing = tempDir.resolve("does_not_exist.csv");

        assertThrows(java.nio.file.NoSuchFileException.class, () -> reader.read(missing));
    }
}
