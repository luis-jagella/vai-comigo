package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.intent.Intents.*;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasPackage;
import static androidx.test.espresso.intent.VerificationModes.times;
import static org.junit.Assert.*;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PrefillFlowTest {
    private final FakeAddressResolver fake = new FakeAddressResolver();
    private final ResolvedPlace pickup = new ResolvedPlace(-23.55, -46.63, "Praça Fictícia, 10, Cidade Exemplo");
    private final ResolvedPlace alternate = new ResolvedPlace(-23.56, -46.64, "Praça Fictícia & teste #20? x=1");
    private final ResolvedPlace destination = new ResolvedPlace(-23.57, -46.65, "Clínica Fictícia, 200, Cidade Exemplo");

    @Before public void setUp() { Intents.init(); }
    @After public void tearDown() { Intents.release(); }

    private ActivityScenario<PickupActivity> review() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ActivityScenario<PickupActivity> scenario = ActivityScenario.launch(new Intent(context, PickupActivity.class)
                .putExtra(PickupActivity.DESTINATION_NAME, "Clínica fictícia")
                .putExtra(PickupActivity.DESTINATION_ADDRESS, "Rua Fictícia, 200, Cidade Exemplo"));
        scenario.onActivity(activity -> { activity.resolver.cancel(); activity.resolver = fake; });
        manualReview();
        return scenario;
    }

    private void manualReview() {
        onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
        onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 10"), closeSoftKeyboard());
        onView(withText(R.string.review_trip)).perform(scrollTo(), click());
    }

    private void allowSearch() {
        onView(withText(R.string.prepare_uber_points)).perform(scrollTo(), click());
        onView(withText(R.string.allow_address_search)).perform(scrollTo(), click());
    }

    private void prepare(ActivityScenario<PickupActivity> scenario) {
        allowSearch();
        scenario.onActivity(activity -> fake.complete(Collections.singletonList(pickup)));
        onView(withText(FakeAddressResolver.choice(pickup))).perform(scrollTo(), click());
        scenario.onActivity(activity -> fake.complete(Collections.singletonList(destination)));
        onView(withText(FakeAddressResolver.choice(destination))).perform(scrollTo(), click());
    }

    @Test public void decliningSearchNeverGeocodesOrSendsPoints() {
        intending(hasPackage("com.ubercab")).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
        try (ActivityScenario<PickupActivity> scenario = review()) {
            assertTrue(fake.queries.isEmpty());
            onView(withText(R.string.prepare_uber_points)).perform(scrollTo(), click());
            assertTrue(fake.queries.isEmpty());
            onView(withText(R.string.prefill_manual_fallback)).perform(scrollTo(), click());
            onView(withText(R.string.confirm_uber_description)).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText(R.string.open_uber)).perform(click());
            assertTrue(fake.queries.isEmpty());
            assertNull(uberIntent().getData().getQuery());
        }
    }

    @Test public void bothAmbiguousPointsRequireChoiceAndFinalConfirmation() {
        intending(hasPackage("com.ubercab")).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
        try (ActivityScenario<PickupActivity> scenario = review()) {
            allowSearch();
            assertEquals(Collections.singletonList("Praça Fictícia, 10"), fake.queries);
            scenario.onActivity(activity -> fake.complete(Arrays.asList(pickup, alternate)));
            assertEquals(1, fake.queries.size());
            onView(withText(R.string.continue_uber)).check(doesNotExist());
            onView(withText(FakeAddressResolver.choice(alternate))).perform(scrollTo(), click());
            assertEquals("Rua Fictícia, 200, Cidade Exemplo", fake.queries.get(1));
            scenario.onActivity(activity -> fake.complete(Arrays.asList(destination, pickup)));
            onView(withText(R.string.continue_uber)).check(doesNotExist());
            onView(withText(FakeAddressResolver.choice(destination))).perform(scrollTo(), click());
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.confirm_uber_prefilled)).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText(R.string.cancel)).perform(click());
            intended(hasPackage("com.ubercab"), times(0));
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.open_uber)).perform(click());
            intended(hasPackage("com.ubercab"), times(1));
            Uri uri = uberIntent().getData();
            assertEquals("uber", uri.getScheme());
            assertEquals("riderequest", uri.getAuthority());
            assertEquals(6, uri.getQueryParameterNames().size());
            assertEquals(Double.toString(alternate.latitude), uri.getQueryParameter("pickup[latitude]"));
            assertEquals(Double.toString(alternate.longitude), uri.getQueryParameter("pickup[longitude]"));
            assertEquals(alternate.label, uri.getQueryParameter("pickup[formatted_address]"));
            assertEquals(Double.toString(destination.latitude), uri.getQueryParameter("dropoff[latitude]"));
            assertEquals(Double.toString(destination.longitude), uri.getQueryParameter("dropoff[longitude]"));
            assertEquals(destination.label, uri.getQueryParameter("dropoff[formatted_address]"));
            assertNull(uri.getQueryParameter("x"));
            assertNull(uri.getFragment());
            assertNull(uberIntent().getExtras());
            assertEquals(Intent.ACTION_VIEW, uberIntent().getAction());
        }
    }

    @Test public void emptyResultsOfferRetryWithoutAutomaticRequests() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            allowSearch();
            scenario.onActivity(activity -> fake.complete(Collections.emptyList()));
            onView(withText(R.string.address_search_failed)).perform(scrollTo()).check(matches(isDisplayed()));
            assertEquals(1, fake.queries.size());
            onView(withText(R.string.retry_address_search)).perform(scrollTo(), click());
            assertEquals(1, fake.queries.size());
            onView(withText(R.string.allow_address_search)).perform(scrollTo(), click());
            assertEquals(2, fake.queries.size());
            intended(hasPackage("com.ubercab"), times(0));
        }
    }

    @Test public void cancelledSearchCannotUseLateResultsEvenDuringANewSearch() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            allowSearch();
            AddressResolver.Callback old = fake.pending;
            onView(withText(R.string.prefill_manual_fallback)).perform(scrollTo(), click());
            onView(withText(R.string.cancel)).perform(click());
            allowSearch();
            scenario.onActivity(activity -> old.completed(Collections.singletonList(alternate)));
            onView(withText(FakeAddressResolver.choice(alternate))).check(doesNotExist());
            scenario.onActivity(activity -> fake.complete(Collections.singletonList(pickup)));
            onView(withText(FakeAddressResolver.choice(pickup))).perform(scrollTo()).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(0));
        }
    }

    @Test public void backgroundAndRecreationDiscardSearchResults() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            allowSearch();
            AddressResolver.Callback old = fake.pending;
            scenario.moveToState(Lifecycle.State.CREATED);
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.onActivity(activity -> old.completed(Collections.singletonList(pickup)));
            onView(withText(R.string.pickup_reason)).perform(scrollTo()).check(matches(isDisplayed()));
            manualReview();
            allowSearch();
            AddressResolver.Callback beforeRecreation = fake.pending;
            scenario.recreate();
            scenario.onActivity(activity -> beforeRecreation.completed(Collections.singletonList(pickup)));
            onView(withText(R.string.pickup_reason)).perform(scrollTo()).check(matches(isDisplayed()));
            intended(hasPackage("com.ubercab"), times(0));
        }
    }

    @Test public void correctingPickupInvalidatesAllConfirmedPoints() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            prepare(scenario);
            onView(withText(R.string.edit_pickup_address)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).perform(replaceText("Praça Fictícia, 99"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withText(R.string.review_prefill_ready)).check(doesNotExist());
            assertEquals(2, fake.queries.size());
            allowSearch();
            assertEquals("Praça Fictícia, 99", fake.queries.get(2));
            intended(hasPackage("com.ubercab"), times(0));
        }
    }

    @Test public void unavailableUberKeepsConfirmedPointsForExplicitRetry() {
        try (ActivityScenario<PickupActivity> scenario = review()) {
            prepare(scenario);
            onView(withText(R.string.continue_uber)).perform(scrollTo(), click());
            onView(withText(R.string.open_uber)).perform(click());
            onView(withText(R.string.uber_unavailable)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withText(R.string.review_prefill_ready)).perform(scrollTo()).check(matches(isDisplayed()));
            assertEquals(2, fake.queries.size());
        }
    }

    private Intent uberIntent() {
        return Intents.getIntents().stream().filter(value -> "com.ubercab".equals(value.getPackage()))
                .findFirst().get();
    }
}
