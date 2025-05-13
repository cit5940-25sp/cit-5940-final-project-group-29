package strategy;

import model.Movie;
import model.Player;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GenreWinCondition} class.
 * <p>
 * These tests check whether a player meets the win condition
 * by naming 1 or more movies of a specified genre.
 * </p>
 */
public class GenreWinConditionTest {

    /**
     * Test that a player wins after playing exactly WIN_COUNT (1) movie of the target genre.
     */
    @Test
    void testCheckWin_withOneMatchingGenre_returnsTrue() {
        Player player = new Player("TestPlayer");

        // Adding 1 "Action" movie
        Movie m1 = new Movie("Action Movie 1", 2001);
        m1.addGenre("Action");

        player.addPlayedMovie(m1);

        GenreWinCondition condition = new GenreWinCondition("Action");
        assertTrue(condition.checkWin(player));  // Check if player wins with 1 Action movie
        assertEquals("Player wins by naming 1 number of movies in the genre: Action", condition.getDescription());  // Description
    }

    /**
     * Test that a player does not win with fewer than WIN_COUNT matching movies.
     */
    @Test
    void testCheckWin_withFewerThanOneMatchingGenre_returnsFalse() {
        Player player = new Player("TestPlayer");

        // Adding no "Action" movies
        Movie m1 = new Movie("Comedy Movie 1", 2010);
        m1.addGenre("Comedy");

        Movie m2 = new Movie("Sci-Fi Movie 1", 2011);
        m2.addGenre("Sci-Fi");

        player.addPlayedMovie(m1);
        player.addPlayedMovie(m2);

        GenreWinCondition condition = new GenreWinCondition("Action");
        assertFalse(condition.checkWin(player));  // Check if player does NOT win with no Action movies
    }

    /**
     * Test that a player wins with a mix of genres and exactly WIN_COUNT matching movies.
     */
    @Test
    void testCheckWin_withMixedGenresAndOneMatching_returnsTrue() {
        Player player = new Player("TestPlayer");

        // Adding a mix of genres, but exactly 1 "Action" movie
        Movie m1 = new Movie("Action Movie 1", 2001);
        m1.addGenre("Action");

        Movie m2 = new Movie("Comedy Movie 1", 2002);
        m2.addGenre("Comedy");

        Movie m3 = new Movie("Sci-Fi Movie 1", 2003);
        m3.addGenre("Sci-Fi");

        player.addPlayedMovie(m1);
        player.addPlayedMovie(m2);
        player.addPlayedMovie(m3);

        GenreWinCondition condition = new GenreWinCondition("Action");
        assertTrue(condition.checkWin(player));  // Check if player wins with 1 Action movie
    }

    /**
     * Test the player's progress method when they have named 1 movie of the target genre.
     */
    @Test
    public void testGetPlayerProgress_withOneMatchingMovie() {
        Player player = new Player("Test Player");

        // Adding 1 "Action" movie
        Movie movie1 = new Movie("Action Movie 1", 2001);
        movie1.addGenre("Action");

        // Add the movie to the player
        player.addPlayedMovie(movie1);

        // Create a win condition for the genre "Action"
        GenreWinCondition winCondition = new GenreWinCondition("Action");

        // Check that the player's progress is 1/1
        String progress = winCondition.getPlayerProgress(player);
        assertEquals("1/1 Action", progress);
    }

    /**
     * Test the `getTargetGenreForProgress` method to check the target genre.
     */
    @Test
    public void testGetTargetGenreForProgress() {
        GenreWinCondition condition = new GenreWinCondition("Action");

        // Check if the target genre is returned correctly
        assertEquals("Action", condition.getTargetGenreForProgress());
    }

    /**
     * Test the `getWinCountForProgress` method to check the win count.
     */
    @Test
    public void testGetWinCountForProgress() {
        GenreWinCondition condition = new GenreWinCondition("Action");

        // Check if the win count is returned correctly
        assertEquals(1, condition.getWinCountForProgress());
    }
}