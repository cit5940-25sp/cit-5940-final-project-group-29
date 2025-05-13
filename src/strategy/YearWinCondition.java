package strategy;

import model.Movie;
import model.Player;

/**
 * Win condition where a player wins by naming movies from a specific year.
 */
public class YearWinCondition implements IWinCondition {
    private int targetYear;
    private static final int WIN_COUNT = 1;

    /**
     * Constructs a YearWinCondition for the given year.
     * @param targetYear the target release year
     */
    public YearWinCondition(int targetYear) {
        this.targetYear = targetYear;
    }

    /**
     * Checks whether the player meets the win condition.
     * @param player the player to check
     * @return true if the player has won, false otherwise
     */
    @Override
    public boolean checkWin(Player player) {
        int count = 0;
        for (Movie movie: player.getPlayedMovies()) {
            if (movie.getYear() == targetYear) {
                count++;
            }

            if (count >= WIN_COUNT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provides a description of the win condition.
     * @return a string describing the win condition
     */
    @Override
    public String getDescription() {
        return "Player wins by naming movies released in " + targetYear + " " + WIN_COUNT + " times.";
    }

    /**
     * Returns the player's progress towards winning with this year win condition.
     *
     * @param player the player whose progress is being evaluated
     * @return a string in the format "X/Y from targetYear", where X is the number of movies from the target year
     *         the player has played, and Y is the number required to win. If the player or their played movies are null,
     *         it returns "0/Y from targetYear".
     */
    @Override
    public String getPlayerProgress(Player player) {
        if (player == null || player.getPlayedMovies() == null) return "0/" + WIN_COUNT;
        long count = player.getPlayedMovies().stream()
                .filter(movie -> movie.getYear() == targetYear)
                .count();
        return count + "/" + WIN_COUNT + " from " + targetYear;
    }

    /**
     * Returns the target year for this win condition.
     *
     * @return the target year the player must meet to win
     */
    public int getTargetYearForProgress() {
        return this.targetYear;
    }

    /**
     * Returns the win count required to win with this year condition.
     *
     * @return the number of movies required to win
     */
    public int getWinCountForProgress() {
        return WIN_COUNT;
    }
}