package strategy;

import model.Movie;
import model.Person;
import model.PersonRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link strategy.ActorLinkStrategy} class.
 * <p>
 * These tests verify the behavior of the isValidLink and getReason methods
 * when determining whether two movies share at least one actor.
 */
public class ActorLinkStrategyTest {

    /**
     * Tests that two movies with the same actor are considered a valid link.
     * Also checks that the reason returned includes the shared actor's name.
     */
    @Test
    void testIsValidLink_sharedActor_returnsTrue() {
        Movie m1 = new Movie("Movie One", 2001);
        Movie m2 = new Movie("Movie Two", 2002);

        Person sharedActor = new Person("Emma Stone", PersonRole.ACTOR);
        m1.addActor(sharedActor);
        m2.addActor(sharedActor);

        ActorLinkStrategy strategy = new ActorLinkStrategy();
        assertTrue(strategy.isValidLink(m1, m2));
        assertEquals("Shared actor: Emma Stone", strategy.getReason(m1, m2));
    }

    /**
     * Tests that two movies with different actors are not considered a valid link.
     * Also checks that the reason returned indicates no shared actors.
     */
    @Test
    void testIsValidLink_noSharedActor_returnsFalse() {
        Movie m1 = new Movie("Movie One", 2001);
        Movie m2 = new Movie("Movie Two", 2002);

        m1.addActor(new Person("Tom Hanks", PersonRole.ACTOR));
        m2.addActor(new Person("Meryl Streep", PersonRole.ACTOR));

        ActorLinkStrategy strategy = new ActorLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared actors", strategy.getReason(m1, m2));
    }

    /**
     * Tests that two movies with empty actor lists are not considered a valid link.
     * Also checks that the reason returned indicates no shared actors.
     */
    @Test
    void testIsValidLink_emptyActorLists_returnsFalse() {
        Movie m1 = new Movie("Empty Cast 1", 1999);
        Movie m2 = new Movie("Empty Cast 2", 2000);

        ActorLinkStrategy strategy = new ActorLinkStrategy();
        assertFalse(strategy.isValidLink(m1, m2));
        assertEquals("No shared actors", strategy.getReason(m1, m2));
    }
}
