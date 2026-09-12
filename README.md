# Memory Wall – Java Game

[![Java CI with Maven](https://github.com/shaghayegh-IT/java-projects/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/shaghayegh-IT/java-projects/actions/workflows/maven.yml)

## Overview

Memory Wall is a memory game built in Java with Swing. It simulates a 16×16
LED wall inspired by WS2812 LED rings: the game briefly lights up a pattern
(a heart, a star, a smiley, ...), hides it, and the player has to redraw it
from memory by clicking or dragging on the grid.

![Memory Wall Screenshot](screenshots/memory-wall.png)

## Features

- 16×16 interactive LED-style grid
- 16 hand-designed patterns (heart, star, smiley, house, rocket, and more)
  plus 6 procedurally generated pattern types, for variety between rounds
- Clear round flow: countdown → show pattern → hide → draw from memory → result
- Mouse input: click or drag to draw the remembered pattern
- Score and level system, with the show time shrinking as the level rises
- Visual feedback per cell: correct, wrong, and missed
- Animated UI (idle rainbow wave, glowing button, blinking mistakes)

## Technologies

- Java 17
- Java Swing / AWT (rendering and input)
- Maven (build)
- JUnit 5 (unit tests)

## Architecture

The code is split into two packages with a one-way dependency: `ui` depends
on `game`, never the other way around. That keeps all game rules testable
without opening a window.

**`memorywall.game`** – plain Java, no Swing/AWT imports:

| Class            | Responsibility                                                            |
|------------------|----------------------------------------------------------------------------|
| `GameConfig`     | Grid size, timing and scoring constants                                   |
| `GamePhase`      | The phases a round moves through (`IDLE → COUNTDOWN → SHOW → HIDE → DRAW → RESULT`) |
| `PatternLibrary` | Builds the 16 fixed patterns and the 6 procedural pattern generators       |
| `GameBoard`      | Owns the target/player grids, phase transitions, scoring and `evaluate()`  |

**`memorywall.ui`** – Swing-specific:

| Class            | Responsibility                                                            |
|------------------|----------------------------------------------------------------------------|
| `LedPalette`     | All colors used to render the wall                                        |
| `BoardLayout`    | Converts between pixel coordinates and grid cells; defines button bounds  |
| `BoardRenderer`  | Paints the LED grid and info bar for a given `GameBoard`                  |
| `InputHandler`   | Translates mouse events into `GameBoard` calls                            |
| `MemoryWallPanel`| Wires board + renderer + input together and drives the ~60fps game loop   |
| `MemoryWallApp`  | Application entry point (`main`), builds the window                       |

## How to Run

### With Maven

```bash
mvn package
java -jar target/memory-wall-1.0.0.jar
```

### From an IDE (e.g. IntelliJ IDEA)

1. Open the project folder as a Maven project.
2. Run `memorywall.ui.MemoryWallApp`.

## Testing

Unit tests cover the parts that don't need a display: pattern geometry and
game-state transitions.

```bash
mvn test
```

- `PatternLibraryTest` – checks pattern dimensions, that patterns aren't
  empty, geometric symmetry of the heart and diamond shapes, and that
  pattern selection is reproducible for a given random seed.
- `GameBoardTest` – checks phase transitions (countdown → show → hide →
  draw), that drawing only works during the DRAW phase, and that
  `evaluate()` computes accuracy, score and level-ups correctly.

Rendering and mouse input are not unit-tested, since they need a real
display and are mostly thin Swing glue code.

## Continuous Integration

Every push to `main` and every Pull Request targeting `main` triggers a
GitHub Actions workflow
([`.github/workflows/maven.yml`](.github/workflows/maven.yml)) that builds
the project with Maven and runs the full JUnit test suite on a clean
environment (Java 17, Temurin). The workflow fails if compilation or any
test fails, so regressions are visible before a Pull Request is merged.

## What I Learned

- Separating game logic from rendering makes the logic testable without a
  GUI, and makes the rendering code easier to read since it no longer also
  decides *what* happens, only *how it looks*.
- Extracting repeated pixel-math (like the action button's position, which
  used to be calculated twice) into one shared place removes a class of bugs
  where the drawn button and the clickable area could drift apart.
- Replacing magic numbers with named constants made it much easier to see
  the actual game rules (e.g. the level-up threshold, or how show time
  shrinks with level) at a glance.
- Setting up a Maven project with JUnit 5 from scratch, including a runnable
  jar via the manifest's `Main-Class`.

## Future Improvements

- Persist high scores between runs (e.g. to a small local file)
- Add a difficulty/pattern-set selection screen
- Add more procedural pattern generators for additional variety
- Package the game as a native app image (`jpackage`) instead of a plain jar

## Author

Shaghayegh Raisi Nafchi
Bachelor student in Informatics, FHNW
Background in Computer Engineering and Web Development
