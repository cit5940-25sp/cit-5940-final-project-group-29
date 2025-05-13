package strategy;

import model.Movie;

/**
 * Interface for defining different strategies to determine if two movies are connected.
 */
public interface ILinkStrategy {

    /**
     * Checks if two movies are considered connected under this strategy.
     * @param from the source movie
     * @param to the target movie
     * @return true if the movies are connected, false otherwise
     */
    boolean isValidLink(Movie from, Movie to);

    /**
     * Provides a textual explanation of why two movies are linked.
     * @param from the source movie
     * @param to the target movie
     * @return a string explaining the connection
     */
    String getReason(Movie from, Movie to);
}