package strategy;

import model.Movie;
import model.Person;
import java.util.Collections;

/**
 * Link strategy where two movies are connected if they share a common director.
 */
public class DirectorLinkStrategy implements ILinkStrategy {

    /**
     * Checks if two movies are considered connected under this strategy.
     * @param from the source movie
     * @param to the target movie
     * @return true if the movies are connected, false otherwise
     */
    @Override
    public boolean isValidLink(Movie from, Movie to) {
        if (from == null || to == null) {
            return false;
        }

        // Collections.disjoint is fast and readable for set intersection checks
        return !Collections.disjoint(from.getDirectors(), to.getDirectors());
    }

    /**
     *
     * @param from the source movie
     * @param to the target movie
     * @return the shared directors
     */
    @Override
    public String getReason(Movie from, Movie to) {
        // dir1 = 1st director
        // dir2 = 2nd director
        for (Person dir1: from.getDirectors()) {
            for (Person dir2: to.getDirectors()) {
                if (dir1.getName().equals(dir2.getName())) {
                    return "Shared director: " + dir1.getName();
                }
            }
        }
        return "No shared director";
    }
}