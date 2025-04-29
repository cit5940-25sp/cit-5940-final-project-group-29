[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/nK589Lr0)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=19360701&assignment_repo_type=AssignmentRepo)

# Movie Name Game

## Project Overview
This project implements a text-based multiplayer game inspired by [cine2nerdle](https://cine2nerdle.app/battle), where players name movies that are connected by shared actors, directors, writers, cinematographers, or composers.  
The game uses data from TMDB and enforces win conditions based on genres or years.

## Directory Structure

```
|
|--- .gitignore          # Files and folders to be excluded from Git tracking
|--- README.md           # Overview and explanation of this repository
|--- src/
|    |--- controller/    # Game controller logic
|    |--- model/         # Core data models (Movie, Player, Person, etc)
|    |--- strategy/      # Link strategies and win condition strategies
|    |--- view/          # Text-based user interface (TUI)
|    |--- MovieNameGame.java # Program entry point (main method)
|--- test/
|    |--- controller/    # Unit tests for controller classes
|    |--- model/         # Unit tests for model classes
|    |--- strategy/      # Unit tests for strategy classes
|    |--- view/          # Unit tests for view classes
```

## How to Run
From the project root directory, compile and run:

```bash
javac src/**/*.java
java src/MovieNameGame
```

Or use IntelliJ's "Run" configuration for `MovieNameGame`.

## How to Test
Run all unit tests using JUnit 5:

- In IntelliJ: Right-click the `test/` folder → "Run All Tests"
- Or use the test runner integrated into IntelliJ.

## Notes
- The project follows the Model-View-Controller (MVC) design pattern.
- An additional design pattern, Strategy Pattern, is used for dynamic link validation and win condition checking.
- This project uses Java 8+ and JUnit 5.8.1.
