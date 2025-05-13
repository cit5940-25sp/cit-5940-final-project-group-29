package strategy;

import model.Movie;
import model.Person;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategy that validates whether two movies share at least one cinematographer.
 */
public class CinematographerLinkStrategy implements ILinkStrategy {

    /**
     * Checks if two movies share at least one cinematographer by comparing their cinematographer sets.
     *
     * @param from the first movie
     * @param to the second movie
     * @return {@code true} if the movies share a cinematographer, otherwise {@code false}
     */
    @Override
    public boolean isValidLink(Movie from, Movie to) {
        return !Collections.disjoint(
                from.getCinematographers(),
                to.getCinematographers()
        );
    }

    /**
     * Returns a human-readable explanation describing the cinematographer link between two movies.
     *
     * @param from the first movie
     * @param to the second movie
     * @return a string stating the name of the shared cinematographer, or an exception if none found
     */
    @Override
    public String getReason(Movie from, Movie to) {
        Set<Person> common = from.getCinematographers().stream()
                .filter(to.getCinematographers()::contains)
                .collect(Collectors.toSet());

        if (!common.isEmpty()) {
            String name = common.iterator().next().getName();
            return "Shared cinematographer: " + name;
        } else {
            return "No shared cinematographers.";
        }
    }
}
