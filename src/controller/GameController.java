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
    private int movesMadeThisGame = 0; // Counts player moves after initial movie
    private final List<GameMove> gameMoveHistory;

    private final Random random = new Random();

    /**
     * Inner class to store detailed information about each game move.
     */
    public static class GameMove {
        public final Movie movie;
        public final Player player; // Player who played this movie (null for initial movie)
        public final String linkStrategyName; // Name of the strategy used to link this movie
        public final String linkReason; // Specific reason for the link (e.g., "Shared Actor: Tom Hanks")
        public final boolean playerFirstMove; // True if this is the first movie played by a player after initial

        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason, boolean playerFirstMove) {
            this.movie = movie;
            this.player = player;
            this.linkStrategyName = linkStrategyName;
            this.linkReason = linkReason;
            this.playerFirstMove = playerFirstMove;
        }
        // Overload constructor for the initial system movie
        public GameMove(Movie movie, Player player, String linkStrategyName, String linkReason) {
            this(movie, player, linkStrategyName, linkReason, false);
        }
    }


    public GameController(MovieIndex movieIndex, Player p1, Player p2) {
        if (movieIndex == null || p1 == null || p2 == null) {
            throw new IllegalArgumentException("MovieIndex and Players cannot be null.");
        }
        this.movieIndex = movieIndex;
        this.gameMoveHistory = new ArrayList<>();
        this.currentPlayer = p1;
        this.otherPlayer = p2;
    }

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

    private Movie selectRandomInitialMovie() {
        Set<String> allTitles = movieIndex.getAllTitlesSorted();
        if (allTitles == null || allTitles.isEmpty()) return null;
        List<String> titlesList = new ArrayList<>(allTitles);
        return movieIndex.findMovieByTitle(titlesList.get(random.nextInt(titlesList.size())));
    }

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

        int choice = -1;
        if (canUseGenre && canUseYear) choice = random.nextInt(2);
        else if (canUseGenre) choice = 0;
        else choice = 1;

        if (choice == 0) {
            List<String> genreList = new ArrayList<>(uniqueGenres);
            return new GenreWinCondition(genreList.get(random.nextInt(genreList.size())));
        } else {
            List<Integer> yearList = new ArrayList<>(uniqueYears);
            return new YearWinCondition(yearList.get(random.nextInt(yearList.size())));
        }
    }

    public String getPlayerProgress(Player player) {
        if (player == null || currentWinCondition == null) return "N/A";
        return currentWinCondition.getPlayerProgress(player);
    }

    public void playerLostOnTimeout() {
        if (this.gameOver) return;
        System.out.println("Controller: Player " + (currentPlayer != null ? currentPlayer.getPlayerName() : "N/A") + " timed out.");
        this.gameOver = true;
        this.winner = this.otherPlayer;
    }

    public MovieIndex getMovieIndex() { return movieIndex; }
    public void setCurrentLinkStrategy(ILinkStrategy strategy) { this.currentLinkStrategy = strategy; }
    public String getCurrentLinkStrategyName() { return (this.currentLinkStrategy != null) ? this.currentLinkStrategy.getClass().getSimpleName().replace("LinkStrategy", "") : "None"; }
    public ILinkStrategy getCurrentLinkStrategy() { return this.currentLinkStrategy; }

    public void switchTurn() {
        Player temp = currentPlayer;
        currentPlayer = otherPlayer;
        otherPlayer = temp;
        this.currentLinkStrategy = null;
    }

    public String processPlayerMove(String movieTitle) {
        if (gameOver) return "Error: Game is already over.";
        if (currentLinkStrategy == null) return "Error: No link strategy selected for this turn.";
        if (movieTitle == null || movieTitle.trim().isEmpty()) {
            this.gameOver = true; this.winner = otherPlayer;
            return "Error: Movie title was empty. " + currentPlayer.getPlayerName() + " loses. " + otherPlayer.getPlayerName() + " wins!";
        }

        Movie guessedMovie = movieIndex.findMovieByTitle(movieTitle.trim());
        String currentPlayerName = currentPlayer.getPlayerName();
        String otherPlayerName = otherPlayer.getPlayerName();


        if (guessedMovie == null) {
            this.gameOver = true; this.winner = otherPlayer;
            return "Error: Movie '" + movieTitle.trim() + "' not found. " + currentPlayerName + " loses. " + otherPlayerName + " wins!";
        }

        if (gameMoveHistory.stream().anyMatch(move -> move.movie.getTitle().equalsIgnoreCase(guessedMovie.getTitle()))) {
            this.gameOver = true; this.winner = otherPlayer;
            return "Error: '" + guessedMovie.getTitle() + "' has already been played. " + currentPlayerName + " loses. " + otherPlayerName + " wins!";
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
        if (currentPlayer != null) currentPlayer.addPlayedMovie(guessedMovie);
        gameMoveHistory.add(new GameMove(guessedMovie, currentPlayer, linkStrategyNameForHistory, linkReason, isFirstPlayerMove));
        movesMadeThisGame++;

        if (currentWinCondition.checkWin(currentPlayer)) {
            this.gameOver = true; this.winner = currentPlayer;
            return "VALID_MOVE_AND_WIN:" + guessedMovie.getTitle() + " is the winning link! " + currentPlayerName + " wins!";
        }

        return "OK:" + guessedMovie.getTitle() + " is a valid link!";
    }

    public boolean isGameOver() { return gameOver; }

    public List<GameMove> getDetailedGameHistory() {
        return Collections.unmodifiableList(gameMoveHistory);
    }

    public Movie getLastPlayedMovieFromHistory() {
        return gameMoveHistory.isEmpty() ? null : gameMoveHistory.get(gameMoveHistory.size() - 1).movie;
    }

    public Player getCurrentPlayer() { return currentPlayer; }
    public Player getOtherPlayer() { return otherPlayer; }
    public Player getWinner() { return winner; }
    public String getCurrentWinConditionDescription() { return (currentWinCondition != null) ? currentWinCondition.getDescription() : "Win condition not set."; }

    public int getRoundCount() {
        if (movesMadeThisGame == 0) return 1; // Start at round 1 as soon as first player makes a move
        return (movesMadeThisGame + 1) / 2;
    }
}
