package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FavoriteRecoveryTest {
    private static final String DAMAGED = "unreadable-test-fixture";
    private SharedPreferences preferences;
    private String original;
    private boolean snapshotTaken;

    @Before
    public void seedUnreadableFixture() {
        preferences = InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("favorites", Context.MODE_PRIVATE);
        original = preferences.getString("destinations", null);
        snapshotTaken = true;
        assertTrue(preferences.edit().putString("destinations", DAMAGED).commit());
    }

    @After
    public void restoreOriginalFavorites() {
        if (snapshotTaken) {
            assertTrue(preferences.edit().putString("destinations", original).commit());
        }
    }

    @Test
    public void retryAndCancelNeverDiscardData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.retry_favorites)).perform(scrollTo(), click());
            assertEquals(DAMAGED, preferences.getString("destinations", ""));
            onView(withText(R.string.restart_favorites)).perform(scrollTo(), click());
            onView(withText(R.string.restart_favorites_message)).check(matches(isDisplayed()));
            onView(withText(R.string.cancel)).perform(click());
            assertEquals(DAMAGED, preferences.getString("destinations", ""));
            onView(withText(R.string.restart_favorites_confirm)).check(doesNotExist());
        }
    }

    @Test
    public void confirmationAllowsRegisteringPlacesAgainAfterWrongType() {
        assertTrue(preferences.edit().putInt("destinations", 42).commit());
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.restart_favorites)).perform(scrollTo(), click());
            onView(withText(R.string.restart_favorites_confirm)).perform(click());
            assertFalse(preferences.contains("destinations"));
            onView(withText(R.string.add_favorite)).perform(scrollTo(), click());
            onView(withHint(R.string.name_hint)).perform(replaceText("Lugar fictício"), closeSoftKeyboard());
            onView(withHint(R.string.address_hint)).perform(replaceText("Rua Fictícia, 20"), closeSoftKeyboard());
            onView(withText(R.string.save_favorite)).perform(scrollTo(), click());
            assertEquals("Lugar fictício", new FavoriteStore(preferences).load().get(0).name);
        }
    }

    @Test
    public void recreationDismissesConfirmationWithoutDiscardingData() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.restart_favorites)).perform(scrollTo(), click());
            scenario.recreate();
            onView(withText(R.string.restart_favorites_confirm)).check(doesNotExist());
            assertEquals(DAMAGED, preferences.getString("destinations", ""));
            onView(withText(R.string.retry_favorites)).perform(scrollTo(), click());
        }
    }

    @Test
    public void retryReadsRepairedDataWithoutDiscardingIt() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            seedReadableFixture();
            onView(withText(R.string.retry_favorites)).perform(scrollTo(), click());
            onView(withText("Casa de teste\nRua Fictícia, 10")).check(matches(isDisplayed()));
            onView(withText(R.string.restart_favorites)).check(doesNotExist());
        }
    }

    @Test
    public void staleConfirmationCannotDiscardReadableFavorites() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.restart_favorites)).perform(scrollTo(), click());
            seedReadableFixture();
            String repaired = preferences.getString("destinations", "");
            onView(withText(R.string.restart_favorites_confirm)).perform(click());
            assertEquals(repaired, preferences.getString("destinations", ""));
            onView(withText("Casa de teste\nRua Fictícia, 10")).check(matches(isDisplayed()));
        }
    }

    private void seedReadableFixture() {
        assertTrue(preferences.edit().remove("destinations").commit());
        new FavoriteStore(preferences).save(
                new FavoriteDestination("recovery-test", "Casa de teste", "Rua Fictícia, 10"));
    }
}
