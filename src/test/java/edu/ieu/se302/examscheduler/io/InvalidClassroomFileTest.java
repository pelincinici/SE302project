package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.Classroom;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every kind of broken classroom file we could think of.
 * Each case checks the line number and a part of the message the user will see.
 */
class InvalidClassroomFileTest {

    private final ClassroomReader reader = new ClassroomReader();

    @TempDir
    Path tempDir;

    private Path write(String text) throws Exception {
        Path path = tempDir.resolve("classrooms.csv");
        Files.writeString(path, text, StandardCharsets.UTF_8);
        return path;
    }

    static Stream<Arguments> brokenFiles() {
        return Stream.of(
                // description, file content, expected line, part of the message
                Arguments.of("capacity is text", "TITLE\nM302;abc", 2, "whole number"),
                Arguments.of("capacity is a decimal", "TITLE\nM302;45.5", 2, "whole number"),
                Arguments.of("capacity is empty", "TITLE\nM302;", 2, "whole number"),
                Arguments.of("capacity is too big for int", "TITLE\nM302;99999999999", 2, "whole number"),
                Arguments.of("capacity is zero", "TITLE\nM302;0", 2, "greater than 0"),
                Arguments.of("capacity is negative", "M302;-5", 1, "greater than 0"),
                Arguments.of("semicolon is missing", "TITLE\nM302 45", 2, "name;capacity"),
                Arguments.of("comma used instead of semicolon", "TITLE\nM302,45", 2, "name;capacity"),
                Arguments.of("too many columns", "TITLE\nM302;45;extra", 2, "name;capacity"),
                Arguments.of("name is empty", "TITLE\n;45", 2, "name is empty"),
                Arguments.of("same classroom twice", "TITLE\nM302;45\nD104;120\nM302;60", 4, "more than once"),
                Arguments.of("same classroom twice with spaces", "TITLE\nM302;45\n  M302 ;60", 3, "more than once"),
                Arguments.of("error after empty lines", "TITLE\n\n\nM302;45\n\n\nD104;x", 7, "whole number"),
                Arguments.of("good lines then a bad one", "TITLE\nM302;45\nM303;45\nD104;120\nD105;8O", 5, "whole number"),
                Arguments.of("letter O instead of zero in the first line", "M302;4O\nD104;120", 1, "whole number"),
                Arguments.of("decimal capacity in the first line", "M302;45.5\nD104;120", 1, "whole number"),
                Arguments.of("too many columns in the first line", "M302;45;x\nD104;120", 1, "name;capacity"),
                Arguments.of("zero capacity in the first line", "M302;0\nD104;120", 1, "greater than 0"),
                Arguments.of("only the title line", "TITLE", 1, "no classrooms"),
                Arguments.of("course file given by mistake", "CourseCode_01\n['Std_ID_001', 'Std_ID_002']", 2, "name;capacity")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenFiles")
    void brokenFileGivesLineNumberAndMessage(String description, String content, int line, String messagePart) {
        FileFormatException e = assertThrows(FileFormatException.class, () -> reader.read(write(content)));

        assertEquals(line, e.getLineNumber(), description);
        assertEquals("classrooms.csv", e.getFileName());
        assertTrue(e.getMessage().startsWith("classrooms.csv, line " + line + ": "), e.getMessage());
        assertTrue(e.getMessage().contains(messagePart), e.getMessage());
    }

    @Test
    void emptyFileIsAnError() {
        FileFormatException e = assertThrows(FileFormatException.class, () -> reader.read(write("")));

        assertTrue(e.getMessage().contains("no classrooms"));
    }

    @Test
    void missingFileGivesIOExceptionNotFormatError() {
        Path missing = tempDir.resolve("does_not_exist.csv");

        assertThrows(java.nio.file.NoSuchFileException.class, () -> reader.read(missing));
    }

    /**
     * Known limit of the title rule: a first line with no digits after ';'
     * (like "M302;forty") or with no ';' at all (like "M302 45") looks like a title,
     * so it is skipped without an error. The user interface should show how many
     * classrooms were read, so the user can notice a missing one.
     */
    @ParameterizedTest(name = "first line \"{0}\" is skipped as a title")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"M302;forty", "M302 45"})
    void firstLineWithoutDigitsAfterSemicolonIsSkipped(String firstLine) throws Exception {
        List<Classroom> rooms = reader.read(write(firstLine + "\nD104;120"));

        assertEquals(1, rooms.size());
        assertEquals("D104", rooms.getFirst().getName());
    }
}
