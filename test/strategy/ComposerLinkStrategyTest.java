package strategy;

import model.Movie;
import model.Person;
import model.PersonRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ComposerLinkStrategy}.
 * <p>
 * These tests validate correct behavior of isValidLink and getReason
 * when checking for shared composers between two movies.
 */
public class ComposerLinkStrategyTest {

    /**
     * Tests that a shared composer results in a valid link and correct reason message.
     */
    @Test
    void testIsValidLink_sharedComposer_returnsTrue() {
        Movie m1 = new Movie("Interstellar", 2014);
        Movie m2 = new Movie("Inception", 2010);

        Person hansZimmer = new Person("Hans Zimmer", PersonRole.COMPOSER);
        m1.addComposer(hansZimmer);
        m2.addComposer(hansZimmer);

        ComposerLinkStrategy strategy = new ComposerLinkStrategy();
        assertTrue(strategy.isValidLink(m1, m2));
        assertEquals("Shared composer: Hans Zimmer", strategy.getReason(m1, m2));
    }

    /**
     * Tests that different composers result in no valid link.
     */
    @Test
    void testIsValidLink_noSharedComposer_returnsFalse() {
        Movie m1 = new Movie("Movie A", 2005);
        Movie m2 = new Movie("Movie B", 2006);

        m1.addComposer(new Person("John Williams", PersonRole.COMPOSER));
        m2.addComposer(new Person("Howard Shore", PersonRole.COMPOSER));

        ComposerLinkStrategy strategy = new ComposerLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared composers", strategy.getReason(m1, m2));
    }

    /**
     * Tests empty composer lists result in no link.
     */
    @Test
    void testIsValidLink_emptyComposers_returnsFalse() {
        Movie m1 = new Movie("Silent Film", 1920);
        Movie m2 = new Movie("Mute Documentary", 1930);

        ComposerLinkStrategy strategy = new ComposerLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared composers", strategy.getReason(m1, m2));
    }
}
