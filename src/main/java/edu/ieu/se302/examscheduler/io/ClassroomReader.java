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
 *
 * Format: one classroom per line as "name;capacity", for example "Classroom_01;40".
 * The first line may be a title such as
 * "ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM"; it is skipped.
 * Empty lines and spaces around values are ignored.
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

            // The first line is a title if it is not a valid "name;number" line.
            if (firstLine) {
                firstLine = false;
                if (!looksLikeClassroom(line)) {
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

    private boolean looksLikeClassroom(String line) {
        String[] parts = line.split(";", -1);
        return parts.length == 2 && parts[1].strip().matches("-?\\d+");
    }

    /** Removes spaces at both ends and the invisible BOM mark some editors add. */
    private static String clean(String line) {
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }
        return line.strip();
    }
}