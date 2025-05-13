package strategy;

import model.Movie;
import model.Person;
import model.PersonRole;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Strategy that validates two movies share at least one writer.
 */
public class WriterLinkStrategy implements ILinkStrategy {
    /**
     * Checks if two movies are considered connected under this strategy.
     * @param from the source movie
     * @param to the target movie
     * @return true if the movies are connected, false otherwise
     */
    @Override
    public boolean isValidLink(Movie from, Movie to) {
        return !Collections.disjoint(
                from.getWriters(),
                to.getWriters()
        );
    }

    /**
     * Provides a textual explanation of why two movies are linked.
     * @param from the source movie
     * @param to the target movie
     * @return a string explaining the connection
     */
    @Override
    public String getReason(Movie from, Movie to) {
        Set<Person> common = from.getWriters().stream()
                .filter(to.getWriters()::contains)
                .collect(Collectors.toSet());

        if (!common.isEmpty()) {
            String name = common.iterator().next().getName();
            return "Shared writer: " + name;
        } else {
            return "No shared writers.";
        }
    }
}