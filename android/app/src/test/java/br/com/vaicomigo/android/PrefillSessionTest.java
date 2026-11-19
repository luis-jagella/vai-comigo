package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class PrefillSessionTest {
    private final ResolvedPlace first = new ResolvedPlace(-23.55, -46.63, "Rua Fictícia, 10");
    private final ResolvedPlace second = new ResolvedPlace(-23.56, -46.64, "Rua Fictícia, 20");

    @Test public void searchRequiresConsentStage() {
        PrefillSession session = new PrefillSession();
        assertEquals(PrefillSession.Stage.IDLE, session.stage());
        assertFalse(session.confirmed());
        assertThrows(IllegalStateException.class, session::startPickupSearch);
        session.begin();
        assertEquals(PrefillSession.Stage.CONSENT, session.stage());
        session.startPickupSearch();
        assertEquals(PrefillSession.Stage.SEARCHING_PICKUP, session.stage());
    }

    @Test public void evenSingleResultsRequireBothExplicitChoices() {
        PrefillSession session = searching();
        session.pickupResults(Collections.singletonList(first));
        assertNull(session.pickup());
        assertFalse(session.confirmed());
        session.choosePickup(0);
        assertSame(first, session.pickup());
        assertFalse(session.confirmed());
        session.destinationResults(Collections.singletonList(second));
        assertNull(session.destination());
        assertFalse(session.confirmed());
        session.chooseDestination(0);
        assertTrue(session.confirmed());
        assertSame(second, session.destination());
    }

    @Test public void ambiguousResultsKeepTheSelectedPointNotTheFirstOne() {
        PrefillSession session = searching();
        session.pickupResults(Arrays.asList(first, second));
        session.choosePickup(1);
        session.destinationResults(Arrays.asList(second, first));
        session.chooseDestination(1);
        assertSame(second, session.pickup());
        assertSame(first, session.destination());
    }

    @Test public void emptyDestinationDropsPartialPickupAndOffersFailure() {
        PrefillSession session = searching();
        session.pickupResults(Collections.singletonList(first));
        session.choosePickup(0);
        session.destinationResults(Collections.emptyList());
        assertEquals(PrefillSession.Stage.FAILED, session.stage());
        assertNull(session.pickup());
        assertNull(session.destination());
        assertFalse(session.confirmed());
    }

    @Test public void missingAndNullOnlyResultsFailWithoutASelection() {
        PrefillSession session = searching();
        session.pickupResults(null);
        assertEquals(PrefillSession.Stage.FAILED, session.stage());
        session.begin();
        session.startPickupSearch();
        session.pickupResults(Collections.singletonList(null));
        assertEquals(PrefillSession.Stage.FAILED, session.stage());
    }

    @Test public void choicesAreCopiedBoundedAndUnmodifiable() {
        PrefillSession session = searching();
        List<ResolvedPlace> source = new ArrayList<>(Arrays.asList(first, second, first, second));
        session.pickupResults(source);
        source.clear();
        assertEquals(3, session.choices().size());
        assertThrows(UnsupportedOperationException.class, () -> session.choices().clear());
    }

    @Test public void invalidChoiceDoesNotAdvanceOrConfirm() {
        PrefillSession session = searching();
        session.pickupResults(Collections.singletonList(first));
        assertThrows(IndexOutOfBoundsException.class, () -> session.choosePickup(1));
        assertEquals(PrefillSession.Stage.PICKUP_CHOICES, session.stage());
        assertNull(session.pickup());
    }

    @Test public void resetAndRestartDiscardCoordinatesAndChoices() {
        PrefillSession session = searching();
        session.pickupResults(Collections.singletonList(first));
        session.choosePickup(0);
        session.destinationResults(Collections.singletonList(second));
        session.chooseDestination(0);
        session.reset();
        assertEquals(PrefillSession.Stage.IDLE, session.stage());
        assertNull(session.pickup());
        assertNull(session.destination());
        assertTrue(session.choices().isEmpty());
        assertFalse(session.confirmed());
        session.begin();
        assertEquals(PrefillSession.Stage.CONSENT, session.stage());
        assertFalse(session.confirmed());
    }

    @Test public void lateOrOutOfOrderResultsCannotSkipStages() {
        PrefillSession session = searching();
        assertThrows(IllegalStateException.class, () -> session.destinationResults(Collections.singletonList(second)));
        session.reset();
        assertThrows(IllegalStateException.class, () -> session.pickupResults(Collections.singletonList(first)));
        assertThrows(IllegalStateException.class, () -> session.chooseDestination(0));
        assertFalse(session.confirmed());
    }

    private PrefillSession searching() {
        PrefillSession session = new PrefillSession();
        session.begin();
        session.startPickupSearch();
        return session;
    }
}
