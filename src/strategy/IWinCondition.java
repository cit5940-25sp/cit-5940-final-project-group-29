package strategy;

import model.Player;

/**
 * Interface for defining different winning conditions in the Movie Name Game.
 */
public interface IWinCondition {

    /**
     * Checks whether the player meets the win condition.
     * @param player the player to check
     * @return true if the player has won, false otherwise
     */
    boolean checkWin(Player player);

    /**
     * Provides a description of the win condition.
     * @return a string describing the win condition
     */
    String getDescription();
}
