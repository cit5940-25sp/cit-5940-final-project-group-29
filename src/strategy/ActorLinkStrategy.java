package strategy;

import model.Movie;
import model.Person;

/**
 * Link strategy where two movies are considered connected if they share at least one actor.
 */
public class ActorLinkStrategy implements ILinkStrategy {

    /**
     * Determines whether two movies share at least one actor.
     *
     * @param from the source movie
     * @param to the target movie
     * @return {@code true} if the movies share an actor, otherwise {@code false}
     */
    @Override
    public boolean isValidLink(Movie from, Movie to) {
        for (Person actor1 : from.getActors()) {
            for (Person actor2 : to.getActors()) {
                if (actor1.getName().equals(actor2.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a description explaining the actor link between two movies.
     *
     * @param from the source movie
     * @param to the target movie
     * @return a string explaining the shared actor if one exists, or a default message otherwise
     */
    @Override
    public String getReason(Movie from, Movie to) {
        for (Person actor1 : from.getActors()) {
            for (Person actor2 : to.getActors()) {
                if (actor1.getName().equals(actor2.getName())) {
                    return "Shared actor: " + actor1.getName();
                }
            }
        }
        return "No shared actors";
    }
}
