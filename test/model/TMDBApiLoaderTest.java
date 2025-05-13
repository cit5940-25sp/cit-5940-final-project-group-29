package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TMDBApiLoader} using pure JUnit (no mock frameworks).
 * <p>
 * We inject a stubbed {@link TMDBApiLoader.HttpService} that returns
 * predefined JSON payloads based on the requested URL, enabling fast,
 * deterministic tests without real HTTP calls.
 */
class TMDBApiLoaderTest {

    // Sample JSON response for movie ID 1
    private static final String MOVIE1_JSON = """
        {
          "title": "Fight Club",
          "release_date": "1999-10-15",
          "genres": [
            {"name": "Drama"},
            {"name": "Thriller"}
          ]
        }
        """;

    // Sample JSON response for credits of movie ID 1
    private static final String CREDITS1_JSON = """
        {
          "cast": [
            {"name": "Edward Norton"},
            {"name": "Brad Pitt"}
          ],
          "crew": [
            {"job": "Director", "name": "David Fincher"},
            {"job": "Writer",   "name": "Jim Uhls"}
          ]
        }
        """;

    // Sample JSON response for movie ID 2
    private static final String MOVIE2_JSON = """
        {
          "title": "Movie B",
          "release_date": "2001-02-02",
          "genres": [
            {"name": "Comedy"}
          ]
        }
        """;

    // Sample JSON response for credits of movie ID 2
    private static final String CREDITS2_JSON = """
        {
          "cast": [
            {"name": "Actor B"}
          ],
          "crew": [
            {"job": "Director", "name": "Director B"}
          ]
        }
        """;

    /**
     * Before each test, replace the real HTTP service with a stub
     * that returns the appropriate JSON based on the URL.
     */
    @BeforeEach
    void setup() {
        TMDBApiLoader.setHttpService(url -> {
            if (url.contains("/movie/1?")) {
                return MOVIE1_JSON;
            } else if (url.contains("/movie/1/credits?")) {
                return CREDITS1_JSON;
            } else if (url.contains("/movie/2?")) {
                return MOVIE2_JSON;
            } else if (url.contains("/movie/2/credits?")) {
                return CREDITS2_JSON;
            }
            throw new IllegalArgumentException("Unexpected URL: " + url);
        });

        // Hard-code the API key for the test to bypass the need for config.properties
        System.setProperty("api_key", "YOUR_API_KEY");
    }

    /**
     * Verifies that fetching a single movie (ID=1) correctly parses:
     * <ul>
     *   <li>Title and release year</li>
     *   <li>Genre list</li>
     *   <li>Top cast members</li>
     *   <li>Director and writer from crew</li>
     * </ul>
     *
     * @throws Exception if the loader throws any unexpected exception
     */
    @Test
    void testFetchMovieById_parsesBasicAndCredits() throws Exception {
        Movie movie = TMDBApiLoader.fetchMovieById(1);

        // Title and year
        assertEquals("Fight Club", movie.getTitle());
        assertEquals(1999, movie.getYear());

        // Genres
        Set<String> genres = movie.getGenres();
        assertTrue(genres.contains("Drama"));
        assertTrue(genres.contains("Thriller"));

        // Cast
        assertTrue(
                movie.getActors().stream()
                        .anyMatch(p -> p.getName().equals("Edward Norton")),
                "Expected Edward Norton in cast");
        assertTrue(
                movie.getActors().stream()
                        .anyMatch(p -> p.getName().equals("Brad Pitt")),
                "Expected Brad Pitt in cast");

        // Crew: Director
        assertTrue(
                movie.getDirectors().stream()
                        .anyMatch(p -> p.getName().equals("David Fincher")),
                "Expected David Fincher as director");
        // Crew: Writer
        assertTrue(
                movie.getWriters().stream()
                        .anyMatch(p -> p.getName().equals("Jim Uhls")),
                "Expected Jim Uhls as writer");
    }

    /**
     * Verifies that {@link TMDBApiLoader#fetchMoviesByIds(List)}:
     * <ul>
     *   <li>Returns the correct number of movies</li>
     *   <li>Preserves the input order (ID=1 then ID=2)</li>
     * </ul>
     *
     * @throws Exception if the loader throws any unexpected exception
     */
    @Test
    void testFetchMoviesByIds_returnsCorrectOrder() throws Exception {
        List<Movie> movies = TMDBApiLoader.fetchMoviesByIds(List.of(1, 2));

        assertEquals(2, movies.size(), "Expected two movies in result");
        assertEquals("Fight Club", movies.get(0).getTitle(),
                "First movie should be Fight Club");
        assertEquals("Movie B", movies.get(1).getTitle(),
                "Second movie should be Movie B");
    }
}
