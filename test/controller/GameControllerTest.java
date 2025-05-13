package controller;

import model.Movie;
import model.MovieIndex;
import model.Person;
import model.PersonRole;
import model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import strategy.ActorLinkStrategy;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GameController} class.
 * Verifies correct game initialization, player moves, win/loss conditions,
 * and error handling logic.
 */
class GameControllerTest {

    private GameController gameController;
    private Player player1;
    private Player player2;
    private Movie inception;
    private Movie revenant;
    private Person leo;

    /**
     * Initializes a fresh GameController and two sample movies (with a shared actor)
     * before each test.
     */
    @BeforeEach
    void setUp() {
        player1 = new Player("Alice");
        player2 = new Player("Bob");

        // Create shared actor with proper role
        leo = new Person("Leonardo DiCaprio", PersonRole.ACTOR);

        // Setup movies
        inception = new Movie("Inception", 2010);
        inception.addActor(leo);
        inception.addGenre("Sci-Fi");

        revenant = new Movie("The Revenant", 2015);
        revenant.addActor(leo);
        revenant.addGenre("Drama");

        MovieIndex movieIndex = new MovieIndex(Arrays.asList(inception, revenant));
        gameController = new GameController(movieIndex, player1, player2);
    }

    /**
     * Verifies that the game initializes correctly with a non-null initial movie and win condition.
     */
    @Test
    void testGameInitialization() {
        Movie initial = gameController.initializeNewGame();
        assertNotNull(initial, "Initial movie should not be null");
        assertFalse(gameController.isGameOver(), "Game should not be over after initialization");
        assertNotNull(gameController.getCurrentWinConditionDescription(), "Win condition should be set");
    }

    /**
     * Verifies a valid move using the ActorLinkStrategy between two movies sharing the same actor.
     */
    @Test
    void testValidMoveWithActorLink() {
        Movie initial = gameController.initializeNewGame();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());

        // Decide what movie to guess based on which one was picked first
        String initialTitle = initial.getTitle();
        String moveTitle;

        if (initialTitle.equals("Inception")) {
            moveTitle = "The Revenant";
        } else if (initialTitle.equals("The Revenant")) {
            moveTitle = "Inception";
        } else {
            fail("Unexpected initial movie: " + initialTitle);
            return;
        }

        String result = gameController.processPlayerMove(moveTitle);
        System.out.println("Result: " + result);
        assertTrue(result.startsWith("OK:") || result.startsWith("VALID_MOVE_AND_WIN:"), "Move should be valid");
    }

    /**
     * Verifies that a repeated movie submission by the other player is correctly flagged.
     */
    @Test
    void testRepeatedMovieMove() {
        gameController.initializeNewGame();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());
        String firstMove = gameController.processPlayerMove("The Revenant");

        gameController.switchTurn();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());
        String result = gameController.processPlayerMove("The Revenant");

        if (firstMove.startsWith("VALID_MOVE_AND_WIN")) {
            assertEquals("Error: Game is already over.", result);
        } else {
            assertEquals("REPEATED_MOVE:The Revenant", result, "Should detect repeated movie");
        }
    }

    /**
     * Simulates a player timeout and checks that the game ends with the other player declared the winner.
     */
    @Test
    void testPlayerTimeoutLoss() {
        gameController.initializeNewGame();
        gameController.playerLostOnTimeout();

        assertTrue(gameController.isGameOver(), "Game should be over after timeout");
        assertEquals(player2, gameController.getWinner(), "Other player should win after timeout");
    }

    /**
     * Ensures that calling switchTurn correctly swaps the current and other player.
     */
    @Test
    void testSwitchTurn() {
        Player originalCurrent = gameController.getCurrentPlayer();
        gameController.switchTurn();
        assertEquals(originalCurrent, gameController.getOtherPlayer(), "Players should switch turns");
    }

    /**
     * Verifies that making a move without selecting a link strategy results in an appropriate error.
     */
    @Test
    void testNoLinkStrategyError() {
        gameController.initializeNewGame(); // No strategy set

        String result = gameController.processPlayerMove("The Revenant");
        assertTrue(result.contains("No link strategy selected"));
        assertFalse(gameController.isGameOver());
    }

    /**
     * Verifies that once the game is over, further move attempts are rejected.
     */
    @Test
    void testGameOverMoveAttempt() {
        gameController.initializeNewGame();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());
        gameController.processPlayerMove("The Revenant");

        gameController.playerLostOnTimeout(); // Simulate end
        String result = gameController.processPlayerMove("Inception");
        assertEquals("Error: Game is already over.", result);
    }

    /**
     * Ensures the last played movie returned by the controller matches the last submitted valid movie.
     */
    @Test
    void testGetLastPlayedMovie() {
        gameController.initializeNewGame();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());
        gameController.processPlayerMove("The Revenant");

        Movie last = gameController.getLastPlayedMovieFromHistory();
        assertNotNull(last);
        assertEquals("The Revenant", last.getTitle());
    }

    /**
     * Verifies accessors for current link strategy name and player progress do not return null.
     */
    @Test
    void testGetPlayerProgressAndStrategyName() {
        gameController.initializeNewGame();
        gameController.setCurrentLinkStrategy(new ActorLinkStrategy());

        assertEquals("Actor", gameController.getCurrentLinkStrategyName());
        assertNotNull(gameController.getPlayerProgress(player1));
    }

    /**
     * Verifies that the move history returned by the controller is immutable.
     */
    @Test
    void testGameMoveHistoryImmutable() {
        gameController.initializeNewGame();
        List<GameController.GameMove> history = gameController.getDetailedGameHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.add(null));
    }

    /**
     * Ensures the constructor of GameController throws exceptions when passed null arguments.
     */
    @Test
    void testConstructorNullChecks() {
        assertThrows(IllegalArgumentException.class, () -> new GameController(null, player1, player2));
        assertThrows(IllegalArgumentException.class, () -> new GameController(new MovieIndex(Arrays.asList()), null, player2));
        assertThrows(IllegalArgumentException.class, () -> new GameController(new MovieIndex(Arrays.asList()), player1, null));
    }
}
