[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/nK589Lr0)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=19360701&assignment_repo_type=AssignmentRepo)

# Movie Name Game

## Project Overview
This project implements a text-based multiplayer movie-linking game inspired by [cine2nerdle](https://cine2nerdle.app/battle).  
Players take turns naming movies that are connected to the previously played one via shared actors, directors, writers, cinematographers, or composers.

The game supports **two data loaders**:
- `MovieDataLoader` (used in the project): Loads local movie and credit data from preprocessed TMDB CSV files.
- `TMDBApiLoader` (not used in the final implementation): Designed to fetch data using the TMDB API and a user-provided API key.

**Note:** The game does not use real-time API fetching. All data in the current implementation is based on static local CSV files.

## Gameplay Rules

- A **random win condition** is assigned at the beginning of each game:
  - Either "find a movie from a specific genre" or "find a movie from a specific year".
  - The first player to play a movie that satisfies the win condition wins the game.

- On each turn:
  1. The player selects a **link strategy** by pressing 1–5 (Actor / Director / Writer / Composer / Cinematographer).
  2. A **30-second timer** starts.
  3. The player types in a movie title.
     - If the link is **valid** according to the selected strategy, the move is accepted.
     - If the link is **invalid**, the game prompts a reason, and the player can try again within the same turn.
     - If the player **fails to submit a valid movie within 30 seconds**, the game ends and they lose.

- Only **two possible outcomes**:
  - Win: Successfully play a movie satisfying the win condition.
  - Loss: Fail to provide a valid movie in time.

## Directory Structure

```

|
\|--- .gitignore              # Files and folders to be excluded from Git tracking
\|--- README.md               # Overview and explanation of this repository
\|--- config.properties       # (ignored) contains your TMDB API key (used only by TMDBApiLoader)
\|--- libs/                   # Third-party JAR dependencies (e.g., JSON)
\|--- src/
\|    |--- controller/        # Game controller logic
\|    |--- model/             # Core data models (Movie, Player, Person, etc.)
\|    |--- strategy/          # Link strategies and win condition strategies
\|    |--- view/              # Text-based user interface (TUI)
\|    |--- MovieNameGame.java # Program entry point (main method)
\|--- test/
\|    |--- controller/        # Unit tests for controller classes
\|    |--- model/             # Unit tests for model classes
\|    |--- strategy/          # Unit tests for strategy classes

````

## Configuration (Optional)
If you want to experiment with `TMDBApiLoader`, create a `config.properties` file in the project root containing your TMDB API key:

```properties
api_key=YOUR_TMDB_API_KEY
````

This file is ignored by Git for security reasons.

## How to Run

From the project root directory, compile and run:

```bash
javac -cp "libs/json-20250107.jar" src/**/*.java
java -cp "libs/json-20250107.jar:src" MovieNameGame
```

Or use IntelliJ's "Run" configuration for `MovieNameGame` (include `libs/*` on the classpath).

## How to Test

Run all unit tests using JUnit 5:

* In IntelliJ: Right-click the `test/` folder → "Run All Tests"
* Or use the terminal:

  ```bash
  javac -cp "libs/*:test" test/**/*.java
  java -jar junit-platform-console-standalone-1.8.1.jar \
    --classpath "libs/*:src:test" --scan-classpath
  ```

## Notes

* The project follows the **Model-View-Controller (MVC)** design pattern.
* The **Strategy Pattern** is used for dynamic link validation and win condition checking.
* The game runs entirely offline based on **local TMDB CSV data**.
* Java 11+, JUnit 5.8.1, and external libraries in `libs/` are used.

