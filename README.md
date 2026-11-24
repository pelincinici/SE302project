# SE302-Section2-Team1

SE302 Software Engineering — Section 2, Team 1 term project repository.

## Team Members

| Name | GitHub |
|------|--------|
| Berkay Kocabıyık | [@berkaykocabiyik](https://github.com/berkaykocabiyik) |
| Doğa Pazı | [@Doga-p](https://github.com/Doga-p) |
| Pelin Çinici | [@pelincinici](https://github.com/pelincinici) |
| Mert Ali Memiş | [@mertali7](https://github.com/mertali7) |
| Bilgenur Memiş | [@bilgenurmemis](https://github.com/bilgenurmemis) |
| Elif Güneş Tüzer | [@gunestuzer](https://github.com/gunestuzer) |

## Repository Structure

```
.
├── docs/
│   └── ui/                  # Screen sketches
├── src/
│   ├── main/java/edu/ieu/se302/examscheduler/
│   │   ├── App.java         # Starts the program
│   │   ├── ui/              # Screens (JavaFX)
│   │   ├── service/         # Links the screens with the rest
│   │   ├── scheduler/       # The scheduling algorithm
│   │   ├── io/              # Reading and writing files
│   │   └── model/           # Data classes
│   └── test/
│       ├── java/...         # JUnit 5 tests (same packages as main)
│       └── resources/       # Test CSV files
├── pom.xml                  # Maven project file
├── mvnw, mvnw.cmd, .mvn/    # Maven Wrapper (no Maven install needed)
└── README.md
```

## Architecture

The program has five packages. Each package has one job, and a package only uses
the packages below it:

```
ui  ──►  service  ──►  scheduler ──┐
                  └─►  io ─────────┴─►  model
```

- **ui** – JavaFX screens: main window, dialogs, the four views (classroom, student,
  course, day) and the help window. It only shows data and passes the user's actions
  to `service`.
- **service** – loads the files with `io`, keeps the current data and exam period,
  runs `scheduler` and gives the result to `ui`.
- **scheduler** – the algorithm. Keeps manual exams fixed, places the other exams into
  slots and classrooms without breaking the student rules, and reports "no solution"
  when some exams cannot be placed. It has a limit on tries or time.
- **io** – reads the classroom file and the course–student file. A wrong line gives an
  error with the file name and line number.
- **model** – data classes (`Classroom`, `Course`, `Student`, `ExamPeriod`, `TimeSlot`,
  `ExamAssignment`, `Schedule`). They do not use any other package.

Because `scheduler` and `io` do not know about the screens, they can be tested
without opening a window.

## Getting Started

Requirements: **JDK 21 or newer**. You do not need to install Maven: the project uses the Maven Wrapper.

```
git clone https://github.com/pelincinici/SE302project.git
cd SE302project
.\mvnw.cmd javafx:run      (Windows)
./mvnw javafx:run          (macOS / Linux)
```

Run the tests with `.\mvnw.cmd test`. On Windows write `.\` at the start, otherwise some computers say "is not recognized".
The first run takes a few minutes because Maven and JavaFX are downloaded. The window may open behind other windows.

**IntelliJ IDEA:** File → Open → choose `pom.xml` → "Open as Project". Run with the Maven tool window: Plugins → javafx → `javafx:run`.
