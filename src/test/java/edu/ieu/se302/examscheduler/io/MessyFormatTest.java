package edu.ieu.se302.examscheduler.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.ieu.se302.examscheduler.model.Classroom;
import edu.ieu.se302.examscheduler.model.Course;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Files that are valid but written in an untidy way, as they can come from
 * Excel, Notepad or another computer: BOM mark, Windows / old Mac line endings,
 * tabs, extra spaces, many empty lines, Turkish characters.
 */
class MessyFormatTest {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @TempDir
    Path tempDir;

    private Path write(String fileName, String text, boolean withBom) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (withBom) {
            bytes.write(BOM);
        }
        bytes.write(text.getBytes(StandardCharsets.UTF_8));
        Path path = tempDir.resolve(fileName);
        Files.write(path, bytes.toByteArray());
        return path;
    }

    private List<Classroom> classrooms(String text, boolean withBom) throws Exception {
        return new ClassroomReader().read(write("classrooms.csv", text, withBom));
    }

    private List<Course> courses(String text, boolean withBom) throws Exception {
        return new CourseStudentReader().read(write("courses.csv", text, withBom));
    }

    // ---------- classroom file ----------

    @Test
    void classroomFileWithBomAndWindowsLineEndings() throws Exception {
        List<Classroom> rooms = classrooms("TITLE\r\nM302;45\r\nD104;120\r\n", true);

        assertEquals(2, rooms.size());
        assertEquals("M302", rooms.getFirst().getName());
    }

    @Test
    void bomDoesNotHideFirstClassroomWhenThereIsNoTitle() throws Exception {
        List<Classroom> rooms = classrooms("M302;45\nD104;120", true);

        assertEquals(2, rooms.size());
        assertEquals("M302", rooms.getFirst().getName());
        assertEquals(45, rooms.getFirst().getCapacity());
    }

    @Test
    void mixedLineEndingsInOneFile() throws Exception {
        List<Classroom> rooms = classrooms("TITLE\r\nM302;45\nD104;120\rE101;35", false);

        assertEquals(3, rooms.size());
        assertEquals(35, rooms.get(2).getCapacity());
    }

    @Test
    void tabsAndSpacesAroundValues() throws Exception {
        List<Classroom> rooms = classrooms("TITLE\n\tM302\t;\t45\t\n   D104 ;   120   ", false);

        assertEquals("M302", rooms.getFirst().getName());
        assertEquals(45, rooms.getFirst().getCapacity());
        assertEquals("D104", rooms.get(1).getName());
        assertEquals(120, rooms.get(1).getCapacity());
    }

    @Test
    void manyEmptyAndBlankLines() throws Exception {
        List<Classroom> rooms = classrooms("\n\n\nTITLE\n\n \n\t\nM302;45\n\n\n   \nD104;120\n\n\n", false);

        assertEquals(2, rooms.size());
    }

    @Test
    void turkishCharactersInClassroomNames() throws Exception {
        List<Classroom> rooms = classrooms("DERSLİKLER\nBüyük_Amfi;150\nLab-Ğ;20\nŞ-101;40\nİ-Öğrenci_Salonu;90", false);

        assertEquals(4, rooms.size());
        assertEquals("Büyük_Amfi", rooms.getFirst().getName());
        assertEquals("Lab-Ğ", rooms.get(1).getName());
        assertEquals("İ-Öğrenci_Salonu", rooms.get(3).getName());
    }

    @ParameterizedTest(name = "title \"{0}\" is skipped")
    @ValueSource(strings = {
            "ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM",
            "Classroom;Capacity",
            "name ; capacity (seats)",
            "DERSLİKLER",
            "Classrooms for 2026-1 exams",
            "Derslik;Kapasite"
    })
    void differentTitleLinesAreSkipped(String title) throws Exception {
        List<Classroom> rooms = classrooms(title + "\nM302;45\nD104;120", false);

        assertEquals(2, rooms.size());
        assertEquals("M302", rooms.getFirst().getName());
    }

    @Test
    void leadingZerosInCapacity() throws Exception {
        List<Classroom> rooms = classrooms("TITLE\nM302;045\nLAB-A;0030", false);

        assertEquals(45, rooms.getFirst().getCapacity());
        assertEquals(30, rooms.get(1).getCapacity());
    }

    // ---------- course-student file ----------

    @Test
    void courseFileWithBomAndWindowsLineEndings() throws Exception {
        List<Course> list = courses("SE302\r\n['A1', 'A2']\r\n\r\nSE115\r\n['A3']\r\n", true);

        assertEquals(2, list.size());
        assertEquals("SE302", list.getFirst().getCode());
        assertEquals(List.of("A1", "A2"), list.getFirst().getStudentIds());
    }

    @Test
    void mixedQuotesAndSpacingInsideList() throws Exception {
        List<Course> list = courses("SE302\n[ 'A1' ,\"A2\",A3 ,   'A4'   ]", false);

        assertEquals(List.of("A1", "A2", "A3", "A4"), list.getFirst().getStudentIds());
    }

    @Test
    void tabsInsideList() throws Exception {
        List<Course> list = courses("SE302\n[\t'A1',\t'A2'\t]", false);

        assertEquals(List.of("A1", "A2"), list.getFirst().getStudentIds());
    }

    @Test
    void blankLinesWithSpacesBetweenCourses() throws Exception {
        List<Course> list = courses("SE302\n['A1']\n   \n\t\n\nSE115\n\n['A2']\n\n", false);

        assertEquals(2, list.size());
        assertEquals(List.of("A2"), list.get(1).getStudentIds());
    }

    @Test
    void courseCodesWithDashesAndDigits() throws Exception {
        List<Course> list = courses("CE-215\n['A1']\nMATH153\n['A2']\nSE_302_B\n['A3']", false);

        assertEquals("CE-215", list.getFirst().getCode());
        assertEquals("SE_302_B", list.get(2).getCode());
    }

    @Test
    void veryLongStudentListOnOneLine() throws Exception {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= 2000; i++) {
            ids.add("'" + String.format("2023060%04d", i) + "'");
        }
        List<Course> list = courses("MATH153\n[" + String.join(", ", ids) + "]", false);

        assertEquals(2000, list.getFirst().getStudentCount());
        assertEquals("20230600001", list.getFirst().getStudentIds().getFirst());
        assertEquals("20230602000", list.getFirst().getStudentIds().get(1999));
    }
}
