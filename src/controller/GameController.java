package controller;

import model.Movie;
import model.MovieIndex;
import model.Player;
import strategy.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages game state, player turns, move validation, win condition checking,
 * and provides detailed information for display in the TUI.
 */
public class GameController {

    private final MovieIndex movieIndex;
    private ILinkStrategy currentLinkStrategy;
    private IWinCondition currentWinCondition;
    private Player currentPlayer;
    private Player otherPlayer;
    private Player winner;

    private boolean gameOver = false;
    private int movesMadeThisGame = 0;
    private final List<GameMove> gameMoveHistory;

    private final Random random = new Random();

    /**
     * Represents a single move in the game, including which player played which movie,
     * the strategy used, and the reason why the move was valid.
     */
    public static class GameMove {
        public final Movie movie;
        public final Player player;
        public final String linkStrategyName;
        public final String linkReason;
        public final boolean playerFirstMove;

        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason, boolean playerFirstMove) {
            this.movie = movie;
            this.player = player;
            this.linkStrategyName = linkStrategyName;
            this.linkReason = linkReason;
            this.playerFirstMove = playerFirstMove;
        }

        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason) {
            this(movie, player, linkStrategyName, linkReason, false);
        }
    }

    /**
     * Constructs a GameController with the provided movie index and players.
     *
     * @param movieIndex MovieIndex containing all valid movies.
     * @param p1 First player.
     * @param p2 Second player.
     */
    public GameController(MovieIndex movieIndex, Player p1, Player p2) {
        if (movieIndex == null || p1 == null || p2 == null) {
            throw new IllegalArgumentException("MovieIndex and Players cannot be null.");
        }
        this.movieIndex = movieIndex;
        this.gameMoveHistory = new ArrayList<>();
        this.currentPlayer = p1;
        this.otherPlayer = p2;
    }

    /**
     * Initializes a new game session by selecting a starting movie and win condition.
     *
     * @return The initial Movie object that begins the chain, or null on failure.
     */
    public Movie initializeNewGame() {
        this.gameOver = false;
        this.winner = null;
        this.gameMoveHistory.clear();
        this.movesMadeThisGame = 0;
        if (this.currentPlayer != null) this.currentPlayer.resetForNewGame();
        if (this.otherPlayer != null) this.otherPlayer.resetForNewGame();
        this.currentLinkStrategy = null;

        Movie initialMovie = selectRandomInitialMovie();
        if (initialMovie != null) {
            gameMoveHistory.add(new GameMove(initialMovie, null, "N/A", "Initial Game Movie"));
        } else {
            System.err.println("CRITICAL: No movies available in MovieIndex to start the game.");
            this.gameOver = true;
            return null;
        }

        this.currentWinCondition = selectRandomWinCondition();
        if (this.currentWinCondition == null) {
            System.err.println("CRITICAL: Could not set a random win condition.");
            this.gameOver = true;
            return null;
        }

        System.out.println("Game Initialized. Starting Movie: " + initialMovie.getTitle() + ". Win Condition: " + currentWinCondition.getDescription());
        return initialMovie;
    }

    /**
     * Randomly selects a movie from the index to be the starting movie.
     *
     * @return A randomly chosen Movie object or null if none available.
     */
    private Movie selectRandomInitialMovie() {
        Set<String> allTitles = movieIndex.getAllTitlesSorted();
        if (allTitles == null || allTitles.isEmpty()) return null;
        List<String> titlesList = new ArrayList<>(allTitles);
        return movieIndex.findMovieByTitle(titlesList.get(random.nextInt(titlesList.size())));
    }

    /**
     * Randomly selects a win condition from available genre or year options.
     *
     * @return A randomly constructed IWinCondition, or null if none available.
     */
    private IWinCondition selectRandomWinCondition() {
        Set<String> allTitles = movieIndex.getAllTitlesSorted();
        if (allTitles == null || allTitles.isEmpty()) return null;

        Set<String> uniqueGenres = allTitles.stream()
                .map(movieIndex::findMovieByTitle)
                .filter(m -> m != null && m.getGenres() != null)
                .flatMap(m -> m.getGenres().stream())
                .filter(g -> g != null && !g.trim().isEmpty())
                .collect(Collectors.toSet());

        Set<Integer> uniqueYears = allTitles.stream()
                .map(movieIndex::findMovieByTitle)
                .filter(m -> m != null && m.getYear() > 0)
                .map(Movie::getYear)
                .collect(Collectors.toSet());

        boolean canUseGenre = !uniqueGenres.isEmpty();
        boolean canUseYear = !uniqueYears.isEmpty();
        if (!canUseGenre && !canUseYear) return null;

        int choice = (canUseGenre && canUseYear) ? random.nextInt(2) : (canUseGenre ? 0 : 1);
        return (choice == 0)
                ? new GenreWinCondition(new ArrayList<>(uniqueGenres).get(random.nextInt(uniqueGenres.size())))
                : new YearWinCondition(new ArrayList<>(uniqueYears).get(random.nextInt(uniqueYears.size())));
    }

    /**
     * Returns a string summarizing the player's progress toward the win condition.
     *
     * @param player The player to check.
     * @return A description of the player's progress.
     */
    public String getPlayerProgress(Player player) {
        if (player == null || currentWinCondition == null) return "N/A";
        return currentWinCondition.getPlayerProgress(player);
    }

    /**
     * Called when the current player runs out of time.
     * Automatically ends the game and assigns victory to the other player.
     */
    public void playerLostOnTimeout() {
        if (this.gameOver) return;
        System.out.println("Controller: Player " + (currentPlayer != null ? currentPlayer.getPlayerName() : "N/A") + " timed out.");
        this.gameOver = true;
        this.winner = this.otherPlayer;
    }

    public MovieIndex getMovieIndex() { return movieIndex; }

    public void setCurrentLinkStrategy(ILinkStrategy strategy) { this.currentLinkStrategy = strategy; }

    public String getCurrentLinkStrategyName() {
        return (this.currentLinkStrategy != null)
                ? this.currentLinkStrategy.getClass().getSimpleName().replace("LinkStrategy", "")
                : "None";
    }

    public ILinkStrategy getCurrentLinkStrategy() { return this.currentLinkStrategy; }

    /**
     * Switches the current turn to the other player and resets the current link strategy.
     */
    public void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = otherPlayer;
        otherPlayer = temp;
        this.currentLinkStrategy = null;
    }

    /**
     * Processes a player's move by validating the guessed movie.
     * Ensures the movie exists, hasn't been played, and is valid per the selected strategy.
     *
     * @param movieTitle The title of the movie guessed by the player.
     * @return A status string indicating success or the reason for failure.
     */
    public String processPlayerMove(String movieTitle) {
        if (gameOver) return "Error: Game is already over.";
        if (currentLinkStrategy == null) return "Error: No link strategy selected for this turn.";
        if (movieTitle == null || movieTitle.trim().isEmpty()) {
            return "EMPTY_INPUT";
        }

        Movie guessedMovie = movieIndex.findMovieByTitle(movieTitle.trim());
        String currentPlayerName = currentPlayer.getPlayerName();
        String otherPlayerName = otherPlayer.getPlayerName();

        if (guessedMovie == null) return "NOT FOUND:" + movieTitle.trim();

        if (gameMoveHistory.stream().anyMatch(move -> move.movie.getTitle().equalsIgnoreCase(guessedMovie.getTitle()))) {
            return "REPEATED_MOVE:" + guessedMovie.getTitle();
        }

        Movie lastPlayedMovieFull = gameMoveHistory.isEmpty() ? null : gameMoveHistory.get(gameMoveHistory.size() - 1).movie;
        String linkReason = "N/A (First player move)";
        String linkStrategyNameForHistory = currentLinkStrategy.getClass().getSimpleName();
        boolean isFirstPlayerMove = (gameMoveHistory.size() == 1);

        if (lastPlayedMovieFull != null) {
            if (!currentLinkStrategy.isValidLink(lastPlayedMovieFull, guessedMovie)) {
                String reasonText = currentLinkStrategy.getReason(lastPlayedMovieFull, guessedMovie);
                return "Error: Invalid link to '" + guessedMovie.getTitle() + "' by " + currentPlayerName + ". Reason: " + reasonText;
            }
            linkReason = currentLinkStrategy.getReason(lastPlayedMovieFull, guessedMovie);
        } else {
            linkReason = "Starts the chain";
        }

        System.out.println("Controller: Valid move '" + guessedMovie.getTitle() + "' by " + currentPlayerName);
        if (currentPlayer != null) currentPlayer.addPlayedMovie(guessedMovie);
        gameMoveHistory.add(new GameMove(guessedMovie, currentPlayer, linkStrategyNameForHistory, linkReason, isFirstPlayerMove));
        movesMadeThisGame++;

        if (currentWinCondition.checkWin(currentPlayer)) {
            this.gameOver = true;
            this.winner = currentPlayer;
            return "VALID_MOVE_AND_WIN:" + guessedMovie.getTitle() + " is the winning link! " + currentPlayerName + " wins!";
        }

        return "OK:" + guessedMovie.getTitle() + " is a valid link!";
    }

    public boolean isGameOver() { return gameOver; }

    /**
     * Returns a read-only list of all moves played so far in the game.
     *
     * @return Unmodifiable list of GameMove objects.
     */
    public List<GameMove> getDetailedGameHistory() {
        return Collections.unmodifiableList(gameMoveHistory);
    }

    /**
     * Gets the most recently played movie.
     *
     * @return The last played Movie, or null if no moves have been made.
     */
    public Movie getLastPlayedMovieFromHistory() {
        return gameMoveHistory.isEmpty() ? null : gameMoveHistory.get(gameMoveHistory.size() - 1).movie;
    }

    public Player getCurrentPlayer() { return currentPlayer; }

    public Player getOtherPlayer() { return otherPlayer; }

    public Player getWinner() { return winner; }

    public String getCurrentWinConditionDescription() {
        return (currentWinCondition != null) ? currentWinCondition.getDescription() : "Win condition not set.";
    }

    /**
     * Calculates the current round number based on how many total player moves have occurred.
     *
     * @return The current round number.
     */
    public int getRoundCount() {
        if (movesMadeThisGame == 0) return 1;
        return (movesMadeThisGame + 1) / 2;
    }
}
