package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Unit tests for the {@link model.Player} class.
 */
public class PlayerTest {

    /**
     * Tests that a Player object can be created with the correct name.
     */
    @Test
    public void testPlayerCreation() {
        Player player = new Player("Alice");
        assertEquals("Alice", player.getPlayerName());
        assertNotNull(player.getPlayedMovies());
        assertTrue(player.getPlayedMovies().isEmpty());
    }

    /**
     * Tests that a movie can be added to the player's played movies list.
     */
    @Test
    public void testAddPlayedMovie() {
        Player player = new Player("Bob");
        Movie movie = new Movie("Inception", 2010);
        player.addPlayedMovie(movie);

        List<Movie> played = player.getPlayedMovies();
        assertEquals(1, played.size());
        assertEquals("Inception", played.get(0).getTitle());
        assertEquals(2010, played.get(0).getYear());
    }
}
