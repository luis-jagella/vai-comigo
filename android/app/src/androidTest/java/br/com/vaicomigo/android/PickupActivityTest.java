package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PickupActivityTest {
    private ActivityScenario<PickupActivity> launch() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return ActivityScenario.launch(new Intent(context, PickupActivity.class)
                .putExtra(PickupActivity.DESTINATION_NAME, "Clínica fictícia")
                .putExtra(PickupActivity.DESTINATION_ADDRESS, "Rua Fictícia, 200, Cidade Exemplo"));
    }

    @Test public void manualPickupCanBeReviewedWithoutLocationPermission() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            onView(withText(R.string.pickup_reason)).check(matches(isDisplayed()));
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 10"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withText(R.string.review_title)).check(matches(isDisplayed()));
            onView(withText("Praça Fictícia, 10")).check(matches(isDisplayed()));
            onView(withText("Clínica fictícia\nRua Fictícia, 200, Cidade Exemplo")).perform(scrollTo());
            onView(withText(R.string.change_pickup)).perform(scrollTo(), click());
            onView(withText(R.string.pickup_reason)).check(matches(isDisplayed()));
        }
    }

    @Test public void blankManualAddressShowsError() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).check(matches(hasErrorText(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.address_error))));
        }
    }

    @Test public void stoppingActivityDiscardsManualPickupAndDoesNotResumeTracking() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 10"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            onView(withText(R.string.pickup_reason)).check(matches(isDisplayed()));
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).check(matches(withText("")));
        }
    }

    @Test public void manualCorrectionPrefillsAddressAndKeepsDestination() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            openManualCorrection();
            onView(withHint(R.string.address_hint)).check(matches(withText("Praça Fictícia, 10")));
            onView(withText(R.string.continue_uber)).check(doesNotExist());
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 20"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withText("Praça Fictícia, 20")).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText("Praça Fictícia, 10")).check(doesNotExist());
            onView(withText("Clínica fictícia\nRua Fictícia, 200, Cidade Exemplo"))
                    .perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test public void blankCorrectionStaysInFormUntilReviewedAgain() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            openManualCorrection();
            onView(withHint(R.string.address_hint)).perform(replaceText(" "), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).check(matches(hasErrorText(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.address_error))));
            onView(withText(R.string.continue_uber)).check(doesNotExist());
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 30"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withText("Praça Fictícia, 30")).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test public void recreatingManualCorrectionDiscardsBothPreviousAddressAndDraft() {
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            openManualCorrection();
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 99"), closeSoftKeyboard());
            scenario.recreate();
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).check(matches(withText("")));
            onView(withText(R.string.continue_uber)).check(doesNotExist());
        }
    }

    private void openManualCorrection() {
        onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
        onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 10"), closeSoftKeyboard());
        onView(withText(R.string.review_trip)).perform(scrollTo(), click());
        onView(withText(R.string.edit_pickup_address)).perform(scrollTo(), click());
    }
}
