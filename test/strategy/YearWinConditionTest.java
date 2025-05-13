package strategy;

import model.Movie;
import model.Player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link strategy.YearWinCondition} class.
 */
public class YearWinConditionTest {

    /**
     * Tests that a YearWinCondition object can be instantiated with a target year.
     */
    @Test
    public void testYearWinConditionInstantiation() {
        YearWinCondition condition = new YearWinCondition(2010);
        assertNotNull(condition);
    }

    /**
     * Tests that the player wins when they have played the required number of movies
     * from the target year (WIN_COUNT = 1).
     */
    @Test
    public void testCheckWin_withSufficientMoviesFromTargetYear() {
        // Create a player
        Player player = new Player("Test Player");

        // Create some movies
        Movie movie1 = new Movie("Movie 1", 2010);  // This one should count
        Movie movie2 = new Movie("Movie 2", 2008);  // This movie shouldn't count

        // Add movies to the player
        player.addPlayedMovie(movie1);
        player.addPlayedMovie(movie2);

        // Create a win condition for year 2010 (with WIN_COUNT = 1)
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check if the player wins (should win after 1 movie from 2010)
        assertTrue(winCondition.checkWin(player), "Player should win after playing 1 movie from 2010.");
    }

    /**
     * Tests that the player does not win if they have fewer than the required number of movies
     * from the target year (WIN_COUNT = 1).
     */
    @Test
    public void testCheckWin_withInsufficientMoviesFromTargetYear() {
        // Create a player
        Player player = new Player("Test Player");

        // Create movies, only 3 from the target year (2010)
        Movie movie1 = new Movie("Movie 1", 2008);
        Movie movie2 = new Movie("Movie 2", 2011);
        Movie movie3 = new Movie("Movie 3", 2013);
        Movie movie4 = new Movie("Movie 4", 2008);
        Movie movie5 = new Movie("Movie 5", 2007);

        // Add movies to the player
        player.addPlayedMovie(movie1);
        player.addPlayedMovie(movie2);
        player.addPlayedMovie(movie3);
        player.addPlayedMovie(movie4);
        player.addPlayedMovie(movie5);

        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check that the player does not win (no movie from 2010)
        assertFalse(winCondition.checkWin(player), "Player should not win with fewer than 5 movies from 2010.");
    }

    /**
     * Tests that the player does not win if no movies match the target year.
     */
    @Test
    public void testCheckWin_withNoMoviesFromTargetYear() {
        // Create a player
        Player player = new Player("Test Player");

        // Create movies, none from the target year (2010)
        Movie movie1 = new Movie("Movie 1", 2005);
        Movie movie2 = new Movie("Movie 2", 2008);
        Movie movie3 = new Movie("Movie 3", 2015);

        // Add movies to the player
        player.addPlayedMovie(movie1);
        player.addPlayedMovie(movie2);
        player.addPlayedMovie(movie3);

        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check that the player does not win (no movie from 2010)
        assertFalse(winCondition.checkWin(player), "Player should not win with no movies from 2010.");
    }

    /**
     * Tests that the getDescription method returns the correct win condition description.
     */
    @Test
    public void testGetDescription() {
        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Expected description
        String expectedDescription = "Player wins by naming movies released in 2010 1 times.";

        // Check that the description is correct
        assertEquals(expectedDescription, winCondition.getDescription());
    }

    /**
     * Tests that the player wins if they have exactly 1 movie from the target year (2010).
     */
    @Test
    public void testGetPlayerProgress_withOneMovieFromTargetYear() {
        // Create a player
        Player player = new Player("Test Player");

        // Create movies, only 1 from the target year (2010)
        Movie movie1 = new Movie("Movie 1", 2010);

        // Add movies to the player
        player.addPlayedMovie(movie1);

        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check that the player's progress is 1/1
        String progress = winCondition.getPlayerProgress(player);
        assertEquals("1/1 from 2010", progress);
    }

    /**
     * Tests that the player's progress is "0/1" if they have no movies.
     */
    @Test
    public void testGetPlayerProgress_withNoMovies() {
        // Create a player
        Player player = new Player("Test Player");

        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check that the player's progress is 0/1 (no movies from 2010)
        String progress = winCondition.getPlayerProgress(player);
        assertEquals("0/1 from 2010", progress);
    }

    /**
     * Tests that the player's progress is calculated correctly when they have more than 1 movie
     * from the target year, but still need only 1 to win.
     */
    @Test
    public void testGetPlayerProgress_withMultipleMoviesFromTargetYear() {
        // Create a player
        Player player = new Player("Test Player");

        // Create movies, only 1 from the target year (2010)
        Movie movie1 = new Movie("Movie 1", 2010);
        Movie movie2 = new Movie("Movie 2", 2010);  // Only these two will count

        // Add movies to the player
        player.addPlayedMovie(movie1);
        player.addPlayedMovie(movie2);

        // Create a win condition for year 2010
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check that the player's progress is 2/1
        String progress = winCondition.getPlayerProgress(player);
        assertEquals("2/1 from 2010", progress);  // Since player played two movies, it will still be 2/1
    }

    /**
     * Tests that the getTargetYearForProgress method returns the correct target year.
     */
    @Test
    public void testGetTargetYearForProgress() {
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check if the target year is returned correctly
        assertEquals(2010, winCondition.getTargetYearForProgress());
    }

    /**
     * Tests that the getWinCountForProgress method returns the correct win count (1).
     */
    @Test
    public void testGetWinCountForProgress() {
        YearWinCondition winCondition = new YearWinCondition(2010);

        // Check if the win count is returned correctly
        assertEquals(1, winCondition.getWinCountForProgress());
    }
}