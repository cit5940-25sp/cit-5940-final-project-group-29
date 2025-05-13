package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link model.Person} class.
 */
public class PersonTest {

    /**
     * Tests that a Person object is created with the correct name and role.
     */
    @Test
    public void testPersonCreation() {
        Person person = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        assertEquals("Christopher Nolan", person.getName());
        assertEquals(PersonRole.DIRECTOR, person.getRole());
    }

    /**
     * Tests that the Person fields are not null after creation.
     */
    @Test
    public void testPersonFieldsNotNull() {
        Person person = new Person("Hans Zimmer", PersonRole.COMPOSER);
        assertNotNull(person.getName());
        assertNotNull(person.getRole());
    }

    /**
     * Tests the equality of two Person objects based on their name.
     */
    @Test
    public void testEquals() {
        Person person1 = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        Person person2 = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        Person person3 = new Person("Hans Zimmer", PersonRole.COMPOSER);

        // Testing equality based on name
        assertEquals(person1, person2); // Same name should be equal
        assertNotEquals(person1, person3); // Different names should not be equal
    }

    /**
     * Tests that the hashCode method works correctly for Person objects.
     */
    @Test
    public void testHashCode() {
        Person person1 = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        Person person2 = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        Person person3 = new Person("Hans Zimmer", PersonRole.COMPOSER);

        // Testing hashCode consistency
        assertEquals(person1.hashCode(), person2.hashCode()); // Same name should have the same hash code
        assertNotEquals(person1.hashCode(), person3.hashCode()); // Different names should have different hash codes
    }

    /**
     * Tests the toString method to verify the correct string format.
     */
    @Test
    public void testToString() {
        Person personWithRole = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        Person personWithoutRole = new Person("Hans Zimmer", null);

        // Testing toString with role
        assertEquals("Christopher Nolan (DIRECTOR)", personWithRole.toString());

        // Testing toString without role (should just return the name)
        assertEquals("Hans Zimmer", personWithoutRole.toString());
    }
}