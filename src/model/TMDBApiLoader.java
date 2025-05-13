package model;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * TMDBApiLoader is responsible for fetching and parsing movie data from
 * The Movie Database (TMDB) API.
 * It supports:
 *   Fetching a single movie by its TMDB ID, including title, year,
 *       genres, cast, and crew
 *   Fetching multiple movies by a list of TMDB IDs
 * This class uses a {@link HttpService} to perform HTTP GET requests,
 * which can be overridden in unit tests to return stubbed JSON data
 * without real network calls.
 */
public class TMDBApiLoader {

    /**
     * An abstraction over HTTP GET operations.  Production code uses
     * {@link DefaultHttpService}, while tests can inject a stub.
     */
    public interface HttpService {
        /**
         * Performs an HTTP GET to the given URL and returns the response body
         * as a String.
         *
         * @param url the full URL to GET
         * @return the response body text
         * @throws Exception if an I/O or interruption occurs
         */
        String get(String url) throws Exception;
    }

    /**
     * Default implementation of {@link HttpService} that uses
     * {@link HttpClient} under the hood.
     */
    private static class DefaultHttpService implements HttpService {
        private final HttpClient client = HttpClient.newHttpClient();

        @Override
        public String get(String url) throws Exception {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }

    /**
     * The {@link HttpService} instance used for all API calls.
     * Defaults to {@link DefaultHttpService}.  Tests can override via
     * {@link #setHttpService(HttpService)}.
     */
    private static HttpService httpService = new DefaultHttpService();

    /**
     * API key loaded from {@code config.properties}.
     */
    private static final String API_KEY;

    /**
     * Base URL for TMDB API endpoints.
     */
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    static {
        // Load API key from external properties file
        Properties props = new Properties();
        try (var reader = new FileReader("config.properties")) {
            props.load(reader);
            API_KEY = props.getProperty("api_key");
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load API key from config.properties", e);
        }
    }

    /**
     * Allows injection of a custom {@link HttpService}, primarily for testing.
     *
     * @param service the {@link HttpService} to use for subsequent calls
     */
    public static void setHttpService(HttpService service) {
        httpService = Objects.requireNonNull(service, "HttpService must not be null");
    }

    /**
     * Fetches a single movie by its TMDB ID.
     *
     * @param movieId the TMDB movie ID
     * @return a {@link Movie} object populated with title, year, genres,
     *         cast, and crew information
     * @throws Exception if an I/O, JSON parsing, or any other error occurs
     */
    public static Movie fetchMovieById(int movieId) throws Exception {
        // 1) Retrieve basic movie info
        String movieJson = httpService.get(
                BASE_URL + "/movie/" + movieId + "?api_key=" + API_KEY);
        JSONObject obj = new JSONObject(movieJson);

        // Parse title and year
        String title = obj.getString("title");
        String releaseDate = obj.optString("release_date", "0000-00-00");
        int year = releaseDate.length() >= 4
                ? Integer.parseInt(releaseDate.substring(0, 4))
                : 0;

        Movie movie = new Movie(title, year);

        // Parse genres array
        JSONArray genresArray = obj.optJSONArray("genres");
        if (genresArray != null) {
            for (int i = 0; i < genresArray.length(); i++) {
                String genreName = genresArray
                        .getJSONObject(i)
                        .optString("name", null);
                if (genreName != null) {
                    movie.addGenre(genreName);
                }
            }
        }

        // 2) Retrieve and populate credits (cast & crew)
        populateCredits(movie, movieId);
        return movie;
    }

    /**
     * Fetches multiple movies in sequence by their TMDB IDs.
     *
     * @param movieIds list of TMDB movie IDs
     * @return a {@link List} of {@link Movie} objects in the same order
     * @throws Exception if any fetch operation fails
     */
    public static List<Movie> fetchMoviesByIds(List<Integer> movieIds) throws Exception {
        List<Movie> result = new ArrayList<>();
        for (int id : movieIds) {
            result.add(fetchMovieById(id));
        }
        return result;
    }

    /**
     * Populates the {@code movie} with cast and crew information by calling
     * the TMDB "/movie/{id}/credits" endpoint.
     *
     * @param movie   the {@link Movie} object to populate
     * @param movieId the TMDB movie ID
     * @throws Exception if an I/O or JSON parsing error occurs
     */
    private static void populateCredits(Movie movie, int movieId) throws Exception {
        String creditsJson = httpService.get(
                BASE_URL + "/movie/" + movieId + "/credits?api_key=" + API_KEY);
        JSONObject obj = new JSONObject(creditsJson);

        // Parse cast (up to 10 actors)
        JSONArray castArray = obj.optJSONArray("cast");
        if (castArray != null) {
            int limit = Math.min(castArray.length(), 10);
            for (int i = 0; i < limit; i++) {
                String actorName = castArray
                        .getJSONObject(i)
                        .optString("name", null);
                if (actorName != null) {
                    movie.addActor(new Person(actorName, PersonRole.ACTOR));
                }
            }
        }

        // Parse crew: director, writers, composer
        JSONArray crewArray = obj.optJSONArray("crew");
        if (crewArray != null) {
            for (int i = 0; i < crewArray.length(); i++) {
                JSONObject crewMember = crewArray.getJSONObject(i);
                String job  = crewMember.optString("job", "");
                String name = crewMember.optString("name", null);
                if (name == null) {
                    continue;
                }
                switch (job.toLowerCase(Locale.ROOT)) {
                    case "director":
                        movie.addDirector(new Person(name, PersonRole.DIRECTOR));
                        break;
                    case "writer":
                    case "screenplay":
                        movie.addWriter(new Person(name, PersonRole.WRITER));
                        break;
                    case "original music composer":
                        movie.addComposer(new Person(name, PersonRole.COMPOSER));
                        break;
                    case "director of photography":
                        case "cinematographer":
                        movie.addCinematographer(new Person(name, PersonRole.CINEMATOGRAPHER));
                        break;
                    default:
                        // ignore other roles
                }
            }
        }
    }
}
