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
}
