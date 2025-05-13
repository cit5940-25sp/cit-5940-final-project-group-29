package strategy;

import model.Movie;
import model.Person;
import model.PersonRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link DirectorLinkStrategy} class.
 * <p>
 * These tests verify the correct identification of shared directors
 * between two Movie objects using isValidLink and getReason.
 */
public class DirectorLinkStrategyTest {

    /**
     * Test that two movies with the same director return true and a proper reason.
     */
    @Test
    void testIsValidLink_sharedDirector_returnsTrue() {
        Movie m1 = new Movie("The Prestige", 2006);
        Movie m2 = new Movie("Inception", 2010);

        Person nolan = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        m1.addDirector(nolan);
        m2.addDirector(nolan);

        DirectorLinkStrategy strategy = new DirectorLinkStrategy();
        assertTrue(strategy.isValidLink(m1, m2));
        assertEquals("Shared director: Christopher Nolan", strategy.getReason(m1, m2));
    }

    /**
     * Test that two movies with different directors return false.
     */
    @Test
    void testIsValidLink_differentDirectors_returnsFalse() {
        Movie m1 = new Movie("E.T.", 1982);
        Movie m2 = new Movie("Titanic", 1997);

        m1.addDirector(new Person("Steven Spielberg", PersonRole.DIRECTOR));
        m2.addDirector(new Person("James Cameron", PersonRole.DIRECTOR));

        DirectorLinkStrategy strategy = new DirectorLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared director", strategy.getReason(m1, m2));
    }

    /**
     * Test behavior when one or both movies have no directors listed.
     */
    @Test
    void testIsValidLink_emptyDirectors_returnsFalse() {
        Movie m1 = new Movie("No Director 1", 2000);
        Movie m2 = new Movie("No Director 2", 2001);

        DirectorLinkStrategy strategy = new DirectorLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared director", strategy.getReason(m1, m2));
    }

    /**
     * Test that null movies return false safely.
     */
    @Test
    void testIsValidLink_nullMovie_returnsFalse() {
        Movie m1 = new Movie("Interstellar", 2014);
        Person nolan = new Person("Christopher Nolan", PersonRole.DIRECTOR);
        m1.addDirector(nolan);

        DirectorLinkStrategy strategy = new DirectorLinkStrategy();

        assertFalse(strategy.isValidLink(m1, null));
        assertFalse(strategy.isValidLink(null, m1));
        assertFalse(strategy.isValidLink(null, null));
    }
}
