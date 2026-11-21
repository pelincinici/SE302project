package edu.ieu.se302.examscheduler.io;

import edu.ieu.se302.examscheduler.model.Course;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the course-student file (attendance lists).
 *
 * Format: a course code on one line, and on the next line the list of its students:
 * <pre>
 * CourseCode_01
 * ['Std_ID_170', 'Std_ID_077', 'Std_ID_097']
 *
 * CourseCode_02
 * ['Std_ID_238', 'Std_ID_132']
 * </pre>
 * Empty lines between courses are ignored. Quotes around IDs can be ' or " or none.
 */
public class CourseStudentReader {

    public List<Course> read(Path file) throws IOException, FileFormatException {
        String fileName = file.getFileName().toString();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        List<Course> courses = new ArrayList<>();
        Set<String> codes = new HashSet<>();

        String currentCode = null;   // course code waiting for its student list
        int currentCodeLine = 0;

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = clean(lines.get(i));
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("[")) {
                // A student list. It must come right after a course code.
                if (currentCode == null) {
                    throw new FileFormatException(fileName, lineNumber,
                            "student list has no course code above it");
                }
                List<String> studentIds = parseStudentList(line, fileName, lineNumber);
                courses.add(new Course(currentCode, studentIds));
                currentCode = null;
            } else {
                // A course code.
                if (currentCode != null) {
                    throw new FileFormatException(fileName, currentCodeLine,
                            "course \"" + currentCode + "\" has no student list");
                }
                if (line.matches(".*[\\s,;'\"\\]].*")) {
                    throw new FileFormatException(fileName, lineNumber,
                            "expected a course code (like CourseCode_01) but found \"" + line + "\"");
                }
                if (!codes.add(line)) {
                    throw new FileFormatException(fileName, lineNumber,
                            "course \"" + line + "\" is listed more than once");
                }
                currentCode = line;
                currentCodeLine = lineNumber;
            }
        }

        if (currentCode != null) {
            throw new FileFormatException(fileName, currentCodeLine,
                    "course \"" + currentCode + "\" has no student list");
        }
        if (courses.isEmpty()) {
            throw new FileFormatException(fileName, lines.size(), "no courses found in the file");
        }
        return courses;
    }

    /** Turns "['Std_ID_001', 'Std_ID_002']" into a list of IDs. */
    private List<String> parseStudentList(String line, String fileName, int lineNumber)
            throws FileFormatException {
        if (!line.endsWith("]")) {
            throw new FileFormatException(fileName, lineNumber, "student list must end with ]");
        }

        String inside = line.substring(1, line.length() - 1).strip();
        List<String> ids = new ArrayList<>();
        if (inside.isEmpty()) {
            return ids;   // a course with no students
        }

        Set<String> seen = new HashSet<>();
        for (String item : inside.split(",", -1)) {
            String id = removeQuotes(item.strip());
            if (id.isEmpty()) {
                throw new FileFormatException(fileName, lineNumber,
                        "student list has an empty student ID");
            }
            if (!seen.add(id)) {
                throw new FileFormatException(fileName, lineNumber,
                        "student \"" + id + "\" is listed more than once");
            }
            ids.add(id);
        }
        return ids;
    }

    private String removeQuotes(String text) {
        if (text.length() >= 2) {
            char first = text.charAt(0);
            char last = text.charAt(text.length() - 1);
            if ((first == '\'' || first == '"') && first == last) {
                return text.substring(1, text.length() - 1).strip();
            }
        }
        return text;
    }

    /** Removes spaces at both ends and the invisible BOM mark some editors add. */
    private static String clean(String line) {
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        return line.strip();
    }
}