package model;

import java.util.Objects;

/**
 * Represents a person
 * involved in movie production, such as an actor, director, writer, or composer.
 */
public class Person {
    private String name;
    private PersonRole role; // Role might be relevant for equality if a person can be an actor and director separately

    /**
     * Constructs a Person with a name and a role.
     *
     * @param name the name of the person
     * @param role the role of the person in the movie
     */
    public Person(String name, PersonRole role) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Person name cannot be null or empty.");
        }
        // Role can be null if not specified or if it's a general person object
        this.name = name.trim();
        this.role = role;
    }

    /**
     * Returns the name of the person.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the role of the person.
     *
     * @return the role, can be null.
     */
    public PersonRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return name + (role != null ? " (" + role + ")" : "");
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two Persons are considered equal if they have the same name.
     * Role is not considered for basic equality of the person entity,
     * as one person can have multiple roles across different contexts.
     * If role-specific equality is needed, a different method or comparator should be used.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     * {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(name, person.name);
    }

    /**
     * Returns a hash code value for the object.
     * This method is supported for the benefit of hash tables such as those provided by
     * {@link java.util.HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
