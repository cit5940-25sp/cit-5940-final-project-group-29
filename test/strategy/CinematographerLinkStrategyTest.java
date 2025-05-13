package strategy;

import model.Movie;
import model.Person;
import model.PersonRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CinematographerLinkStrategyTest {

    /**
     * Tests that two movies with the same cinematographer return true for isValidLink.
     */
    @Test
    void testValidLinkWhenSharedCinematographer() {
        Movie a = new Movie("A", 2000);
        Movie b = new Movie("B", 2001);
        Person c = new Person("Jane Doe", PersonRole.CINEMATOGRAPHER);
        a.addCinematographer(c);
        b.addCinematographer(c);

        CinematographerLinkStrategy strat = new CinematographerLinkStrategy();
        assertTrue(strat.isValidLink(a, b));
        assertTrue(strat.getReason(a, b).contains("Jane Doe"));
    }

    /**
     * Tests that movies with different cinematographers return false.
     */
    @Test
    void testInvalidLinkWithoutSharedCinematographer() {
        Movie a = new Movie("A", 2000);
        Movie b = new Movie("B", 2001);
        a.addCinematographer(new Person("X", PersonRole.CINEMATOGRAPHER));
        b.addCinematographer(new Person("Y", PersonRole.CINEMATOGRAPHER));

        CinematographerLinkStrategy strat = new CinematographerLinkStrategy();
        assertFalse(strat.isValidLink(a, b));
    }
}
