package model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Loads movie data from TMDB CSV files using OpenCSV for robust parsing.
 * Constructs Movie objects with associated metadata including genres, cast, and crew.
 */
public class MovieDataLoader {

    /**
     * Loads movies from metadata and credits CSV files.
     *
     * @param moviesCsvPath  Path to movies metadata CSV
     * @param creditsCsvPath Path to movie credits CSV
     * @return List of fully populated Movie objects
     * @throws IOException  If file reading fails
     * @throws CsvException If CSV parsing fails
     */
    public static List<Movie> loadMovies(String moviesCsvPath, String creditsCsvPath)
            throws IOException, CsvException {

        Map<Integer, Movie> movieIdToMovie = new HashMap<>();

        // Process movies metadata
        try (CSVReader reader = new CSVReader(new FileReader(moviesCsvPath))) {
            String[] headers = reader.readNext();
            Map<String, Integer> indexMap = buildHeaderIndexMap(headers);

            String[] fields;
            while ((fields = reader.readNext()) != null) {
                try {
                    Movie movie = parseMovieRow(fields, indexMap);
                    movieIdToMovie.put(movie.getId(), movie);
                } catch (Exception e) {
                    System.err.println("Skipping invalid movie row: " + String.join(",", fields));
                    e.printStackTrace();
                }
            }
        }

        // Process credits metadata
        try (CSVReader reader = new CSVReader(new FileReader(creditsCsvPath))) {
            String[] headers = reader.readNext();
            Map<String, Integer> indexMap = buildHeaderIndexMap(headers);

            String[] fields;
            while ((fields = reader.readNext()) != null) {
                try {
                    enhanceMovieWithCredits(fields, indexMap, movieIdToMovie);
                } catch (Exception e) {
                    System.err.println("Skipping invalid credits row: " + String.join(",", fields));
                    e.printStackTrace();
                }
            }
        }

        return new ArrayList<>(movieIdToMovie.values());
    }

    /**
     * Parses a movie row and returns a Movie object.
     */
    private static Movie parseMovieRow(String[] fields, Map<String, Integer> indexMap) {
        int id = Integer.parseInt(getField(fields, indexMap, "id"));
        String title = getField(fields, indexMap, "title");
        int year = parseYear(getField(fields, indexMap, "release_date"));

        Movie movie = new Movie(title, year);
        movie.setId(id);

        // Parse genres JSON array
        String genresJson = cleanJson(getField(fields, indexMap, "genres"));
        JSONArray genresArray = new JSONArray(genresJson);
        for (int i = 0; i < genresArray.length(); i++) {
            JSONObject genre = genresArray.getJSONObject(i);
            if (genre.has("name")) {
                movie.addGenre(genre.getString("name"));
            }
        }

        return movie;
    }

    /**
     * Adds cast and crew members to an existing Movie object.
     */
    private static void enhanceMovieWithCredits(String[] fields,
                                                Map<String, Integer> indexMap,
                                                Map<Integer, Movie> movieMap) {
        int movieId = Integer.parseInt(getField(fields, indexMap, "movie_id"));
        Movie movie = movieMap.get(movieId);
        if (movie == null) return;

        // Add cast
        String castJson = cleanJson(getField(fields, indexMap, "cast"));
        JSONArray castArray = new JSONArray(castJson);
        for (int i = 0; i < castArray.length(); i++) {
            JSONObject member = castArray.getJSONObject(i);
            if (member.has("name")) {
                movie.addActor(new Person(member.getString("name"), PersonRole.ACTOR));
            }
        }

        // Add crew by role
        String crewJson = cleanJson(getField(fields, indexMap, "crew"));
        JSONArray crewArray = new JSONArray(crewJson);
        for (int i = 0; i < crewArray.length(); i++) {
            JSONObject member = crewArray.getJSONObject(i);
            if (!member.has("job") || !member.has("name")) continue;

            String job = member.getString("job").toLowerCase();
            String name = member.getString("name");

            if (job.contains("director")) {
                movie.addDirector(new Person(name, PersonRole.DIRECTOR));
            } else if (job.contains("writer") || job.contains("screenplay")) {
                movie.addWriter(new Person(name, PersonRole.WRITER));
            } else if (job.contains("composer")) {
                movie.addComposer(new Person(name, PersonRole.COMPOSER));
            } else if (job.contains("cinematographer") || job.contains("photography")) {
                movie.addCinematographer(new Person(name, PersonRole.CINEMATOGRAPHER));
            }
        }
    }

    /**
     * Retrieves a field value safely from the field array using a case-insensitive index map.
     */
    private static String getField(String[] fields, Map<String, Integer> indexMap, String fieldName) {
        Integer index = indexMap.get(fieldName.toLowerCase());
        if (index == null || index >= fields.length) {
            throw new IllegalArgumentException("Missing or invalid field: " + fieldName);
        }
        return fields[index];
    }

    /**
     * Converts the CSV header row into a lowercase key → column index map.
     */
    private static Map<String, Integer> buildHeaderIndexMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].toLowerCase(), i);
        }
        return map;
    }

    /**
     * Extracts the year from a YYYY-MM-DD formatted date string.
     */
    private static int parseYear(String date) {
        if (date == null || date.length() < 4) return 0;
        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (NumberFormatException e) {
            System.err.println("Invalid release date: " + date);
            return 0;
        }
    }

    /**
     * Cleans JSON string extracted from CSV by replacing double quotes used as escape.
     */
    private static String cleanJson(String raw) {
        return raw.replace("\"\"", "\"");
    }
}
