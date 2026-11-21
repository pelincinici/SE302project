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
├── docs/        # Project documentation (requirements, design, reports)
├── src/         # Java source code
├── .gitignore   # Java / IntelliJ ignore rules
└── README.md
```

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
