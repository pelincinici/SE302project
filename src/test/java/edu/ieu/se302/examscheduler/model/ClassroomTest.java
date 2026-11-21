package edu.ieu.se302.examscheduler.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClassroomTest {

    @ParameterizedTest(name = "{0} with {1} seats")
    @CsvSource({
            "M302, 45",
            "AMFI-1, 200",
            "SB-Z10, 25",
            "Büyük_Amfi, 150",
            "LAB-B, 1"
    })
    void keepsNameAndCapacity(String name, int capacity) {
        Classroom room = new Classroom(name, capacity);

        assertEquals(name, room.getName());
        assertEquals(capacity, room.getCapacity());
        assertEquals(name + " (" + capacity + ")", room.toString());
    }
}
