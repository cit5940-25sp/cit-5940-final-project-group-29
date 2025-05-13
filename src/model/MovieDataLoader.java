package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * MovieDataLoader is responsible for parsing TMDB CSV files and constructing
 * a list of Movie objects with their associated genres, cast, and crew.
 */
public class MovieDataLoader {

    /**
     * Loads movies and their credits from the specified CSV files.
     * <p>
     * This method reads the header line of each CSV to determine column indices
     * dynamically, then parses each subsequent line into Movie instances.
     *
     * @param moviesCsvPath   Path to the TMDB movies CSV file
     * @param creditsCsvPath  Path to the TMDB credits CSV file
     * @return List of Movie objects populated with genres, actors, directors, etc.
     * @throws IOException if an I/O error occurs while reading the files
     */
    public static List<Movie> loadMovies(
            String moviesCsvPath, String creditsCsvPath
    ) throws IOException {
        Map<Integer, Movie> movieIdToMovie = new HashMap<>();

        // Parse movies CSV
        try (BufferedReader br = new BufferedReader(new FileReader(moviesCsvPath))) {
            String headerLine = br.readLine();
            Map<String, Integer> indexMap = buildHeaderIndexMap(headerLine);
            int requiredFieldCount = Collections.max(indexMap.values()) + 1;

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = parseCsvLine(line);

                // Skip lines with missing fields
                if (fields.length < requiredFieldCount) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(fields[indexMap.get("id")]);
                    String title = fields[indexMap.get("title")];
                    int year = parseYear(fields[indexMap.get("release_date")]);

                    Movie movie = new Movie(title, year);

                    String genresField = fields[indexMap.get("genres")];
                    JSONArray genresArray = new JSONArray(genresField);
                    for (int i = 0; i < genresArray.length(); i++) {
                        JSONObject genreObj = genresArray.getJSONObject(i);
                        movie.addGenre(genreObj.getString("name"));
                    }

                    movieIdToMovie.put(id, movie);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        // Parse credits CSV
        try (BufferedReader br = new BufferedReader(new FileReader(creditsCsvPath))) {
            String headerLine = br.readLine();
            Map<String, Integer> indexMap = buildHeaderIndexMap(headerLine);
            int requiredFieldCount = Collections.max(indexMap.values()) + 1;

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = parseCsvLine(line);

                // Skip lines with missing fields
                if (fields.length < requiredFieldCount) {
                    continue;
                }

                try {
                    int movieId = Integer.parseInt(fields[indexMap.get("movie_id")]);
                    Movie movie = movieIdToMovie.get(movieId);
                    if (movie == null) continue;

                    String castField = fields[indexMap.get("cast")];
                    JSONArray castArray = new JSONArray(castField);
                    for (int i = 0; i < castArray.length(); i++) {
                        JSONObject castObj = castArray.getJSONObject(i);
                        String name = castObj.getString("name");
                        movie.addActor(new Person(name, PersonRole.ACTOR));
                    }

                    String crewField = fields[indexMap.get("crew")];
                    JSONArray crewArray = new JSONArray(crewField);
                    for (int i = 0; i < crewArray.length(); i++) {
                        JSONObject crewObj = crewArray.getJSONObject(i);
                        String job = crewObj.getString("job");
                        String name = crewObj.getString("name");
                        if ("Director".equalsIgnoreCase(job)) {
                            movie.addDirector(new Person(name, PersonRole.DIRECTOR));
                        } else if ("Writer".equalsIgnoreCase(job) || "Screenplay".equalsIgnoreCase(job)) {
                            movie.addWriter(new Person(name, PersonRole.WRITER));
                        } else if ("Original Music Composer".equalsIgnoreCase(job)) {
                            movie.addComposer(new Person(name, PersonRole.COMPOSER));
                        } else if ("Director of Photography".equalsIgnoreCase(job)
                                || "Cinematographer".equalsIgnoreCase(job)) {
                            movie.addCinematographer(new Person(name, PersonRole.CINEMATOGRAPHER));
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }

        return new ArrayList<>(movieIdToMovie.values());
    }


    /**
     * Builds a mapping from CSV header names to their column indices.
     *
     * @param headerLine The first line of the CSV file containing header names
     * @return Map of header name to column index
     */
    private static Map<String, Integer> buildHeaderIndexMap(String headerLine) {
        String[] headers = parseCsvLine(headerLine);
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            indexMap.put(headers[i], i);
        }
        return indexMap;
    }

    /**
     * Parses a single CSV line into fields, handling quoted values and RFC4180 ""-escape.
     *
     * @param line The raw CSV line
     * @return Array of field values with quotes and escapes resolved
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Extracts the year from a release date string in the format YYYY-MM-DD.
     *
     * @param releaseDate The release date string
     * @return The year as an integer, or 0 if unavailable or invalid
     */
    private static int parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
