package strategy;

import model.Movie;
import model.Person;

/**
 * Link strategy where two movies are considered connected if they share at least one composer.
 */
public class ComposerLinkStrategy implements ILinkStrategy {

    /**
     * Checks whether two movies share any composer.
     *
     * @param from the source movie
     * @param to   the target movie
     * @return {@code true} if the movies share a composer, otherwise {@code false}
     */
    @Override
    public boolean isValidLink(Movie from, Movie to) {
        for (Person composer1 : from.getComposers()) {
            for (Person composer2 : to.getComposers()) {
                if (composer1.getName().equals(composer2.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Provides a human‑readable reason explaining the composer link.
     *
     * @param from the source movie
     * @param to   the target movie
     * @return a description of the shared composer, or a default message if none exists
     */
    @Override
    public String getReason(Movie from, Movie to) {
        for (Person composer1 : from.getComposers()) {
            for (Person composer2 : to.getComposers()) {
                if (composer1.getName().equals(composer2.getName())) {
                    return "Shared composer: " + composer1.getName();
                }
            }
        }
        return "No shared composers";
    }
}
