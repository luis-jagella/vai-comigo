package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import org.junit.Test;

public class PickupSessionTest {
    private static final double LATITUDE = -23.55, LONGITUDE = -46.63;

    @Test public void startsWithoutRequestingLocation() {
        PickupSession session = new PickupSession();
        assertEquals(PickupSession.State.EXPLANATION, session.state());
        assertNull(session.position());
    }

    @Test public void permissionIsRequestedOnlyAfterExplicitChoice() {
        PickupSession session = new PickupSession();
        session.useLocation(false);
        assertEquals(PickupSession.State.PERMISSION, session.state());
        session.permissionResult(true);
        assertEquals(PickupSession.State.LOCATING, session.state());
        session.located(LATITUDE, LONGITUDE, 12, 1_000);
        session.reviewLocation();
        assertEquals(PickupSession.State.REVIEW, session.state());
        assertEquals(LATITUDE, session.position().latitude, 0);
    }

    @Test public void denialStillAllowsManualReview() {
        PickupSession session = new PickupSession();
        session.useLocation(false);
        session.permissionResult(false);
        assertEquals(PickupSession.State.DENIED, session.state());
        session.manual();
        session.reviewManual(" Rua Fictícia, 123, Cidade Exemplo ");
        assertEquals("Rua Fictícia, 123, Cidade Exemplo", session.address());
        assertEquals(PickupSession.State.REVIEW, session.state());
        assertNull(session.position());
    }

    @Test public void alreadyGrantedPermissionStillRequiresUserChoice() {
        PickupSession session = new PickupSession();
        session.permissionResult(true);
        assertEquals(PickupSession.State.EXPLANATION, session.state());
        session.useLocation(true);
        assertEquals(PickupSession.State.LOCATING, session.state());
    }

    @Test public void revocationErasesPreviouslyReviewedPosition() {
        PickupSession session = locatedSession();
        session.reviewLocation();
        session.permissionRevoked();
        assertNull(session.position());
        assertEquals(PickupSession.State.DENIED, session.state());
        assertThrows(IllegalStateException.class, session::reviewLocation);
    }

    @Test public void latePositionAfterCancellationIsIgnored() {
        PickupSession session = new PickupSession();
        session.useLocation(true);
        session.reset();
        session.located(LATITUDE, LONGITUDE, 10, 0);
        assertNull(session.position());
        assertEquals(PickupSession.State.EXPLANATION, session.state());
    }

    @Test public void switchingToManualErasesCoordinatesAndIgnoresLateCallbacks() {
        PickupSession session = locatedSession();
        session.manual();
        session.located(LATITUDE, LONGITUDE, 10, 0);
        session.unavailable();
        assertNull(session.position());
        assertEquals(PickupSession.State.MANUAL, session.state());
    }

    @Test public void leavingFlowAlsoErasesManualAddress() {
        PickupSession session = new PickupSession();
        session.manual();
        session.reviewManual("Rua Fictícia, 123");
        session.reset();
        assertNull(session.address());
        assertNull(session.position());
    }

    @Test public void staleInaccurateAndInvalidPositionsCannotBecomePickup() {
        assertFalse(PickupSession.usable(LATITUDE, LONGITUDE, 10, 120_001));
        assertFalse(PickupSession.usable(LATITUDE, LONGITUDE, 101, 0));
        assertFalse(PickupSession.usable(LATITUDE, LONGITUDE, 0, 0));
        assertFalse(PickupSession.usable(LATITUDE, LONGITUDE, Float.NaN, 0));
        assertFalse(PickupSession.usable(Double.NaN, LONGITUDE, 10, 0));
        assertFalse(PickupSession.usable(91, LONGITUDE, 10, 0));
        assertFalse(PickupSession.usable(LATITUDE, 181, 10, 0));
        assertFalse(PickupSession.usable(LATITUDE, LONGITUDE, 10, -1));
        assertTrue(PickupSession.usable(LATITUDE, LONGITUDE, 100, 120_000));
        PickupSession session = new PickupSession();
        session.useLocation(true);
        session.located(LATITUDE, LONGITUDE, 800, 0);
        assertNull(session.position());
        session.located(LATITUDE, LONGITUDE, 10, 1_000);
        assertNotNull(session.position());
    }

    @Test public void timeoutOffersAlternativeWithoutCoordinates() {
        PickupSession session = new PickupSession();
        session.useLocation(true);
        session.unavailable();
        assertEquals(PickupSession.State.UNAVAILABLE, session.state());
        assertNull(session.position());
        session.manual();
        session.reviewManual("Praça Fictícia, 10");
        assertNotNull(session.address());
    }

    @Test public void emptyOrOversizedManualAddressCannotBeReviewed() {
        PickupSession session = new PickupSession();
        session.manual();
        assertThrows(IllegalArgumentException.class, () -> session.reviewManual(" "));
        assertThrows(IllegalArgumentException.class, () -> session.reviewManual(new String(new char[241]).replace('\0', 'x')));
        assertEquals(PickupSession.State.MANUAL, session.state());
    }

    private PickupSession locatedSession() {
        PickupSession session = new PickupSession();
        session.useLocation(true);
        session.located(LATITUDE, LONGITUDE, 10, 0);
        return session;
    }
}
