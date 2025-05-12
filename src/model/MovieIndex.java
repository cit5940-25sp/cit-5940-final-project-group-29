package model;

import java.util.*;

/**
 * Provides indexing and fast lookup functionality for movies and people involved in movies.
 */
public class MovieIndex {
    private Map<String, Movie> titleToMovie;
    private Map<String, Set<Movie>> personToMovies;
    private Set<String> allTitlesSorted;

    /**
     * Constructs the MovieIndex based on a list of movies.
     *
     * @param movies the list of movies to index
     */
    public MovieIndex(List<Movie> movies) {
        this.titleToMovie = new HashMap<>();
        this.personToMovies = new HashMap<>();
        this.allTitlesSorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (Movie movie : movies) {
            // Index by title
            titleToMovie.put(movie.getTitle(), movie);
            allTitlesSorted.add(movie.getTitle());

            // Index by each actor
            for (Person actor : movie.getActors()) {
                personToMovies.computeIfAbsent(actor.getName(), k -> new HashSet<>()).add(movie);
            }

            // Index by each director
            for (Person director : movie.getDirectors()) {
                personToMovies.computeIfAbsent(director.getName(), k -> new HashSet<>()).add(movie);
            }

            // Index by each writer
            for (Person writer : movie.getWriters()) {
                personToMovies.computeIfAbsent(writer.getName(), k -> new HashSet<>()).add(movie);
            }

            // Index by each composer
            for (Person composer : movie.getComposers()) {
                personToMovies.computeIfAbsent(composer.getName(), k -> new HashSet<>()).add(movie);
            }

            // Index by each cinematographer
            for (Person cinematographer : movie.getCinematographers()) {
                personToMovies.computeIfAbsent(cinematographer.getName(),
                        k -> new HashSet<>()).add(movie);
            }
        }
    }

    /**
     * Finds a movie by its title.
     *
     * @param title the title of the movie
     * @return the Movie object, or null if not found
     */
    public Movie findMovieByTitle(String title) {
        if (title == null || title.isEmpty()) return null;
        return titleToMovie.get(title);
    }

    /**
     * Finds all movies associated with a given person's name.
     *
     * @param personName the name of the person
     * @return a set of movies that person has worked on, or empty set if not found
     */
    public Set<Movie> findMoviesByPerson(String personName) {
        return personToMovies.getOrDefault(personName, Collections.emptySet());
    }

    /**
     * Checks if a movie exists in the index.
     *
     * @param title the title of the movie
     * @return true if the movie exists, false otherwise
     */
    public boolean movieExists(String title) {
        return titleToMovie.containsKey(title);
    }

    /**
     * Provides autocomplete suggestions based on a given prefix of a movie title.
     *
     * @param prefix the prefix to search for
     * @return a list of movie titles matching the prefix
     */
    public List<String> getAutocompleteSuggestions(String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (String title : allTitlesSorted) {
            if (title.toLowerCase().startsWith(prefix.toLowerCase())) {
                suggestions.add(title);
            }
        }
        return suggestions;
    }

    /**
     * Randomly selects a movie from the indexed list.
     *
     * @return a random Movie, or null if no movies are indexed
     */
    public Movie getRandomMovie() {
        if (titleToMovie.isEmpty()) return null;
        List<Movie> allMovies = new ArrayList<>(titleToMovie.values());
        return allMovies.get(new Random().nextInt(allMovies.size()));
    }

}
