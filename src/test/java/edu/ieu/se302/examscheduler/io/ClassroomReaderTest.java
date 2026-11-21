package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.ieu.se302.examscheduler.model.Classroom;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClassroomReaderTest {

    private final ClassroomReader reader = new ClassroomReader();

    @TempDir
    Path tempDir;

    /** Writes the given lines to a test file and returns its path. */
    private Path file(String... lines) throws Exception {
        Path path = tempDir.resolve("classrooms.csv");
        Files.write(path, List.of(lines), StandardCharsets.UTF_8);
        return path;
    }

    @Test
    void readsSampleFile() throws Exception {
        Path sample = Path.of(getClass()
                .getResource("/sample/sampleData_AllClassroomsAndTheirCapacities.csv").toURI());

        List<Classroom> classrooms = reader.read(sample);

        assertEquals(10, classrooms.size());
        assertEquals("Classroom_01", classrooms.get(0).getName());
        assertEquals(40, classrooms.get(0).getCapacity());
        // "Classroom_02;40 " has a space at the end in the sample file
        assertEquals(40, classrooms.get(1).getCapacity());
    }

    @Test
    void worksWithoutTitleLine() throws Exception {
        List<Classroom> classrooms = reader.read(file("M302;60", "M303;45"));

        assertEquals(2, classrooms.size());
        assertEquals("M302", classrooms.get(0).getName());
        assertEquals(45, classrooms.get(1).getCapacity());
    }

    @Test
    void ignoresEmptyLinesAndSpaces() throws Exception {
        List<Classroom> classrooms = reader.read(file("TITLE", "", "  M302 ; 60  ", ""));

        assertEquals(1, classrooms.size());
        assertEquals("M302", classrooms.get(0).getName());
        assertEquals(60, classrooms.get(0).getCapacity());
    }

    @Test
    void capacityNotNumberGivesLineNumber() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("TITLE", "M302;60", "M303;abc")));

        assertEquals(3, e.getLineNumber());
        assertTrue(e.getMessage().contains("line 3"));
    }

    @Test
    void missingSemicolonGivesLineNumber() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("TITLE", "M302 60")));

        assertEquals(2, e.getLineNumber());
    }

    @Test
    void zeroCapacityIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("M302;0")));

        assertEquals(1, e.getLineNumber());
    }

    @Test
    void sameClassroomTwiceIsAnError() throws Exception {
        FileFormatException e = assertThrows(FileFormatException.class,
                () -> reader.read(file("M302;60", "M303;40", "M302;50")));

        assertEquals(3, e.getLineNumber());
    }

    @Test
    void fileWithOnlyTitleIsAnError() throws Exception {
        assertThrows(FileFormatException.class, () -> reader.read(file("TITLE")));
    }
}