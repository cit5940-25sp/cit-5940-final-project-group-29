package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link model.Movie} class.
 */
public class MovieTest {

    /**
     * Tests that a Movie object is created with the correct title and year.
     */
    @Test
    public void testMovieCreation() {
        Movie movie = new Movie("Inception", 2010);
        assertEquals("Inception", movie.getTitle());
        assertEquals(2010, movie.getYear());
    }

    /**
     * Tests that a genre can be added to a Movie.
     */
    @Test
    public void testAddGenre() {
        Movie movie = new Movie("Inception", 2010);
        movie.addGenre("Sci-Fi");
        assertTrue(movie.getGenres().contains("Sci-Fi"));
    }

    /**
     * Tests that an actor can be added to a Movie.
     */
    @Test
    public void testAddActor() {
        Movie movie = new Movie("Inception", 2010);
        Person actor = new Person("Leonardo DiCaprio", PersonRole.ACTOR);
        movie.addActor(actor);
        assertTrue(movie.getActors().contains(actor));
    }

    /**
     * Tests that a director can be added to a Movie.
     */
    @Test
    public void testAddDirector() {
        Movie movie = new Movie("Inception", 2010);
        Person director = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        movie.addDirector(director);
        assertTrue(movie.getDirectors().contains(director));
    }

    /**
     * Tests that a writer can be added to a Movie.
     */
    @Test
    public void testAddWriter() {
        Movie movie = new Movie("Inception", 2010);
        Person writer = new Person("Jonathan Nolan", PersonRole.WRITER);
        movie.addWriter(writer);
        assertTrue(movie.getWriters().contains(writer));
    }

    /**
     * Tests that a composer can be added to a Movie.
     */
    @Test
    public void testAddComposer() {
        Movie movie = new Movie("Inception", 2010);
        Person composer = new Person("Hans Zimmer", PersonRole.COMPOSER);
        movie.addComposer(composer);
        assertTrue(movie.getComposers().contains(composer));
    }

    /**
     * Tests that the toString method returns the correct format.
     */
    @Test
    public void testToString() {
        Movie movie = new Movie("Inception", 2010);
        assertEquals("Inception (2010)", movie.toString());
    }

    /**
     * Tests that a cinematographer can be added to a Movie
     */
    @Test
    void testAddAndGetCinematographers() {
        Movie m = new Movie("Test", 2025);
        Person c = new Person("Roger Deakins", PersonRole.CINEMATOGRAPHER);
        m.addCinematographer(c);
        assertTrue(m.getCinematographers().contains(c));
    }

}
