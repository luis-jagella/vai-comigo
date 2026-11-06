package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import org.junit.Test;

public class ResolvedPlaceTest {
    @Test public void invalidCoordinatesAreRejected() {
        for (double latitude : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -90.01, 90.01}) {
            assertThrows(IllegalArgumentException.class, () -> new ResolvedPlace(latitude, 0, "Lugar fictício"));
        }
        for (double longitude : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, -180.01, 180.01}) {
            assertThrows(IllegalArgumentException.class, () -> new ResolvedPlace(0, longitude, "Lugar fictício"));
        }
    }

    @Test public void finiteBoundariesAndZeroAreAllowedWithoutGuessingMeaning() {
        assertEquals(90, new ResolvedPlace(90, -180, "Limite fictício").latitude, 0);
        assertEquals(180, new ResolvedPlace(-90, 180, "Limite fictício").longitude, 0);
        assertEquals(0, new ResolvedPlace(0, 0, "Ponto fictício").longitude, 0);
    }

    @Test public void blankMissingOrOversizedLabelsAreRejected() {
        for (String label : new String[]{null, " ", "\u00a0", new String(new char[501]).replace('\0', 'x')}) {
            assertThrows(IllegalArgumentException.class, () -> new ResolvedPlace(0, 0, label));
        }
    }

    @Test public void normalizedLabelPreservesUnicodeAndReservedCharacters() {
        assertEquals("Praça fictícia & #10?", new ResolvedPlace(-23.55, -46.63,
                "\u00a0Praça fictícia & #10? ").label);
    }
}
