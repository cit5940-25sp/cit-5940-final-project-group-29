import view.GameView; // Assuming GameView is in the view package

import java.io.IOException;

/**
 * Main application class for the Movie Name Game.
 * Initializes the game view, loads data from CSV files, runs the game loop,
 * and handles shutdown.
 */
public class MovieNameGame {

    /**
     * The main entry point of the application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        GameView gameView = null;
        System.out.println("Starting Movie Name Game...");

        try {
            // 1. Create the GameView instance (initializes Lanterna)
            gameView = new GameView();
            System.out.println("GameView created.");

            // 2. Define the paths to your CSV data files.
            String moviesCsvPath = "data/tmdb_5000_movies.csv";
            String creditsCsvPath = "data/tmdb_5000_credits.csv";
            System.out.println("Attempting to initialize game using CSV files:");
            System.out.println("  Movies: " + moviesCsvPath);
            System.out.println("  Credits: " + creditsCsvPath);


            // 3. Initialize the game data, controller, and initial state within GameView
            boolean initialized = gameView.initializeGame(moviesCsvPath, creditsCsvPath);
            System.out.println("Game initialization result: " + (initialized ? "Success" : "Failed"));


            // 4. If initialization was successful, run the main game loop
            if (initialized) {
                System.out.println("Starting game loop...");
                gameView.runGameLoop(); // This blocks until the game ends or is exited
                System.out.println("Game loop finished.");
            } else {
                // Initialization failed, GameView should display an error.
                System.err.println("Game initialization failed. See TUI for details. Exiting.");
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }

        } catch (IOException e) {
            // Handle errors during Lanterna setup or shutdown
            System.err.println("An critical I/O error occurred setting up or running the game view: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Catch any other unexpected errors during setup or runtime
            System.err.println("An unexpected critical error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Ensure resources are cleaned up regardless of errors
            System.out.println("Attempting to shut down GameView...");
            if (gameView != null) {
                try {
                    gameView.shutdown(); // Close Lanterna screen/terminal, stop timers
                } catch (IOException e) {
                    System.err.println("Error during GameView shutdown: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Movie Name Game finished.");
        }
    }
}
