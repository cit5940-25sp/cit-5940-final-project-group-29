package model;

import java.util.ArrayList;
import java.util.HashMap; // Added for connectionUsage
import java.util.List;
import java.util.Map;    // Added for connectionUsage

/**
 * Represents a player in the Movie Name Game, tracking the movies they have played
 * and their usage of connection strategies.
 */
public class Player {
    private String playerName;
    private List<Movie> playedMovies;
    private Map<String, Integer> connectionUsage;  // Strategy class simple name to count

    /**
     * Constructs a Player with the specified name.
     *
     * @param playerName the name of the player
     */
    public Player(String playerName) {
        this.playerName = playerName;
        this.playedMovies = new ArrayList<>();
        this.connectionUsage = new HashMap<>();  // Initialize connectionUsage
    }

    /**
     * Returns the player's name.
     *
     * @return the name of the player
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Returns the list of movies played by the player.
     *
     * @return a list of movies
     */
    public List<Movie> getPlayedMovies() {
        return playedMovies;
    }

    /**
     * Adds a movie to the list of movies played by this player.
     *
     * @param movie the movie to add
     */
    public void addPlayedMovie(Movie movie) {
        if (movie != null && !this.playedMovies.contains(movie)) {
            playedMovies.add(movie);
        }
    }

    /**
     * Gets the usage count for connection strategies.
     *
     * @return A map where keys are strategy simple class names and values are usage counts.
     */
    public Map<String, Integer> getConnectionUsage() {
        return connectionUsage;
    }

    /**
     * Records the usage of a connection strategy.
     *
     * @param strategyName The simple class name of the strategy used.
     */
    public void recordConnectionUsage(String strategyName) {
        connectionUsage.put(strategyName, connectionUsage.getOrDefault(strategyName, 0) + 1);
    }

    /**
     * Resets player's played movies and connection usage, e.g., for a new game.
     */
    public void resetForNewGame() {
        playedMovies.clear();
        connectionUsage.clear();
    }
}