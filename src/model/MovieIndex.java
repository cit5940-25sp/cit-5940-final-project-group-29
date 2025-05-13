package model;

import java.util.*;

/**
 * Provides indexing and fast lookup functionality for movies and people involved in movies.
 * Includes methods for finding movies by title or person, checking existence,
 * providing autocomplete suggestions, and retrieving all indexed titles.
 */
public class MovieIndex {
    private Map<String, Movie> titleToMovie;
    private Map<String, Set<Movie>> personToMovies;
    private Set<String> allTitlesSorted;

    /**
     * Constructs the MovieIndex based on a list of movies.
     * Populates internal maps for efficient lookups by title and person,
     * and maintains a sorted set of all movie titles.
     *
     * @param movies the list of movies to index. Assumes the list is not null.
     * Handles empty list gracefully.
     */
    public MovieIndex(List<Movie> movies) {
        // Initialize the data structures
        this.titleToMovie = new HashMap<>();
        this.personToMovies = new HashMap<>();

        // Use TreeSet for automatic sorting (case-insensitive)
        this.allTitlesSorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // Process each movie in the input list
        for (Movie movie : movies) {
            if (movie == null || movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
                continue;  // Skip invalid movie entries
            }

            String title = movie.getTitle();

            // Index by title (handle potential duplicates if necessary, though map replaces)
            if (!titleToMovie.containsKey(title)) {
                titleToMovie.put(title, movie);
                allTitlesSorted.add(title);  // Add to the sorted set
            }

            // Index by people involved (Actors, Directors, etc.)
            indexPeople(movie.getActors(), movie);
            indexPeople(movie.getDirectors(), movie);
            indexPeople(movie.getWriters(), movie);
            indexPeople(movie.getComposers(), movie);
            indexPeople(movie.getCinematographers(), movie);
        }
        System.out.println("MovieIndex created with " + titleToMovie.size() + " unique movies indexed.");
    }

    /**
     * Helper method to index a set of people associated with a movie.
     * Adds the movie to the set of movies for each person.
     *
     * @param people The set of Person objects (e.g., actors, directors).
     * @param movie  The movie these people are associated with.
     */
    private void indexPeople(Set<Person> people, Movie movie) {
        if (people == null) {
            return;  // Skip if the person set is null
        }

        for (Person person : people) {
            if (person != null && person.getName() != null && !person.getName().trim().isEmpty()) {
                // computeIfAbsent gets the existing set or creates a new one if the key (person's name) is absent
                personToMovies.computeIfAbsent(person.getName(), k -> new HashSet<>()).add(movie);
            }
        }
    }

    /**
     * Finds a movie by its exact title (case-sensitive lookup based on map key).
     *
     * @param title the title of the movie to find.
     * @return the Movie object if found, or null if the title is null, empty, or not indexed.
     */
    public Movie findMovieByTitle(String title) {
        if (title == null || title.isEmpty()) {
            return null;
        }

        // Direct lookup using the map
        return titleToMovie.get(title);
    }

    /**
     * Finds all movies associated with a given person's name (case-sensitive lookup).
     *
     * @param personName the name of the person (e.g., actor, director).
     * @return a Set of Movie objects associated with the person. Returns an empty set
     * if the person is not found or has no associated movies in the index. Never returns null.
     */
    public Set<Movie> findMoviesByPerson(String personName) {
        if (personName == null || personName.isEmpty()) {
            return Collections.emptySet();  // Return empty set for invalid input
        }

        // getOrDefault ensures an empty set is returned if the key doesn't exist
        return personToMovies.getOrDefault(personName, Collections.emptySet());
    }

    /**
     * Checks if a movie with the given title exists in the index.
     *
     * @param title the title of the movie to check.
     * @return true if a movie with this exact title is indexed, false otherwise.
     */
    public boolean movieExists(String title) {
        if (title == null || title.isEmpty()) {
            return false;
        }
        return titleToMovie.containsKey(title);
    }

    /**
     * Provides autocomplete suggestions for movie titles based on a given prefix.
     * Performs a case-insensitive prefix match against the sorted list of titles.
     *
     * @param prefix The prefix string to search for.
     * @return A List of movie titles that start with the given prefix (case-insensitive).
     * Returns an empty list if the prefix is null, empty, or no matches are found.
     */
    public List<String> getAutocompleteSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();  // No suggestions for empty prefix
        }

        List<String> suggestions = new ArrayList<>();
        String lowerCasePrefix = prefix.toLowerCase();  // For case-insensitive comparison

        // Iterate through the pre-sorted set of titles
        for (String title : allTitlesSorted) {
            if (title.toLowerCase().startsWith(lowerCasePrefix)) {
                suggestions.add(title);
            }
        }
        return suggestions;
    }

    /**
     * Returns the set of all unique movie titles stored in the index,
     * sorted case-insensitively.
     * This is useful for selecting random movies or displaying all available titles.
     *
     * @return An unmodifiable Set of all indexed movie titles, sorted case-insensitively.
     * Returns an empty set if no movies were indexed.
     */
    public Set<String> getAllTitlesSorted() {
        // Return an unmodifiable view to prevent external modification
        return Collections.unmodifiableSet(this.allTitlesSorted);
    }
}