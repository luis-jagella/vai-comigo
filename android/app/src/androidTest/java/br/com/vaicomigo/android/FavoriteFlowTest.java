package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FavoriteFlowTest {
    private static final String NAME = "Laboratório fictício";
    private static final String ADDRESS = "Rua Fictícia, 123, Cidade Exemplo";

    private FavoriteStore store() {
        return new FavoriteStore(InstrumentationRegistry.getInstrumentation().getTargetContext()
                .getSharedPreferences("favorites", Context.MODE_PRIVATE));
    }

    @After
    public void removeOnlyTestFixture() {
        for (FavoriteDestination favorite : store().load()) {
            if (favorite.name.equals(NAME) || favorite.name.equals(NAME + " editado")) {
                store().remove(favorite.id);
            }
        }
    }

    @Test
    public void createRecreateSelectEditAndConfirmDeletion() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.add_favorite)).perform(scrollTo(), click());
            onView(withHint(R.string.name_hint)).perform(replaceText(NAME), closeSoftKeyboard());
            onView(withHint(R.string.address_hint)).perform(replaceText(ADDRESS), closeSoftKeyboard());
            scenario.recreate();
            onView(withHint(R.string.name_hint)).check(matches(withText(NAME)));
            onView(withHint(R.string.address_hint)).check(matches(withText(ADDRESS)));
            onView(withText(R.string.save_favorite)).perform(scrollTo(), click());
            scenario.recreate();
            onView(withText(NAME + "\n" + ADDRESS)).perform(scrollTo(), click());
            onView(withText(R.string.selected_title)).check(matches(isDisplayed()));
            onView(withText(ADDRESS)).check(matches(isDisplayed()));
            onView(withText(R.string.back_to_destinations)).perform(scrollTo(), click());
            onView(withText(R.string.manage_favorites)).perform(scrollTo(), click());
            onView(withText("Editar " + NAME)).perform(scrollTo(), click());
            onView(withHint(R.string.name_hint)).perform(replaceText(NAME + " editado"), closeSoftKeyboard());
            onView(withText(R.string.save_favorite)).perform(scrollTo(), click());
            assertTrue(store().load().stream().anyMatch(favorite -> favorite.name.equals(NAME + " editado")));
            onView(withText(R.string.manage_favorites)).perform(scrollTo(), click());
            onView(withText("Excluir " + NAME + " editado")).perform(scrollTo(), click());
            onView(withText(R.string.cancel)).perform(click());
            assertTrue(store().load().stream().anyMatch(favorite -> favorite.name.equals(NAME + " editado")));
            onView(withText("Excluir " + NAME + " editado")).perform(scrollTo(), click());
            onView(withText(R.string.delete_confirm)).perform(click());
            assertFalse(store().load().stream().anyMatch(favorite -> favorite.name.startsWith(NAME)));
        }
    }

    @Test
    public void blankFormDoesNotCreateFavorite() {
        int before = store().load().size();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withText(R.string.add_favorite)).perform(scrollTo(), click());
            onView(withText(R.string.save_favorite)).perform(scrollTo(), click());
            onView(withHint(R.string.name_hint)).check(matches(hasErrorText(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.name_error))));
            assertEquals(before, store().load().size());
        }
    }
}
