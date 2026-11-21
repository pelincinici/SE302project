package edu.ieu.se302.examscheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Smoke test: shows that JUnit runs with "mvnw test". */
class AppTest {

    @Test
    void appTitleIsSet() {
        assertEquals("Exam Scheduler", App.APP_TITLE);
    }
}
