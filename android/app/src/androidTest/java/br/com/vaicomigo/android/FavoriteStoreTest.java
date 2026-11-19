package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FavoriteStoreTest {
    private SharedPreferences preferences;
    private FavoriteStore store;

    @Before
    public void setUp() {
        preferences = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("favorite-store-test-only", Context.MODE_PRIVATE);
        assertTrue(preferences.edit().clear().commit());
        store = new FavoriteStore(preferences);
    }

    @Test
    public void createUpdateReloadAndRemovePreserveIdentity() {
        store.save(new FavoriteDestination("test-id", " Casa de teste ", " Rua Fictícia, 10, Cidade Exemplo "));
        FavoriteStore reopened = new FavoriteStore(preferences);
        assertEquals("Casa de teste", reopened.load().get(0).name);
        assertEquals("Rua Fictícia, 10, Cidade Exemplo", reopened.load().get(0).address);
        reopened.save(new FavoriteDestination("test-id", "Clínica de teste", "Avenida Fictícia, 20"));
        assertEquals(1, store.load().size());
        assertEquals("Clínica de teste", store.load().get(0).name);
        reopened.remove("test-id");
        assertTrue(store.load().isEmpty());
    }

    @Test
    public void sixthDestinationIsRejectedButEditingIsAllowed() {
        for (int index = 0; index < 5; index++) {
            store.save(new FavoriteDestination("id-" + index, "Teste " + index, "Rua Fictícia, 10"));
        }
        assertThrows(IllegalStateException.class, () -> store.save(
                new FavoriteDestination("sixth", "Outro teste", "Rua Fictícia, 20")));
        store.save(new FavoriteDestination("id-0", "Teste editado", "Rua Fictícia, 30"));
        assertEquals(5, store.load().size());
        assertEquals("Teste editado", store.load().get(0).name);
        store.remove("id-1");
        store.save(new FavoriteDestination("replacement", "Novo teste", "Rua Fictícia, 40"));
        assertEquals(5, store.load().size());
    }

    @Test
    public void invalidFieldsAreRejectedBeforePersistence() {
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("id", " ", "Rua Fictícia"));
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("id", "Casa", " "));
        assertThrows(IllegalArgumentException.class, () -> new FavoriteDestination("id", new String(new char[41]).replace('\0', 'a'), "Rua Fictícia"));
        assertTrue(store.load().isEmpty());
    }

    @Test
    public void damagedDataIsReportedWithoutOverwritingIt() {
        assertTrue(preferences.edit().putString("destinations", "damaged").commit());
        assertThrows(IllegalStateException.class, store::load);
        assertThrows(IllegalStateException.class, () -> store.save(
                new FavoriteDestination("test", "Casa de teste", "Rua Fictícia, 10")));
        assertEquals("damaged", preferences.getString("destinations", ""));
    }

    @Test
    public void wrongPreferenceTypeIsReportedWithoutOverwritingIt() {
        assertTrue(preferences.edit().putInt("destinations", 42).commit());
        assertThrows(IllegalStateException.class, store::load);
        assertThrows(IllegalStateException.class, () -> store.save(
                new FavoriteDestination("test", "Casa de teste", "Rua Fictícia, 10")));
        assertEquals(42, preferences.getInt("destinations", 0));
    }

    @Test
    public void recoveryRemovesOnlyUnreadableFavoritesAndAllowsNewOnes() {
        assertTrue(preferences.edit().putString("destinations", "damaged")
                .putString("unrelated-test-key", "keep").commit());
        store.discardUnreadableFavorites();
        assertFalse(preferences.contains("destinations"));
        assertEquals("keep", preferences.getString("unrelated-test-key", ""));
        assertTrue(store.load().isEmpty());
        store.save(new FavoriteDestination("new-test", "Novo lugar", "Rua Fictícia, 20"));
        assertEquals("new-test", new FavoriteStore(preferences).load().get(0).id);
    }

    @Test
    public void recoveryAlsoHandlesWrongPreferenceType() {
        assertTrue(preferences.edit().putBoolean("destinations", true).commit());
        store.discardUnreadableFavorites();
        assertTrue(store.load().isEmpty());
    }

    @Test
    public void recoveryRefusesReadableOrEmptyFavorites() {
        assertThrows(IllegalStateException.class, store::discardUnreadableFavorites);
        store.save(new FavoriteDestination("keep", "Casa de teste", "Rua Fictícia, 10"));
        String original = preferences.getString("destinations", "");
        assertThrows(IllegalStateException.class, store::discardUnreadableFavorites);
        assertEquals(original, preferences.getString("destinations", ""));
    }
}
