package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage;
import static androidx.test.espresso.intent.VerificationModes.times;
import static org.junit.Assert.*;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Use a clean emulator without Uber. Success is intercepted; no real ride app is launched. */
@RunWith(AndroidJUnit4.class)
public class UberHandoffTest {
    @Before public void setUp() { Intents.init(); }
    @After public void tearDown() { Intents.release(); }

    private ActivityScenario<PickupActivity> review() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ActivityScenario<PickupActivity> scenario = ActivityScenario.launch(new Intent(context, PickupActivity.class)
                .putExtra(PickupActivity.DESTINATION_NAME, "Clínica fictícia")
                .putExtra(PickupActivity.DESTINATION_ADDRESS, "Rua Fictícia, 200, Cidade Exemplo"));
        onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
        onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 10"), closeSoftKeyboard());
        onView(withText(R.string.review_trip)).perform(scrollTo(), click());
        return scenario;
    }

    @Test public void cancellingConfirmationNeverOpensUber() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            onView(withText(R.string.listen_review)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText(R.string.stop_reading)).perform(scrollTo(), click());
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.confirm_uber_description)).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText(R.string.cancel)).perform(click());
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText("Praça Fictícia, 10")).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test public void confirmedHandoffTargetsUberWithoutSendingTripData() {
        intending(hasPackage("com.ubercab")).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
        try (ActivityScenario<PickupActivity> scenario = review()) {
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.open_uber)).perform(click());
            intended(hasPackage("com.ubercab"), times(1));
            Intent intent = Intents.getIntents().stream().filter(value -> "com.ubercab".equals(value.getPackage()))
                    .findFirst().get();
            assertEquals(Intent.ACTION_VIEW, intent.getAction());
            assertEquals("uber://riderequest", intent.getDataString());
            assertNull(intent.getExtras());
            assertNull(intent.getData().getQuery());
        }
    }

    @Test public void missingUberKeepsManualPickupAndOffersSafeReturn() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.open_uber)).perform(click());
            onView(withText(R.string.uber_unavailable)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText("Praça Fictícia, 10")).perform(scrollTo()).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(1));
        }
    }

    @Test public void leavingDuringConfirmationDiscardsTripAndCannotLaunchLater() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText(R.string.pickup_reason)).check(matches(isDisplayed()));
        }
    }
}
