package strategy;

import model.Movie;
import model.Player;

/**
 * Win condition where a player wins by naming a certain number of movies in a specific genre.
 */
public class GenreWinCondition implements IWinCondition {
    private String genre;
    private static final int WIN_COUNT = 1;

    /**
     * Constructs a {@code GenreWinCondition} that checks if the player has named
     * enough movies in the specified genre to win the game.
     *
     * @param genre the target genre required to win (e.g., "Action", "Comedy")
     */
    public GenreWinCondition(String genre) {
        this.genre = genre;
    }

    /**
     * Checks whether the player has played at least {@code WIN_COUNT} movies
     * that match the target genre.
     *
     * @param player the player whose played movies are being evaluated
     * @return {@code true} if the player has named at least {@code WIN_COUNT}
     *         movies in the specified genre, otherwise {@code false}
     */
    @Override
    public boolean checkWin(Player player) {
        int count = 0;
        for (Movie movie : player.getPlayedMovies()) {
            if (movie.getGenres().contains(genre)) {
                count++;
                if (count >= WIN_COUNT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a descriptive string that explains the win condition.
     *
     * @return a message stating how many movies of the specified genre are required to win
     */
    @Override
    public String getDescription() {
        return "Player wins by naming " + WIN_COUNT + " number of movies in the genre: " + genre;
    }

    /**
     * Returns the player's progress towards winning with this genre win condition.
     *
     * @param player the player whose progress is being evaluated
     * @return a string in the format "X/Y genre", where X is the number of movies from the target genre played by the player,
     *         and Y is the required number to win. Returns "0/Y genre" if the player or their played movies are null.
     */
    @Override
    public String getPlayerProgress(Player player) {
        if (player == null || player.getPlayedMovies() == null) return "0/" + WIN_COUNT;
        long count = player.getPlayedMovies().stream()
                .filter(movie -> movie.getGenres().stream()
                        .anyMatch(g -> g.equalsIgnoreCase(genre)))
                .count();
        return count + "/" + WIN_COUNT + " " + genre;
    }

    /**
     * Returns the target genre associated with this win condition.
     *
     * @return the genre required to win the game (e.g., "Action", "Comedy")
     */
    public String getTargetGenreForProgress() {
        return this.genre;
    }

    /**
     * Returns the win count associated with this win condition.
     *
     * @return the number of movies required from the target genre to win
     */
    public int getWinCountForProgress() {
        return WIN_COUNT;
    }
}