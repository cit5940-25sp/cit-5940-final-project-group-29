package strategy;

import model.Movie;
import model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GenreWinCondition} class.
 * <p>
 * These tests check whether a player meets the win condition
 * by naming 3 or more movies of a specified genre.
 */
public class GenreWinConditionTest {

    /**
     * Test that a player wins after playing 3 movies of the target genre.
     */
    @Test
    void testCheckWin_withThreeMatchingGenres_returnsTrue() {
        Player player = new Player("TestPlayer");

        Movie m1 = new Movie("Action Movie 1", 2001);
        m1.addGenre("Action");

        Movie m2 = new Movie("Action Movie 2", 2002);
        m2.addGenre("Action");

        Movie m3 = new Movie("Action Movie 3", 2003);
        m3.addGenre("Action");

        player.addPlayedMovie(m1);
        player.addPlayedMovie(m2);
        player.addPlayedMovie(m3);

        GenreWinCondition condition = new GenreWinCondition("Action");
        assertTrue(condition.checkWin(player));
        assertEquals("Player wins by naming 1 number of movies in the genre: Action", condition.getDescription());
    }

    /**
     * Test that a player does not win with fewer than 3 matching movies.
     */
    @Test
    void testCheckWin_withFewerThanThreeMatchingGenres_returnsFalse() {
        Player player = new Player("TestPlayer");

        Movie m1 = new Movie("Drama 1", 1999);
        m1.addGenre("Drama");

        Movie m2 = new Movie("Drama 2", 2000);
        m2.addGenre("Drama");

        player.addPlayedMovie(m1);
        player.addPlayedMovie(m2);

        GenreWinCondition condition = new GenreWinCondition("Drama");
        assertTrue(condition.checkWin(player));
    }

    /**
     * Test that a player with no matching genres returns false.
     */
    @Test
    void testCheckWin_withNoMatchingGenres_returnsFalse() {
        Player player = new Player("TestPlayer");

        Movie m1 = new Movie("Comedy 1", 2010);
        m1.addGenre("Comedy");

        Movie m2 = new Movie("Sci-Fi 1", 2011);
        m2.addGenre("Sci-Fi");

        player.addPlayedMovie(m1);
        player.addPlayedMovie(m2);

        GenreWinCondition condition = new GenreWinCondition("Horror");
        assertFalse(condition.checkWin(player));
    }
}
