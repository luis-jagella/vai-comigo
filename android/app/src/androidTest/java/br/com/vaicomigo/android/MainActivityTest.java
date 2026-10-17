package br.com.vaicomigo.android;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/** Runs on a device to exercise real framework widgets and navigation. */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test
    public void helpAndReturnToDestinations() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                click(activity, R.string.help);
                assertNotNull(find(activity, R.string.help_description));
                click(activity, R.string.back_to_destinations);
                assertNotNull(find(activity, R.string.destinations_description));
            });
        }
    }

    @Test
    public void settingsExposeAccessibilityAction() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                click(activity, R.string.settings);
                assertNotNull(find(activity, R.string.settings_description));
                assertTrue(find(activity, R.string.open_accessibility) instanceof Button);
                click(activity, R.string.back_to_destinations);
                assertNotNull(find(activity, R.string.destinations_description));
            });
        }
    }

    @Test
    public void navigationTargetsHaveReadableLabelsAndLargeTouchAreas() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                for (int label : new int[]{R.string.help, R.string.settings}) {
                    TextView button = find(activity, label);
                    assertNotNull(button);
                    assertTrue(button.isClickable());
                    float density = activity.getResources().getDisplayMetrics().density;
                    assertTrue(button.getHeight() >= 64 * density);
                }
            });
        }
    }

    private void click(MainActivity activity, int label) {
        TextView button = find(activity, label);
        assertNotNull(button);
        button.performClick();
    }

    private TextView find(MainActivity activity, int label) {
        return find(activity.getWindow().getDecorView(), activity.getString(label));
    }

    private TextView find(View view, String label) {
        if (view instanceof TextView && label.contentEquals(((TextView) view).getText())) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                TextView match = find(group.getChildAt(index), label);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }
}
