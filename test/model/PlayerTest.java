package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;

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

    /**
     * Tests that the connection usage map is updated correctly when a connection strategy is recorded.
     */
    @Test
    public void testRecordConnectionUsage() {
        Player player = new Player("Charlie");

        // Record usage of two strategies
        player.recordConnectionUsage("ActorConnection");
        player.recordConnectionUsage("ActorConnection");
        player.recordConnectionUsage("DirectorConnection");

        Map<String, Integer> usage = player.getConnectionUsage();

        // Verify the usage count for each strategy
        assertEquals(2, usage.get("ActorConnection"));
        assertEquals(1, usage.get("DirectorConnection"));
        assertFalse(usage.containsKey("WriterConnection"));
    }

    /**
     * Tests that the player's movies and connection usage are reset correctly when starting a new game.
     */
    @Test
    public void testResetForNewGame() {
        Player player = new Player("David");

        Movie movie = new Movie("The Dark Knight", 2008);
        player.addPlayedMovie(movie);
        player.recordConnectionUsage("ActorConnection");

        // Verify that the player has a played movie and a connection usage entry
        assertFalse(player.getPlayedMovies().isEmpty());
        assertTrue(player.getConnectionUsage().containsKey("ActorConnection"));

        // Reset for a new game
        player.resetForNewGame();

        // Verify that the player's played movies and connection usage are reset
        assertTrue(player.getPlayedMovies().isEmpty());
        assertTrue(player.getConnectionUsage().isEmpty());
    }
}