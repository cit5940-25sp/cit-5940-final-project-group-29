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
}
