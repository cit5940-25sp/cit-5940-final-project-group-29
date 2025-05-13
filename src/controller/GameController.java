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
 * and provides detailed information for display as per requirements.
 */
public class GameController {

    private final MovieIndex movieIndex;
    private ILinkStrategy currentLinkStrategy;
    private IWinCondition currentWinCondition;
    private Player currentPlayer;
    private Player otherPlayer;
    private Player winner;
    private boolean gameOver = false;
    private int movesMadeThisGame = 0;  // Counts player moves after initial movie
    private final List<GameMove> gameMoveHistory;
    private final Random random = new Random();

    /**
     * Inner class to store detailed information about each game move.
     */
    public static class GameMove {
        public final Movie movie;
        public final Player player;  // Player who played this movie
        public final String linkStrategyName;  // Name of the strategy used to link this movie
        public final String linkReason;  // Specific reason for the link
        public final boolean playerFirstMove;  // True if this is the first movie played by a player after initial

        /**
         * Constructor for a game move.
         *
         * @param movie The movie played.
         * @param player The player who made the move.
         * @param linkStrategyName The strategy used for the link.
         * @param linkReason The reason for the link.
         * @param playerFirstMove Whether this is the player's first move after the initial movie.
         */
        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason, boolean playerFirstMove) {
            this.movie = movie;
            this.player = player;
            this.linkStrategyName = linkStrategyName;
            this.linkReason = linkReason;
            this.playerFirstMove = playerFirstMove;
        }

        /**
         * Constructor for the initial system movie.
         *
         * @param movie The movie played.
         * @param player The player who made the move.
         * @param linkStrategyName The strategy used for the link.
         * @param linkReason The reason for the link.
         */
        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason) {
            this(movie, player, linkStrategyName, linkReason, false);
        }
    }

    /**
     * Initializes the game with players and a random starting movie.
     *
     * @param movieIndex The movie index used to fetch movie data.
     * @param p1 The first player.
     * @param p2 The second player.
     * @throws IllegalArgumentException If the movieIndex or players are null.
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
     * Initializes a new game, resetting all game-related variables.
     *
     * @return The initial movie to start the game.
     */
    public Movie initializeNewGame() {
        this.gameOver = false;
        this.winner = null;
        this.gameMoveHistory.clear();
        this.movesMadeThisGame = 0;

        if (this.currentPlayer != null) {
            this.currentPlayer.resetForNewGame();
        }

        if (this.otherPlayer != null) {
            this.otherPlayer.resetForNewGame();
        }
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
     * Selects a random initial movie from the movie index.
     *
     * @return The randomly selected movie, or null if no movies are available.
     */
    private Movie selectRandomInitialMovie() {
        Set<String> allTitles = movieIndex.getAllTitlesSorted();
        if (allTitles == null || allTitles.isEmpty()) {
            return null;
        }

        List<String> titlesList = new ArrayList<>(allTitles);
        return movieIndex.findMovieByTitle(titlesList.get(random.nextInt(titlesList.size())));
    }

    /**
     * Selects a random win condition (either by genre or year) from available options.
     *
     * @return The selected win condition, or null if no valid condition can be chosen.
     */
    private IWinCondition selectRandomWinCondition() {
        Set<String> allTitles = movieIndex.getAllTitlesSorted();
        if (allTitles == null || allTitles.isEmpty()) {
            return null;
        }

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
        if (!canUseGenre && !canUseYear) {
            return null;
        }

        int choice = -1;
        if (canUseGenre && canUseYear) {
            choice = random.nextInt(2);
        } else if (canUseGenre) {
            choice = 0;
        } else {
            choice = 1;
        }

        if (choice == 0) {
            List<String> genreList = new ArrayList<>(uniqueGenres);
            return new GenreWinCondition(genreList.get(random.nextInt(genreList.size())));
        } else {
            List<Integer> yearList = new ArrayList<>(uniqueYears);
            return new YearWinCondition(yearList.get(random.nextInt(yearList.size())));
        }
    }

    /**
     * Gets the progress of a player based on the current win condition.
     *
     * @param player The player whose progress is to be checked.
     * @return A string representing the player's progress, or "N/A" if the player or win condition is null.
     */
    public String getPlayerProgress(Player player) {
        if (player == null || currentWinCondition == null) {
            return "N/A";
        }
        return currentWinCondition.getPlayerProgress(player);
    }

    /**
     * Handles the scenario where the player loses due to timing out.
     */
    public void playerLostOnTimeout() {
        if (this.gameOver) {
            return;
        }

        System.out.println("Controller: Player " + (currentPlayer != null ? currentPlayer.getPlayerName() : "N/A") + " timed out.");
        this.gameOver = true;
        this.winner = this.otherPlayer;
    }

    /**
     * Gets the movie index used to retrieve movies during gameplay.
     *
     * @return The movie index.
     */
    public MovieIndex getMovieIndex() {
        return movieIndex;
    }

    /**
     * Sets the current link strategy for the player's turn.
     *
     * @param strategy The link strategy to set.
     */
    public void setCurrentLinkStrategy(ILinkStrategy strategy) {
        this.currentLinkStrategy = strategy;
    }

    /**
     * Gets the name of the current link strategy.
     *
     * @return The name of the current link strategy, or "None" if no strategy is selected.
     */
    public String getCurrentLinkStrategyName() {
        return (this.currentLinkStrategy != null) ? this.currentLinkStrategy.getClass().getSimpleName().replace("LinkStrategy", "") : "None";
    }

    /**
     * Gets the current link strategy.
     *
     * @return The current link strategy.
     */
    public ILinkStrategy getCurrentLinkStrategy() {
        return this.currentLinkStrategy;
    }

    /**
     * Switches the turn to the other player and resets the current link strategy.
     */
    public void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = otherPlayer;
        otherPlayer = temp;
        this.currentLinkStrategy = null;
    }

    /**
     * Processes the player's move, validating the movie title and checking if the move is valid.
     *
     * @param movieTitle The movie title guessed by the player.
     * @return A string representing the result of the move (success or failure).
     */
    public String processPlayerMove(String movieTitle) {
        if (gameOver) {
            return "Error: Game is already over.";
        }

        if (currentLinkStrategy == null) {
            return "Error: No link strategy selected for this turn.";
        }

        if (movieTitle == null || movieTitle.trim().isEmpty()) {
            return "EMPTY_INPUT";
        }

        Movie guessedMovie = movieIndex.findMovieByTitle(movieTitle.trim());
        String currentPlayerName = currentPlayer.getPlayerName();
        String otherPlayerName = otherPlayer.getPlayerName();

        if (guessedMovie == null) {
            return "NOT FOUND:" + movieTitle.trim();
        }

        if (gameMoveHistory.stream().anyMatch(move -> move.movie.getTitle().equalsIgnoreCase(guessedMovie.getTitle()))) {
            return "REPEATED_MOVE:" + guessedMovie.getTitle();
        }

        Movie lastPlayedMovieFull = gameMoveHistory.isEmpty() ? null : gameMoveHistory.get(gameMoveHistory.size() - 1).movie;
        String linkReason = "N/A (First player move)";
        String linkStrategyNameForHistory = currentLinkStrategy.getClass().getSimpleName();
        boolean isFirstPlayerMove = (gameMoveHistory.size() == 1);

        if (lastPlayedMovieFull != null && !isFirstPlayerMove) {
            if (!currentLinkStrategy.isValidLink(lastPlayedMovieFull, guessedMovie)) {
                String reasonText = currentLinkStrategy.getReason(lastPlayedMovieFull, guessedMovie);
                this.gameOver = true; this.winner = otherPlayer;
                return "Error: Invalid link to '" + guessedMovie.getTitle() + "' by " + currentPlayerName + ". Reason: " + reasonText + ". " + otherPlayerName + " wins!";
            }
            linkReason = currentLinkStrategy.getReason(lastPlayedMovieFull, guessedMovie);
        } else if (isFirstPlayerMove) {
            linkReason = "Starts the chain";
        }

        System.out.println("Controller: Valid move '" + guessedMovie.getTitle() + "' by " + currentPlayerName);
        if (currentPlayer != null) {
            currentPlayer.addPlayedMovie(guessedMovie);
        }

        gameMoveHistory.add(new GameMove(guessedMovie, currentPlayer, linkStrategyNameForHistory, linkReason, isFirstPlayerMove));
        movesMadeThisGame++;

        if (currentWinCondition.checkWin(currentPlayer)) {
            this.gameOver = true; this.winner = currentPlayer;
            return "VALID_MOVE_AND_WIN:" + guessedMovie.getTitle() + " is the winning link! " + currentPlayerName + " wins!";
        }

        return "OK:" + guessedMovie.getTitle() + " is a valid link!";
    }

    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the detailed history of the game moves.
     *
     * @return An unmodifiable list of game moves.
     */
    public List<GameMove> getDetailedGameHistory() {
        return Collections.unmodifiableList(gameMoveHistory);
    }

    /**
     * Gets the last played movie from the game history.
     *
     * @return The last movie played, or null if no movies have been played.
     */
    public Movie getLastPlayedMovieFromHistory() {
        return gameMoveHistory.isEmpty() ? null : gameMoveHistory.get(gameMoveHistory.size() - 1).movie;
    }

    /**
     * Gets the current player.
     *
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the other player.
     *
     * @return The other player.
     */
    public Player getOtherPlayer() {
        return otherPlayer;
    }

    /**
     * Gets the winner of the game.
     *
     * @return The winner player.
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Gets the current win condition description.
     *
     * @return The win condition description.
     */
    public String getCurrentWinConditionDescription() {
        return (currentWinCondition != null) ? currentWinCondition.getDescription() : "Win condition not set.";
    }

    /**
     * Gets the current round count.
     *
     * @return The current round count.
     */
    public int getRoundCount() {
        if (movesMadeThisGame == 0) {
            return 1;  // Start at round 1 as soon as first player makes a move
        }
        return (movesMadeThisGame + 1) / 2;
    }
}