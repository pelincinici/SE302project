package edu.ieu.se302.examscheduler.io;

import edu.ieu.se302.examscheduler.model.Classroom;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the classroom file.
 * <p>
 * Format: one classroom per line as "name;capacity", for example "Classroom_01;40".
 * Empty lines and spaces around values are ignored.
 * <p>
 * The first line may be a title, for example
 * "ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM" or "Classroom;Capacity".
 * The first line is a title if it has no ';' or if the part after the first ';' has no digits.
 * So a first line with a typo in the number (like "M302;4O" or "M302;45.5") is an error,
 * not a title.
 */
public class ClassroomReader {

    public List<Classroom> read(Path file) throws IOException, FileFormatException {
        String fileName = file.getFileName().toString();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        List<Classroom> classrooms = new ArrayList<>();
        Set<String> names = new HashSet<>();
        boolean firstLine = true;

        for (int i = 0; i < lines.size(); i++) {
            int lineNumber = i + 1;
            String line = clean(lines.get(i));
            if (line.isEmpty()) {
                continue;
            }

            if (firstLine) {
                firstLine = false;
                if (isTitle(line)) {
                    continue;
                }
            }

            String[] parts = line.split(";", -1);
            if (parts.length != 2) {
                throw new FileFormatException(fileName, lineNumber,
                        "expected \"name;capacity\" but found \"" + line + "\"");
            }

            String name = parts[0].strip();
            String capacityText = parts[1].strip();

            if (name.isEmpty()) {
                throw new FileFormatException(fileName, lineNumber, "classroom name is empty");
            }
            if (!names.add(name)) {
                throw new FileFormatException(fileName, lineNumber,
                        "classroom \"" + name + "\" is listed more than once");
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityText);
            } catch (NumberFormatException e) {
                throw new FileFormatException(fileName, lineNumber,
                        "capacity must be a whole number but found \"" + capacityText + "\"");
            }
            if (capacity <= 0) {
                throw new FileFormatException(fileName, lineNumber,
                        "capacity must be greater than 0 but found " + capacity);
            }

            classrooms.add(new Classroom(name, capacity));
        }

        if (classrooms.isEmpty()) {
            throw new FileFormatException(fileName, lines.size(), "no classrooms found in the file");
        }
        return classrooms;
    }

    /**
     * A title line has no ';' at all, or no digits after the first ';'.
     * "ALL OF THE CLASSROOMS; AND THEIR CAPACITIES" and "DERSLIKLER" are titles,
     * but "M302;4O" is not, because "4O" has a digit in it.
     */
    private boolean isTitle(String line) {
        int semicolon = line.indexOf(';');
        if (semicolon < 0) {
            return true;
        }
        String afterSemicolon = line.substring(semicolon + 1);
        return afterSemicolon.chars().noneMatch(Character::isDigit);
    }

    /** Removes spaces at both ends and the invisible BOM mark some editors add. */
    private static String clean(String line) {
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        return line.strip();
    }
}
