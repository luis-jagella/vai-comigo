package br.com.vaicomigo.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Initial navigation; no location, account or ride data is collected. */
public final class MainActivity extends Activity {
    private static final String SCREEN_KEY = "screen";
    private static final int DESTINATIONS = 0;
    private static final int HELP = 1;
    private static final int SETTINGS = 2;
    private int screen = DESTINATIONS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            screen = savedInstanceState.getInt(SCREEN_KEY, DESTINATIONS);
        }
        showScreen(screen);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt(SCREEN_KEY, screen);
        super.onSaveInstanceState(state);
    }

    private void showScreen(int selected) {
        screen = selected;
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(24));
        scroll.addView(content);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });

        int title = selected == HELP ? R.string.help
                : selected == SETTINGS ? R.string.settings : R.string.destinations;
        int description = selected == HELP ? R.string.help_description
                : selected == SETTINGS ? R.string.settings_description : R.string.destinations_description;
        TextView heading = text(title, 30);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            heading.setAccessibilityHeading(true);
        }
        content.addView(heading);
        content.addView(text(description, 22));
        if (selected == DESTINATIONS) {
            addButton(content, R.string.help, () -> showScreen(HELP));
            addButton(content, R.string.settings, () -> showScreen(SETTINGS));
        } else {
            if (selected == SETTINGS) {
                addButton(content, R.string.open_accessibility, this::openAccessibility);
            }
            addButton(content, R.string.back_to_destinations, () -> showScreen(DESTINATIONS));
        }
        content.addView(text(R.string.prototype_notice, 18));
        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private TextView text(int resource, int size) {
        TextView text = new TextView(this);
        text.setText(resource);
        text.setTextSize(size);
        text.setTextColor(Color.rgb(24, 42, 35));
        text.setPadding(0, 0, 0, dp(24));
        return text;
    }

    private void addButton(LinearLayout content, int label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(22);
        button.setAllCaps(false);
        button.setMinHeight(dp(64));
        button.setTextColor(Color.rgb(24, 42, 35));
        button.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.bottomMargin = dp(16);
        content.addView(button, layout);
        button.setOnClickListener(view -> action.run());
    }

    private void openAccessibility() {
        try {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.accessibility_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
