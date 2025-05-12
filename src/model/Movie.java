package model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a movie with its title, release year, genres, and associated people
 * such as actors, directors, writers, and composers.
 */
public class Movie {
    private String title;
    private int year;
    private Set<String> genres;
    private Set<Person> actors;
    private Set<Person> directors;
    private Set<Person> writers;
    private Set<Person> composers;
    private Set<Person> cinematographers;
    private int id;

    /**
     * Constructs a Movie object with the specified title and year.
     *
     * @param title the title of the movie
     * @param year  the release year of the movie
     */
    public Movie(String title, int year) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title;
        this.year = year;
        this.genres = new HashSet<>();
        this.actors = new HashSet<>();
        this.directors = new HashSet<>();
        this.writers = new HashSet<>();
        this.composers = new HashSet<>();
        this.cinematographers = new HashSet<>();
    }

    /**
     * Returns the title of the movie.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the release year of the movie.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the set of genres associated with the movie.
     *
     * @return a set of genres
     */
    public Set<String> getGenres() {
        return genres;
    }

    /**
     * Returns the set of actors associated with the movie.
     *
     * @return a set of actors
     */
    public Set<Person> getActors() {
        return actors;
    }

    /**
     * Returns the set of directors associated with the movie.
     *
     * @return a set of directors
     */
    public Set<Person> getDirectors() {
        return directors;
    }

    /**
     * Returns the set of writers associated with the movie.
     *
     * @return a set of writers
     */
    public Set<Person> getWriters() {
        return writers;
    }

    /**
     * Returns the set of composers associated with the movie.
     *
     * @return a set of composers
     */
    public Set<Person> getComposers() {
        return composers;
    }

    /**
     * Getter for cinematographers.
     * @return set of cinematographers
     */
    public Set<Person> getCinematographers() {
        return cinematographers;
    }

    /**
     * Gets the TMDB ID of the movie.
     * @return the movie ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the TMDB ID of the movie.
     * @param id the movie ID
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * Adds a cinematographer to this movie.
     * @param cinematographer the cinematographer to add
     */
    public void addCinematographer(Person cinematographer) {
        cinematographers.add(cinematographer);
    }

    /**
     * Adds a genre to the movie.
     *
     * @param genre the genre to add
     */
    public void addGenre(String genre) {
        genres.add(genre);
    }

    /**
     * Adds an actor to the movie.
     *
     * @param actor the actor to add
     */
    public void addActor(Person actor) {
        actors.add(actor);
    }

    /**
     * Adds a director to the movie.
     *
     * @param director the director to add
     */
    public void addDirector(Person director) {
        directors.add(director);
    }

    /**
     * Adds a writer to the movie.
     *
     * @param writer the writer to add
     */
    public void addWriter(Person writer) {
        writers.add(writer);
    }

    /**
     * Adds a composer to the movie.
     *
     * @param composer the composer to add
     */
    public void addComposer(Person composer) {
        composers.add(composer);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", title, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year &&
                Objects.equals(title, movie.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }
}