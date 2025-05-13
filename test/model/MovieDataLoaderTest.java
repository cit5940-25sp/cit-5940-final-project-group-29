package model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MovieDataLoader class.
 */
class MovieDataLoaderTest {

    /**
     * Tests that movies are loaded correctly from provided CSV files.
     */
    @Test
    void testLoadMovies() throws Exception {
        String moviesCsv = "test/data/test_movies.csv";
        String creditsCsv = "test/data/test_credits.csv";

        List<Movie> movies = MovieDataLoader.loadMovies(moviesCsv, creditsCsv);

        assertEquals(2, movies.size());

        Movie firstMovie = movies.get(0);
        Movie secondMovie = movies.get(1);

        assertEquals("Inception", firstMovie.getTitle());
        assertEquals(2010, firstMovie.getYear());
        assertTrue(firstMovie.getGenres().contains("Action"));
        assertTrue(firstMovie.getActors().stream().anyMatch(p -> p.getName().equals("Leonardo DiCaprio")));
        assertTrue(firstMovie.getDirectors().stream().anyMatch(p -> p.getName().equals("Christopher Nolan")));

        assertEquals("The Dark Knight", secondMovie.getTitle());
        assertEquals(2008, secondMovie.getYear());
        assertTrue(secondMovie.getGenres().contains("Drama"));
        assertTrue(secondMovie.getActors().stream().anyMatch(p -> p.getName().equals("Christian Bale")));
    }

    /**
     * Tests that loading from non-existent files throws IOException.
     */
    @Test
    void testLoadMoviesFileNotFound() {
        assertThrows(
                Exception.class,
                () -> MovieDataLoader.loadMovies("nonexistent.csv", "nonexistent.csv")
        );
    }
}
