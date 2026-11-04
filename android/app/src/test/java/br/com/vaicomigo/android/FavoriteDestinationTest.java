package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import org.junit.Test;

public class FavoriteDestinationTest {
    @Test public void unicodeWhitespaceCannotBecomeANameOrAddress() {
        for (String blank : new String[]{"", " \t\n", "\u00a0", "\u2003", "\u202f", " \u00a0\u2003\u202f "}) {
            assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", blank, "Rua Fictícia, 10"));
            assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", "Casa de teste", blank));
        }
    }

    @Test public void nullFieldsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", null, "Rua Fictícia, 10"));
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", "Casa de teste", null));
    }

    @Test public void unicodeBoundariesAreRemovedWithoutChangingInternalText() {
        FavoriteDestination value = new FavoriteDestination("test", "\u00a0 Casa\u00a0de teste \u2003",
                "\u202fRua Fictícia,  10\nCidade Exemplo\u00a0");
        assertEquals("Casa\u00a0de teste", value.name);
        assertEquals("Rua Fictícia,  10\nCidade Exemplo", value.address);
    }

    @Test public void limitsApplyAfterBoundaryNormalization() {
        String name = repeat('n', 40), address = repeat('a', 240);
        FavoriteDestination value = new FavoriteDestination("test", "\u2003" + name + "\u00a0", address);
        assertEquals(name, value.name);
        assertEquals(address, value.address);
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", name + "x", address));
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("test", name, address + "x"));
    }

    @Test public void ordinaryTextAndIdentityArePreserved() {
        FavoriteDestination value = new FavoriteDestination("stable-test-id", " Clínica fictícia ", " Rua Fictícia, 10 ");
        assertEquals("stable-test-id", value.id);
        assertEquals("Clínica fictícia", value.name);
        assertEquals("Rua Fictícia, 10", value.address);
    }

    private String repeat(char value, int count) {
        return new String(new char[count]).replace('\0', value);
    }
}
