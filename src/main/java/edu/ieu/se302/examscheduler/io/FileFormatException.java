package edu.ieu.se302.examscheduler.io;

/**
 * Thrown when a line in an input file is not in the expected format.
 * The message tells the user the file name and the line number,
 * for example: "classrooms.csv, line 4: capacity must be a whole number".
 */
public class FileFormatException extends Exception {

    private final String fileName;
    private final int lineNumber;

    public FileFormatException(String fileName, int lineNumber, String problem) {
        super(fileName + ", line " + lineNumber + ": " + problem);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getFileName() {
        return fileName;
    }

    /** Line number in the file, starting from 1. */
    public int getLineNumber() {
        return lineNumber;
    }
}