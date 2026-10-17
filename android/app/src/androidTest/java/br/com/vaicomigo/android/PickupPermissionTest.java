package br.com.vaicomigo.android;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import java.io.IOException;
import java.util.regex.Pattern;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/** Runs only on a clean test installation. Mock provider changes are reverted. */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PickupPermissionTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getTargetContext();
    private final UiDevice device = UiDevice.getInstance(instrumentation);

    private ActivityScenario<PickupActivity> launch() {
        return ActivityScenario.launch(new Intent(context, PickupActivity.class)
                .putExtra(PickupActivity.DESTINATION_NAME, "Casa fictícia")
                .putExtra(PickupActivity.DESTINATION_ADDRESS, "Rua Fictícia, 100, Cidade Exemplo"));
    }

    @Test public void a_denialOffersManualAlternative() {
        assertEquals(PackageManager.PERMISSION_DENIED, context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
        try (ActivityScenario<PickupActivity> scenario = launch()) {
            onView(withText(R.string.use_location)).perform(scrollTo(), click());
            UiObject2 deny = device.wait(Until.findObject(By.res(Pattern.compile(".*:id/permission_deny_button"))), 10_000);
            assertNotNull("Android permission dialog must appear after the explicit action", deny);
            deny.click();
            assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.pickup_denied))), 5_000));
            onView(withText(R.string.manual_pickup)).perform(scrollTo(), click());
            onView(withHint(R.string.address_hint)).perform(replaceText("Rua Fictícia, 10"), closeSoftKeyboard());
            onView(withText(R.string.review_trip)).perform(scrollTo(), click());
            onView(withText(R.string.review_title)).check(matches(isDisplayed()));
        }
    }

    @Test @SuppressWarnings("deprecation")
    public void b_grantedPermissionReadsFreshMockPositionAndReviewsIt() throws IOException {
        instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), Manifest.permission.ACCESS_COARSE_LOCATION);
        instrumentation.getUiAutomation().grantRuntimePermission(context.getPackageName(), Manifest.permission.ACCESS_FINE_LOCATION);
        shell("appops set " + context.getPackageName() + " android:mock_location allow");
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean installed = false;
        try {
            manager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false,
                    true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
            installed = true;
            manager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            try (ActivityScenario<PickupActivity> scenario = launch()) {
                onView(withText(R.string.use_location)).perform(scrollTo(), click());
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(-23.55);
                location.setLongitude(-46.63);
                location.setAccuracy(10);
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                manager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
                assertTrue(device.wait(Until.hasObject(By.text(context.getString(R.string.pickup_found))), 10_000));
                onView(withText(R.string.review_trip)).perform(scrollTo(), click());
                onView(withText(R.string.current_pickup)).check(matches(isDisplayed()));
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED);
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);
                onView(withText(R.string.pickup_reason)).check(matches(isDisplayed()));
            }
        } finally {
            if (installed) { manager.removeTestProvider(LocationManager.GPS_PROVIDER); }
            shell("appops set " + context.getPackageName() + " android:mock_location default");
        }
    }

    private void shell(String command) throws IOException {
        try (ParcelFileDescriptor descriptor = instrumentation.getUiAutomation().executeShellCommand(command);
                ParcelFileDescriptor.AutoCloseInputStream input = new ParcelFileDescriptor.AutoCloseInputStream(descriptor)) {
            byte[] buffer = new byte[1024];
            while (input.read(buffer) != -1) { /* Wait for the test-only command to finish. */ }
        }
    }
}
