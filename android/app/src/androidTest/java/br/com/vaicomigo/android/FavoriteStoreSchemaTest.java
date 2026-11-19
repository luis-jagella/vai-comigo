package br.com.vaicomigo.android;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Synthetic, isolated storage fixtures. Never modifies the user's favorites. */
@RunWith(AndroidJUnit4.class)
public class FavoriteStoreSchemaTest {
    private SharedPreferences preferences;
    private FavoriteStore store;

    @Before
    public void setUp() {
        preferences = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("favorite-schema-test-only", Context.MODE_PRIVATE);
        assertTrue(preferences.edit().clear().commit());
        store = new FavoriteStore(preferences);
    }

    @Test
    public void nonStringFieldsAreRejectedWithoutCoercionOrOverwrite() throws Exception {
        Object[] invalid = {42, true, JSONObject.NULL,
                new JSONArray().put("synthetic"), new JSONObject().put("synthetic", true)};
        for (String field : new String[]{"id", "name", "address"}) {
            for (Object value : invalid) {
                assertUnreadablePreserved(new JSONArray().put(fixture().put(field, value)));
            }
        }
    }

    @Test
    public void missingRequiredFieldsAreRejectedWithoutOverwrite() throws Exception {
        for (String field : new String[]{"id", "name", "address"}) {
            JSONObject item = fixture();
            item.remove(field);
            assertUnreadablePreserved(new JSONArray().put(item));
        }
    }

    @Test
    public void duplicateIdentitiesAreRejectedWithoutOverwrite() throws Exception {
        assertUnreadablePreserved(new JSONArray().put(fixture()).put(fixture()));
    }

    @Test
    public void oversizedCollectionIsRejectedWithoutOverwrite() throws Exception {
        JSONArray values = new JSONArray();
        for (int index = 0; index <= FavoriteStore.MAX_FAVORITES; index++) {
            values.put(fixture().put("id", "synthetic-" + index));
        }
        assertUnreadablePreserved(values);
    }

    @Test
    public void stringsThatResembleOtherJsonTypesRemainValid() throws Exception {
        JSONArray values = new JSONArray().put(fixture().put("id", "42")
                .put("name", "null").put("address", " true "));
        assertTrue(preferences.edit().putString("destinations", values.toString()).commit());
        FavoriteDestination result = store.load().get(0);
        assertEquals("42", result.id);
        assertEquals("null", result.name);
        assertEquals("true", result.address);
        store.save(new FavoriteDestination("42", "Casa de teste", "Rua Fictícia, 10"));
        assertEquals(1, store.load().size());
        assertEquals("Casa de teste", store.load().get(0).name);
    }

    @Test
    public void malformedFieldsAreDiscardedOnlyThroughExplicitRecovery() throws Exception {
        assertUnreadablePreserved(new JSONArray().put(fixture().put("address", JSONObject.NULL)));
        store.discardUnreadableFavorites();
        assertFalse(preferences.contains("destinations"));
        assertEquals("keep", preferences.getString("unrelated-test-key", ""));
        store.save(new FavoriteDestination("new-test", "Casa de teste", "Rua Fictícia, 20"));
        assertEquals("new-test", store.load().get(0).id);
    }

    private JSONObject fixture() throws Exception {
        return new JSONObject().put("id", "synthetic-id")
                .put("name", "Casa de teste").put("address", "Rua Fictícia, 10");
    }

    private void assertUnreadablePreserved(JSONArray values) {
        String original = values.toString();
        assertTrue(preferences.edit().putString("destinations", original)
                .putString("unrelated-test-key", "keep").commit());
        assertThrows(IllegalStateException.class, store::load);
        assertThrows(IllegalStateException.class, () -> store.save(
                new FavoriteDestination("other-test", "Outro teste", "Rua Fictícia, 30")));
        assertThrows(IllegalStateException.class, () -> store.remove("synthetic-id"));
        assertEquals(original, preferences.getString("destinations", ""));
        assertEquals("keep", preferences.getString("unrelated-test-key", ""));
    }
}
