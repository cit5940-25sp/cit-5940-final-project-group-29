package model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MovieIndex class.
 */
class MovieIndexTest {

    /**
     * Tests that findMovieByTitle correctly finds a movie by its title,
     * and returns null when the movie is not found.
     */
    @Test
    void testFindMovieByTitle() {
        Movie movie = new Movie("Inception", 2010);
        MovieIndex index = new MovieIndex(List.of(movie));

        assertEquals(movie, index.findMovieByTitle("Inception"));
        assertNull(index.findMovieByTitle("Nonexistent"));
    }

    /**
     * Tests that findMoviesByPerson returns the correct set of movies
     * associated with a given person's name, and returns an empty set for unknown persons.
     */
    @Test
    void testFindMoviesByPerson() {
        Movie movie = new Movie("Interstellar", 2014);
        Person actor = new Person("Matthew McConaughey", PersonRole.ACTOR);
        movie.addActor(actor);
        MovieIndex index = new MovieIndex(List.of(movie));

        Set<Movie> movies = index.findMoviesByPerson("Matthew McConaughey");
        assertTrue(movies.contains(movie));
        assertTrue(index.findMoviesByPerson("Unknown Actor").isEmpty());
    }

    /**
     * Tests that movieExists correctly identifies existing and non-existing movies.
     */
    @Test
    void testMovieExists() {
        Movie movie = new Movie("Dunkirk", 2017);
        MovieIndex index = new MovieIndex(List.of(movie));

        assertTrue(index.movieExists("Dunkirk"));
        assertFalse(index.movieExists("Unknown Movie"));
    }

    /**
     * Tests that getAutocompleteSuggestions returns movies whose titles match the given prefix.
     */
    @Test
    void testGetAutocompleteSuggestions() {
        Movie movie1 = new Movie("Avatar", 2009);
        Movie movie2 = new Movie("Avengers: Endgame", 2019);
        Movie movie3 = new Movie("Titanic", 1997);

        MovieIndex index = new MovieIndex(List.of(movie1, movie2, movie3));

        List<String> suggestions = index.getAutocompleteSuggestions("Av");
        assertTrue(suggestions.contains("Avatar"));
        assertTrue(suggestions.contains("Avengers: Endgame"));
        assertFalse(suggestions.contains("Titanic"));
    }

    /**
     * Tests that MovieIndex.java covers edge cases
     */
    @Test
    void testEdgeCases() {
        MovieIndex index = new MovieIndex(List.of());

        assertNull(index.findMovieByTitle(null));
        assertNull(index.findMovieByTitle(""));
        assertTrue(index.getAutocompleteSuggestions("").isEmpty());
        assertTrue(index.getAutocompleteSuggestions(null).isEmpty());
        assertTrue(index.findMoviesByPerson("").isEmpty());
        assertTrue(index.findMoviesByPerson(null).isEmpty());
    }

    /**
     * Tests that movies can be found by cinematographer
     */
    @Test
    void testFindMoviesByCinematographer() {
        Movie a = new Movie("A", 2020);
        Person c = new Person("Roger Deakins", PersonRole.CINEMATOGRAPHER);
        a.addCinematographer(c);
        MovieIndex idx = new MovieIndex(List.of(a));

        Set<Movie> films = idx.findMoviesByPerson("Roger Deakins");
        assertEquals(1, films.size());
        assertTrue(films.contains(a));
    }

    /**
     * Tests that the getAllTitlesSorted method returns the correct list of movie titles.
     */
    @Test
    void testGetAllTitlesSorted() {
        Movie movie1 = new Movie("Avatar", 2009);
        Movie movie2 = new Movie("Avengers: Endgame", 2019);
        Movie movie3 = new Movie("Titanic", 1997);

        MovieIndex index = new MovieIndex(List.of(movie1, movie2, movie3));

        Set<String> sortedTitles = index.getAllTitlesSorted();

        // Verify the order and content of sorted titles
        assertEquals(3, sortedTitles.size());
        assertTrue(sortedTitles.contains("Avatar"));
        assertTrue(sortedTitles.contains("Avengers: Endgame"));
        assertTrue(sortedTitles.contains("Titanic"));

        // Check the order is case-insensitive and sorted
        String[] sortedArray = sortedTitles.toArray(new String[0]);
        assertEquals("Avatar", sortedArray[0]);
        assertEquals("Avengers: Endgame", sortedArray[1]);
        assertEquals("Titanic", sortedArray[2]);
    }

}