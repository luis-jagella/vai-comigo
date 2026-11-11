package br.com.vaicomigo.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Checks the installed debug artifact, not a production release. */
@RunWith(AndroidJUnit4.class)
public class DemoPackageTest {
    @Test
    public void installedPackageIsClearlyIdentifiedAsATestBuild() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

        assertEquals("0.1.0-demo.1", info.versionName);
        assertTrue((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        assertEquals("Vai Comigo (teste)", context.getPackageManager()
                .getApplicationLabel(context.getApplicationInfo()).toString());
    }

    @Test
    public void homeDisplaysTheTestNotice() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                ArrayList<View> matches = new ArrayList<>();
                activity.getWindow().getDecorView().findViewsWithText(matches,
                        activity.getString(R.string.prototype_notice), View.FIND_VIEWS_WITH_TEXT);
                assertEquals(1, matches.size());
                assertTrue(matches.get(0).isShown());
            });
        }
    }
}
