package view;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import controller.GameController;
import model.*;
import strategy.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Provides a Text-based User Interface (TUI) for the Movie Name Game using Lanterna.
 * Displays detailed game information as per requirements.
 * Timer directly calls a synchronized updateScreen. All text is white. Loads data from CSV.
 */
public class GameView {

    private Terminal terminal;
    private Screen screen;
    private GameController gameController;
    private MovieIndex movieIndex;

    private StringBuilder currentInput = new StringBuilder();
    private List<String> currentMovieSuggestions = new ArrayList<>();
    private int selectedSuggestionIndex = -1;


    private enum GamePhase {
        LOADING_DATA, GAME_READY, PLAYER_TURN_CHOOSE_LINK, PLAYER_TURN_INPUT_MOVIE,
        SHOWING_MOVE_RESULT, GAME_OVER
    }
    private volatile GamePhase currentPhase = GamePhase.LOADING_DATA;
    private volatile String feedbackMessage = "";

    private static final int TURN_DURATION_SECONDS = 30;
    private volatile int playerMoveSecondsRemaining = TURN_DURATION_SECONDS;
    private AtomicBoolean playerTimerRunning = new AtomicBoolean(false);
    private ScheduledExecutorService turnScheduler;
    private ScheduledFuture<?> currentTimerTask;
    // --- UI Layout Constants ---
    private final int headerLines = 5; // WinCond, P1 Name+Progress, P2 Name+Progress, Rounds, LinkFrom
    private int interactionPromptRow;

    private final int MAX_SUGGESTIONS_DISPLAYED = 3;
    private final int MAX_HISTORY_DISPLAYED = 5;   // Display last 5 movies as requested

    private static final TextColor DEFAULT_TEXT_COLOR = TextColor.ANSI.WHITE;
    private static final TextColor SELECTED_SUGGESTION_BG = TextColor.ANSI.BLUE;
    private static final TextColor INFO_COLOR = TextColor.ANSI.WHITE;
    private static final TextColor PLAYER_COLOR = TextColor.ANSI.WHITE;
    private static final TextColor MOVIE_HISTORY_COLOR = TextColor.ANSI.WHITE;
    private static final TextColor LINK_INFO_COLOR = TextColor.ANSI.WHITE;


    public GameView() throws IOException {
        terminal = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null);
        turnScheduler = Executors.newScheduledThreadPool(1);

        terminal.addResizeListener((term, newSize) -> {
            try {
                synchronized (screen) { screen.doResizeIfNecessary(); }
                updateScreen();
            } catch (IOException e) { e.printStackTrace(); }
        });
        calculateLayoutConstants();
    }

    private void calculateLayoutConstants() {
        interactionPromptRow = headerLines + 1; // Space after header
    }

    public boolean initializeGame(String moviesCsvPath, String creditsCsvPath) {
        currentPhase = GamePhase.LOADING_DATA;
        feedbackMessage = "Loading movie data from CSV...";
        try {
            updateScreen();
            List<Movie> movies = MovieDataLoader.loadMovies(moviesCsvPath, creditsCsvPath);
            if (movies == null || movies.isEmpty()) {
                feedbackMessage = "FATAL: No movies loaded!"; currentPhase = GamePhase.GAME_OVER;
                updateScreen(); return false;
            }
            this.movieIndex = new MovieIndex(movies);
            Player p1 = new Player("Player 1"); // Names can be configured later
            Player p2 = new Player("Player 2");
            this.gameController = new GameController(this.movieIndex, p1, p2);
            Movie initialMovie = this.gameController.initializeNewGame();
            if (initialMovie == null || this.gameController.isGameOver()) {
                feedbackMessage = "FATAL: Failed to init game logic."; currentPhase = GamePhase.GAME_OVER;
                updateScreen(); return false;
            }
            feedbackMessage = gameController.getCurrentPlayer().getPlayerName() + " starts.";
            currentPhase = GamePhase.PLAYER_TURN_CHOOSE_LINK;
            return true;
        } catch (Exception e) {
            feedbackMessage = "FATAL Init Error: " + e.getMessage(); currentPhase = GamePhase.GAME_OVER;
            e.printStackTrace();
            try { updateScreen(); } catch (IOException ioEx) { /* ignore */ }
            return false;
        }
    }

    public void runGameLoop() throws IOException {
        if (currentPhase == GamePhase.LOADING_DATA || (currentPhase == GamePhase.GAME_OVER && gameController == null)) {
            updateScreen();
            terminal.readInput();
            return;
        }

        boolean running = true;
        while (running) {
            if (gameController != null && gameController.isGameOver() && currentPhase != GamePhase.GAME_OVER) {
                currentPhase = GamePhase.GAME_OVER;
                if (feedbackMessage.isEmpty() || !feedbackMessage.contains("wins!")) { // Avoid overwriting specific win/loss message from controller
                    String winnerName = gameController.getWinner() != null ? gameController.getWinner().getPlayerName() : "";
                    if (!winnerName.isEmpty()) {
                        feedbackMessage = winnerName + " wins!";
                        if (gameController.getCurrentWinConditionDescription() != null && !feedbackMessage.contains("wins by default")) {
                            feedbackMessage += " (" + gameController.getCurrentWinConditionDescription() + ")";
                        }
                    } else {
                        feedbackMessage = "Game Over!"; // Generic if no winner determined by controller's message
                    }
                }
                stopPlayerTurnTimer();
            }

            updateScreen();

            if (currentPhase == GamePhase.GAME_OVER) {
                KeyStroke key = terminal.readInput();
                if (key.getKeyType() == KeyType.Escape || key.getKeyType() == KeyType.Enter) {
                    running = false;
                }
                continue;
            }

            KeyStroke keyStroke = terminal.pollInput();
            if (keyStroke != null) {
                handleKeyStroke(keyStroke);
            }

            try { Thread.sleep(30); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                feedbackMessage = "Game loop interrupted.";
                currentPhase = GamePhase.GAME_OVER;
            }
        }
    }

    // --- Timer Methods ---
    private void startPlayerTurnTimer() {
        if (playerTimerRunning.get()) return;
        playerTimerRunning.set(true);

        if (turnScheduler == null || turnScheduler.isShutdown()) {
            turnScheduler = Executors.newScheduledThreadPool(1);
        }


        if (currentTimerTask == null || currentTimerTask.isCancelled()) {
            currentTimerTask = turnScheduler.scheduleAtFixedRate(() -> {
                if (playerTimerRunning.get()) {
                    if (playerMoveSecondsRemaining > 0) {
                        playerMoveSecondsRemaining--;
                        try {
                            updateScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        stopPlayerTurnTimer();
                        handleTimeoutLogic();
                        try {
                            updateScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void stopPlayerTurnTimer() {
        playerTimerRunning.set(false);
        if (currentTimerTask != null && !currentTimerTask.isCancelled()) {
            currentTimerTask.cancel(true);
            currentTimerTask = null;
        }
    }

    private void handleTimeoutLogic() {
        if (gameController != null) {
            String currentPlayerName = gameController.getCurrentPlayer() != null ? gameController.getCurrentPlayer().getPlayerName() : "Player";
            String otherPlayerName = gameController.getOtherPlayer() != null ? gameController.getOtherPlayer().getPlayerName() : "Opponent";
            feedbackMessage = "Time's up! " + currentPlayerName + " loses. " + otherPlayerName + " wins!";
            gameController.playerLostOnTimeout();
        } else {
            feedbackMessage = "Time's up! Game controller error.";
            currentPhase = GamePhase.GAME_OVER;
        }
        currentInput.setLength(0);
        currentMovieSuggestions.clear();
        selectedSuggestionIndex = -1;
    }

    // --- Input Handling ---
    private void handleKeyStroke(KeyStroke keyStroke) throws IOException {
        if (gameController == null && currentPhase != GamePhase.GAME_OVER) return;

        if (keyStroke.getKeyType() == KeyType.Escape) {
            currentPhase = GamePhase.GAME_OVER;
            feedbackMessage = "Game exited by user.";
            stopPlayerTurnTimer();
            return;
        }

        if (currentPhase == GamePhase.PLAYER_TURN_CHOOSE_LINK ||
                (currentPhase == GamePhase.PLAYER_TURN_INPUT_MOVIE &&
                        (keyStroke.getKeyType() == KeyType.Character || keyStroke.getKeyType() == KeyType.Backspace)) ) {
            feedbackMessage = "";
        }

        switch (currentPhase) {
            case PLAYER_TURN_CHOOSE_LINK:
                handleLinkStrategyChoiceInput(keyStroke);
                break;
            case PLAYER_TURN_INPUT_MOVIE:
                handleMovieTitleInput(keyStroke);
                break;
            case SHOWING_MOVE_RESULT:
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    if (gameController.isGameOver()) {
                        currentPhase = GamePhase.GAME_OVER;
                    } else {
                        gameController.switchTurn();
                        currentPhase = GamePhase.PLAYER_TURN_CHOOSE_LINK;
                    }
                    feedbackMessage = "";
                    currentInput.setLength(0);
                    currentMovieSuggestions.clear();
                    selectedSuggestionIndex = -1;
                }
                break;
        }
    }
    private void handleLinkStrategyChoiceInput(KeyStroke keyStroke) {
        if (keyStroke.getKeyType() != KeyType.Character) return;
        if (gameController == null || gameController.getCurrentPlayer() == null) return;

        char choiceChar = keyStroke.getCharacter();
        ILinkStrategy selectedStrategy = null;
        List<ILinkStrategy> availableStrategies = Arrays.asList(
                new ActorLinkStrategy(), new DirectorLinkStrategy(), new WriterLinkStrategy(),
                new ComposerLinkStrategy(), new CinematographerLinkStrategy()
        );

        int choiceIndex = -1;
        try {
            choiceIndex = Integer.parseInt(String.valueOf(choiceChar)) - 1;
        } catch (NumberFormatException e) {
            feedbackMessage = "Invalid input. Press 1-" + availableStrategies.size() + ".";
            return;
        }

        if (choiceIndex >= 0 && choiceIndex < availableStrategies.size()) {
            selectedStrategy = availableStrategies.get(choiceIndex);
        }

        if (selectedStrategy != null) {
            Player currentPlayer = gameController.getCurrentPlayer();
            String strategyName = selectedStrategy.getClass().getSimpleName();

            int usedTimes = currentPlayer.getConnectionUsage().getOrDefault(strategyName, 0);
            if (usedTimes < 3) {
                gameController.setCurrentLinkStrategy(selectedStrategy);
                currentPhase = GamePhase.PLAYER_TURN_INPUT_MOVIE;

                currentInput.setLength(0);
                currentMovieSuggestions.clear();
                selectedSuggestionIndex = -1;
                feedbackMessage = "";

                //  reset timer and start it
                playerMoveSecondsRemaining = TURN_DURATION_SECONDS;
                startPlayerTurnTimer();

            } else {
                feedbackMessage = "Strategy [" + strategyName.replace("LinkStrategy", "") + "] used 3 times. Choose another.";
            }
        } else {
            feedbackMessage = "Invalid strategy number. Press 1-" + availableStrategies.size() + ".";
        }
    }

    private void handleMovieTitleInput(KeyStroke keyStroke) throws IOException {
        switch (keyStroke.getKeyType()) {
            case Character:
                currentInput.append(keyStroke.getCharacter());
                selectedSuggestionIndex = -1;
                updateMovieSuggestionsDisplay();
                break;
            case Backspace:
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    selectedSuggestionIndex = -1;
                    updateMovieSuggestionsDisplay();
                } else {
                    currentMovieSuggestions.clear();
                }
                break;
            case Enter:
                String finalGuess;
                if (selectedSuggestionIndex != -1 && selectedSuggestionIndex < currentMovieSuggestions.size()) {
                    finalGuess = currentMovieSuggestions.get(selectedSuggestionIndex);
                } else {
                    finalGuess = currentInput.toString().trim();
                }
                processPlayerGuessedMovie(finalGuess);
                break;
            case ArrowDown:
                if (!currentMovieSuggestions.isEmpty()) {
                    selectedSuggestionIndex = (selectedSuggestionIndex + 1) % currentMovieSuggestions.size();
                }
                break;
            case ArrowUp:
                if (!currentMovieSuggestions.isEmpty()) {
                    selectedSuggestionIndex = (selectedSuggestionIndex - 1 + currentMovieSuggestions.size()) % currentMovieSuggestions.size();
                }
                break;
            default:
                break;
        }
    }
    private void updateMovieSuggestionsDisplay() {
        if (movieIndex == null || gameController == null) {
            currentMovieSuggestions.clear();
            return;
        }
        if (currentInput.length() > 0) {
            List<String> rawSuggestions = movieIndex.getAutocompleteSuggestions(currentInput.toString());
            List<GameController.GameMove> gameHistoryMoves = gameController.getDetailedGameHistory();
            Set<String> playedTitles = gameHistoryMoves.stream()
                    .map(move -> move.movie.getTitle())
                    .collect(Collectors.toSet());
            currentMovieSuggestions = rawSuggestions.stream()
                    .filter(title -> playedTitles.stream().noneMatch(played -> played.equalsIgnoreCase(title)))
                    .limit(MAX_SUGGESTIONS_DISPLAYED)
                    .collect(Collectors.toList());
        } else {
            currentMovieSuggestions.clear();
        }
        if (selectedSuggestionIndex >= currentMovieSuggestions.size()) {
            selectedSuggestionIndex = -1;
        }
    }

    private void processPlayerGuessedMovie(String guessedMovieTitle) throws IOException {
        if (gameController == null) return;

        if (guessedMovieTitle.isEmpty()) {
            feedbackMessage = "No movie entered. Timer continues.";
            return;
        }

        List<String> playedTitles = gameController.getDetailedGameHistory().stream()
                .map(move -> move.movie.getTitle().toLowerCase())
                .collect(Collectors.toList());

        if (playedTitles.contains(guessedMovieTitle.toLowerCase())) {
            feedbackMessage = "Movie already played. Try another one.";
            return;
        }

        // try processing move
        String resultMessageFromController = gameController.processPlayerMove(guessedMovieTitle);

        if (resultMessageFromController.equals("EMPTY_INPUT")) {
            feedbackMessage = "No movie entered. Timer continues.";
            return;
        }

        if (resultMessageFromController.startsWith("NOT FOUND")) {
            feedbackMessage = "Movie not found in database. Try again.";
            return;
        }

        if (resultMessageFromController.startsWith("REPEATED_MOVE")) {
            feedbackMessage = "Movie already played. Try again.";
            return;
        }


        if (resultMessageFromController.startsWith("OK:") || resultMessageFromController.startsWith("VALID_MOVE_AND_WIN:")) {
            stopPlayerTurnTimer();
            currentPhase = GamePhase.SHOWING_MOVE_RESULT;
            feedbackMessage = resultMessageFromController;

            Player actingPlayer = resultMessageFromController.startsWith("VALID_MOVE_AND_WIN:")
                    ? gameController.getWinner()
                    : gameController.getCurrentPlayer();

            ILinkStrategy usedStrategy = gameController.getCurrentLinkStrategy();
            if (actingPlayer != null && usedStrategy != null) {
                actingPlayer.recordConnectionUsage(usedStrategy.getClass().getSimpleName());
            }

            if (!gameController.isGameOver()) {
                feedbackMessage += " (Press Enter to continue)";
            }
            return;
        }

        if (resultMessageFromController.startsWith("Error:")) {
            feedbackMessage = resultMessageFromController;
        }

    }


    // --- Drawing Methods (Revised for new requirements) ---

    private synchronized void updateScreen() throws IOException {
        if (screen == null) return;
        screen.clear();
        TerminalSize size = screen.getTerminalSize();
        if(size == null) return;

        calculateLayoutConstants();

        // 1. Draw Header (Game Info + Timer) - Now includes player progress and rounds
        drawHeaderAndTimer(size);

        // Dynamic row calculation for content below header
        int currentContentRow = headerLines;

        // 2. Draw Content based on Phase
        switch (currentPhase) {
            case LOADING_DATA:
                printStringCentered(size.getRows() / 2, feedbackMessage.isEmpty() ? "Loading..." : feedbackMessage, DEFAULT_TEXT_COLOR);
                break;
            case GAME_OVER:
                printStringCentered(size.getRows() / 2 - 1, " G A M E   O V E R ", DEFAULT_TEXT_COLOR);
                printStringCentered(size.getRows() / 2, feedbackMessage, DEFAULT_TEXT_COLOR);
                printStringCentered(size.getRows() - 2, "Press ESC or Enter to exit.", DEFAULT_TEXT_COLOR);
                break;
            case PLAYER_TURN_CHOOSE_LINK:
                currentContentRow = drawLinkStrategyChoicesLayout(interactionPromptRow, size);
                break;
            case PLAYER_TURN_INPUT_MOVIE:
                currentContentRow = drawMovieInputLayout(interactionPromptRow, size);
                currentContentRow = drawSuggestionsLayout(currentContentRow, size);
                break;
            case SHOWING_MOVE_RESULT:
                int feedbackDisplayRow = interactionPromptRow + 1;
                if (feedbackDisplayRow + 2 >= size.getRows() - (MAX_HISTORY_DISPLAYED * 2) -1) {
                    feedbackDisplayRow = interactionPromptRow;
                }
                printString(0, feedbackDisplayRow, feedbackMessage, DEFAULT_TEXT_COLOR); // Not centered for potentially long messages
                currentContentRow = feedbackDisplayRow + 2;
                break;
            case GAME_READY:
            default:
                currentContentRow = interactionPromptRow + 1;
        }

        // 3. Draw Feedback (if not already handled by SHOWING_MOVE_RESULT directly)
        if (currentPhase != GamePhase.SHOWING_MOVE_RESULT && currentPhase != GamePhase.GAME_OVER && currentPhase != GamePhase.LOADING_DATA) {
            currentContentRow = drawFeedbackMessageLayout(size, currentContentRow);
        }

        // 4. Draw Game History (at the bottom or after other content)
        int historyStartDrawRow = Math.max(currentContentRow + 1, size.getRows() - (MAX_HISTORY_DISPLAYED * 2) - 1); // Each history item takes ~2 lines
        if (currentPhase != GamePhase.LOADING_DATA && currentPhase != GamePhase.GAME_OVER) {
            drawGameHistoryLayout(size, historyStartDrawRow);
        }

        screen.refresh();
    }

    private void drawHeaderAndTimer(TerminalSize size) throws IOException {
        if (size == null || gameController == null) return;
        int currentRow = 0;

        // Timer
        String timerStr = (playerTimerRunning.get() && currentPhase == GamePhase.PLAYER_TURN_INPUT_MOVIE)
                ? "Time: " + String.format("%2d", playerMoveSecondsRemaining) + "s" : "";
        int timerX = size.getColumns() - timerStr.length() - 1;
        if (timerX < 0) timerX = 0;
        printString(timerX, currentRow, timerStr, DEFAULT_TEXT_COLOR);

        // Win Condition
        String winCond = gameController.getCurrentWinConditionDescription();
        String winCondDisplay = "Win: " + (winCond != null ? winCond : "N/A");
        int maxWinCondWidth = timerX > 0 ? timerX - 1 : size.getColumns() -1;
        if (winCondDisplay.length() > maxWinCondWidth && maxWinCondWidth > 3) {
            winCondDisplay = winCondDisplay.substring(0, maxWinCondWidth - 3) + "...";
        }
        printString(0, currentRow++, winCondDisplay, INFO_COLOR);

        // Player Info (Both users' names, win conditions, and progress)
        Player p1 = gameController.getCurrentPlayer(); // Player whose turn it is
        Player p2 = gameController.getOtherPlayer();   // The other player

        // Ensure p1 and p2 are not null before proceeding
        if (p1 == null || p2 == null) return;

        String p1Status = p1.getPlayerName() + ": " + gameController.getPlayerProgress(p1) + (p1 == gameController.getCurrentPlayer() ? " (Current)" : "");
        printString(0, currentRow++, p1Status, PLAYER_COLOR);

        String p2Status = p2.getPlayerName() + ": " + gameController.getPlayerProgress(p2) + (p2 == gameController.getCurrentPlayer() ? " (Current)" : "");
        printString(0, currentRow++, p2Status, PLAYER_COLOR);

        // Rounds Played
        printString(0, currentRow++, "Round: " + gameController.getRoundCount(), INFO_COLOR);

        // Link From Movie
        Movie lastMovie = gameController.getLastPlayedMovieFromHistory();
        String linkFromText = "Link from: ";
        if (lastMovie != null) {
            linkFromText += lastMovie.getTitle() + " (" + lastMovie.getYear() + ")";
        } else {
            linkFromText += "(Game Start)"; // Should be initial movie title
        }
        printString(0, currentRow++, linkFromText, MOVIE_HISTORY_COLOR);
    }

    private int drawLinkStrategyChoicesLayout(int startRow, TerminalSize size) throws IOException {
        if (gameController == null || gameController.getCurrentPlayer() == null) return startRow;
        Player currentPlayer = gameController.getCurrentPlayer();
        int currentRow = startRow;

        printString(0, currentRow++, "Choose Link Strategy (1-5):", DEFAULT_TEXT_COLOR);
        List<ILinkStrategy> strategies = Arrays.asList(
                new ActorLinkStrategy(), new DirectorLinkStrategy(), new WriterLinkStrategy(),
                new ComposerLinkStrategy(), new CinematographerLinkStrategy()
        );
        for (int i = 0; i < strategies.size(); i++) {
            if (currentRow >= size.getRows() -1) break;
            String stratClassName = strategies.get(i).getClass().getSimpleName();
            String stratNameSimple = stratClassName.replace("LinkStrategy", "");
            int usesLeft = 3 - (currentPlayer.getConnectionUsage() != null ? currentPlayer.getConnectionUsage().getOrDefault(stratClassName, 0) : 0);
            String text = String.format("  %d. %-15s (Uses: %d/3)", (i + 1), stratNameSimple, currentPlayer.getConnectionUsage().getOrDefault(stratClassName,0));
            printString(0, currentRow++, text, DEFAULT_TEXT_COLOR);
        }
        return currentRow;
    }

    private int drawMovieInputLayout(int startRow, TerminalSize size) throws IOException {
        int currentRow = startRow;
        if (currentRow >= size.getRows() -1) return currentRow;
        printString(0, currentRow++, "Enter Movie Title:", DEFAULT_TEXT_COLOR);

        if (currentRow >= size.getRows()) return currentRow;
        String inputDisplay = "> " + currentInput.toString();
        printString(0, currentRow, inputDisplay, DEFAULT_TEXT_COLOR);
        if (screen != null) {
            screen.setCharacter(("> ".length() + currentInput.length()), currentRow,
                    new TextCharacter('|', DEFAULT_TEXT_COLOR, TextColor.ANSI.BLACK));
        }
        return currentRow + 1;
    }

    private int drawSuggestionsLayout(int startRow, TerminalSize size) throws IOException {
        if (currentMovieSuggestions.isEmpty()) return startRow;
        int currentRow = startRow;

        if (currentRow >= size.getRows() -1) return currentRow;
        printString(2, currentRow++, "Suggestions:", INFO_COLOR);

        for (int i = 0; i < currentMovieSuggestions.size() && i < MAX_SUGGESTIONS_DISPLAYED; i++) {
            if (currentRow >= size.getRows()) break; // Check before drawing text
            TextColor fgColor = (i == selectedSuggestionIndex) ? TextColor.ANSI.BLACK : DEFAULT_TEXT_COLOR;
            TextColor bgColor = (i == selectedSuggestionIndex) ? SELECTED_SUGGESTION_BG : TextColor.ANSI.BLACK;
            String suggestionText = "  " + currentMovieSuggestions.get(i);

            printString(2, currentRow, suggestionText.substring(0, Math.min(suggestionText.length(), size.getColumns() - 2)), fgColor, bgColor);
            // Clear rest of line for selected suggestion for better visibility
            if (bgColor != TextColor.ANSI.BLACK) { // Only clear if background changed
                int startClearCol = 2 + suggestionText.length();
                for (int k = startClearCol; k < size.getColumns(); k++) {
                    screen.setCharacter(k, currentRow, TextCharacter.DEFAULT_CHARACTER.withBackgroundColor(bgColor));
                }
            }
            currentRow++;
        }
        return currentRow;
    }

    private int drawFeedbackMessageLayout(TerminalSize size, int startRow) throws IOException {
        if (!feedbackMessage.isEmpty()) {
            int actualFeedbackRow = Math.min(startRow, size.getRows() - 1);
            if (actualFeedbackRow < 0 || actualFeedbackRow >= size.getRows()) return startRow; // Bounds check

            // Clear the line first
            for (int i = 0; i < size.getColumns(); i++) {
                printString(i, actualFeedbackRow, " ", DEFAULT_TEXT_COLOR, TextColor.ANSI.BLACK);
            }
            // Truncate feedback message if too long
            String msgToPrint = feedbackMessage;
            if (msgToPrint.length() > size.getColumns()) {
                msgToPrint = msgToPrint.substring(0, size.getColumns() - 3) + "...";
            }
            printString(0, actualFeedbackRow, msgToPrint, DEFAULT_TEXT_COLOR);
            return actualFeedbackRow + 1;
        }
        return startRow;
    }

    private void drawGameHistoryLayout(TerminalSize size, int startRow) throws IOException {
        if (gameController == null) return;
        List<GameController.GameMove> detailedHistory = gameController.getDetailedGameHistory();
        if (detailedHistory.isEmpty()) return;

        int actualStartRow = Math.max(startRow, 0);
        if (actualStartRow >= size.getRows()) return;

        if (actualStartRow -1 >=0 && actualStartRow -1 < size.getRows()) {
            printString(0, actualStartRow -1, "--- Recent Moves (Last " + MAX_HISTORY_DISPLAYED + ") ---", INFO_COLOR);
        }

        int linesDrawn = 0;
        // Iterate from most recent, but take last MAX_HISTORY_DISPLAYED from the *end* of the list.
        int historySize = detailedHistory.size();
        int startIndex = Math.max(0, historySize - MAX_HISTORY_DISPLAYED);

        for (int i = startIndex; i < historySize; i++) {
            if (actualStartRow + linesDrawn >= size.getRows() -1 && linesDrawn > 0) break;

            GameController.GameMove move = detailedHistory.get(i);
            Movie movie = move.movie;
            if (movie == null) continue;

            String playerPrefix = move.player != null ? move.player.getPlayerName() + ": " :
                    (move.linkReason.equals("Initial Game Movie") ? "Start: " : "Unknown: ");

            String movieLine = String.format("%s (%d)", movie.getTitle(), movie.getYear());
            String genres = " G: " + movie.getGenres().stream().limit(2).collect(Collectors.joining(", "));
            if (movie.getGenres().size() > 2) genres += ", etc.";

            String fullMovieDisplay = playerPrefix + movieLine + genres;
            printString(0, actualStartRow + linesDrawn++, fullMovieDisplay, MOVIE_HISTORY_COLOR);

            if (actualStartRow + linesDrawn >= size.getRows() -1 ) break;

            if (move.linkStrategyName != null && !move.linkStrategyName.equals("N/A") &&
                    !move.linkReason.equals("Initial Game Movie") &&
                    !move.linkReason.equals("Starts the chain") &&
                    move.linkReason != null && !move.linkReason.startsWith("N/A")) {
                String linkLine = String.format("  └─ Via %s: %s", move.linkStrategyName.replace("LinkStrategy",""), move.linkReason);
                printString(0, actualStartRow + linesDrawn++, linkLine, LINK_INFO_COLOR);
            }
            if (linesDrawn >= MAX_HISTORY_DISPLAYED * 2) break; // Limit lines drawn to avoid overflow
        }
    }

    private void printString(int x, int y, String text, TextColor color) throws IOException {
        printString(x, y, text, color, TextColor.ANSI.BLACK);
    }
    private void printString(int x, int y, String text, TextColor fgColor, TextColor bgColor) throws IOException {
        if (screen == null || text == null) return;
        TerminalSize terminalSize = screen.getTerminalSize();
        if (terminalSize == null || y < 0 || y >= terminalSize.getRows()) return;

        for (int i = 0; i < text.length(); i++) {
            int currentX = x + i;
            if (currentX >= 0 && currentX < terminalSize.getColumns()) {
                try {
                    screen.setCharacter(currentX, y, new TextCharacter(
                            text.charAt(i), fgColor, bgColor
                    ));
                } catch (Exception e) { /* ignore drawing error */ }
            } else { break; }
        }
    }

    private void printStringCentered(int y, String text, TextColor color) throws IOException {
        if (screen == null || text == null) return;
        TerminalSize size = screen.getTerminalSize();
        if (size == null) return;
        int x = (size.getColumns() - text.length()) / 2;
        if (x < 0) x = 0;
        printString(x, y, text, color);
    }

    public void shutdown() throws IOException {
        System.out.println("GameView shutdown initiated...");
        stopPlayerTurnTimer();
        if (turnScheduler != null && !turnScheduler.isShutdown()) {
            turnScheduler.shutdownNow();
            try {
                if (!turnScheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    System.err.println("Warning: Timer scheduler did not terminate cleanly after 500ms.");
                } else {
                    System.out.println("Timer scheduler terminated.");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                turnScheduler.shutdownNow();
                System.err.println("Timer scheduler shutdown interrupted.");
            }
        } else {
            System.out.println("Timer scheduler already shutdown or null.");
        }
        if (screen != null) {
            try { screen.close(); System.out.println("Lanterna screen closed."); }
            catch (IOException e) { System.err.println("Error closing Lanterna screen: " + e.getMessage()); }
            screen = null;
        }
        if (terminal != null) {
            try { terminal.close(); System.out.println("Lanterna terminal closed."); }
            catch (IOException e) { System.err.println("Error closing Lanterna terminal: " + e.getMessage()); }
            terminal = null;
        }
        System.out.println("GameView shutdown completed.");
    }
}
