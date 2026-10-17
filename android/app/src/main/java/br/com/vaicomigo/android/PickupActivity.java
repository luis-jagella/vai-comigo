package br.com.vaicomigo.android;

import android.Manifest;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Pickup details exist only while this screen is in the foreground. */
public final class PickupActivity extends Activity {
    static final String DESTINATION_NAME = "destinationName";
    static final String DESTINATION_ADDRESS = "destinationAddress";
    private static final int PERMISSION_REQUEST = 42;
    private static final int INK = Color.rgb(26, 51, 44), GREEN = Color.rgb(25, 91, 70);
    private final PickupSession session = new PickupSession();
    private PickupLocationReader reader;
    private boolean visible;
    private String destinationName, destinationAddress;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        destinationName = getIntent().getStringExtra(DESTINATION_NAME);
        destinationAddress = getIntent().getStringExtra(DESTINATION_ADDRESS);
        if (destinationName == null || destinationAddress == null) {
            finish();
            return;
        }
        reader = new PickupLocationReader(this);
    }

    @Override protected void onStart() {
        super.onStart();
        visible = true;
        if (reader != null) { render(); }
    }

    @Override protected void onResume() {
        super.onResume();
        if (reader != null && (session.position() != null || session.state() == PickupSession.State.LOCATING)
                && !reader.permitted()) {
            reader.cancel();
            session.permissionRevoked();
            render();
        }
    }

    @Override protected void onStop() {
        visible = false;
        if (reader != null) { reader.cancel(); }
        session.reset();
        super.onStop();
    }

    private void useLocation() {
        reader.cancel();
        session.useLocation(reader.permitted());
        render();
        if (session.state() == PickupSession.State.PERMISSION) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
        } else {
            locate();
        }
    }

    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if (request != PERMISSION_REQUEST || !visible || session.state() != PickupSession.State.PERMISSION) { return; }
        session.permissionResult(reader.permitted());
        render();
        if (session.state() == PickupSession.State.LOCATING) { locate(); }
    }

    private void locate() {
        reader.start(new PickupLocationReader.Callback() {
            @Override public void found(double latitude, double longitude, float accuracy, long age) {
                if (!visible) { return; }
                session.located(latitude, longitude, accuracy, age);
                render();
            }
            @Override public void unavailable() {
                if (!visible) { return; }
                session.unavailable();
                render();
            }
            @Override public void denied() {
                if (!visible) { return; }
                session.permissionRevoked();
                render();
            }
        });
    }

    private void manual() {
        reader.cancel();
        session.manual();
        render();
    }

    private void render() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(24), dp(24), dp(24));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247, 247, 240));
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
        paragraph(content, getString(R.string.app_name), 22);
        switch (session.state()) {
            case EXPLANATION:
            case PERMISSION:
                heading(content, R.string.pickup_title);
                paragraph(content, getString(R.string.pickup_reason), 20);
                paragraph(content, destinationName + "\n" + destinationAddress, 20);
                if (session.state() == PickupSession.State.EXPLANATION) {
                    button(content, R.string.use_location, true, this::useLocation);
                }
                button(content, R.string.manual_pickup, false, this::manual);
                break;
            case LOCATING:
                heading(content, R.string.finding_pickup);
                paragraph(content, getString(R.string.finding_description), 20);
                button(content, R.string.manual_pickup, true, this::manual);
                break;
            case DENIED:
            case UNAVAILABLE:
                boolean denied = session.state() == PickupSession.State.DENIED;
                heading(content, denied ? R.string.pickup_denied : R.string.pickup_unavailable);
                TextView error = paragraph(content, getString(denied ? R.string.pickup_denied_description
                        : R.string.pickup_unavailable_description), 20);
                error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                button(content, R.string.manual_pickup, true, this::manual);
                button(content, R.string.use_location, false, this::useLocation);
                break;
            case MANUAL:
                heading(content, R.string.manual_pickup);
                TextView label = paragraph(content, getString(R.string.address_label), 20);
                EditText address = new EditText(this);
                address.setId(View.generateViewId());
                address.setSaveEnabled(false);
                address.setTextColor(INK);
                address.setHintTextColor(Color.rgb(79, 99, 89));
                address.setTextSize(20);
                address.setHint(R.string.address_hint);
                address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                address.setFilters(new InputFilter[]{new InputFilter.LengthFilter(240)});
                address.setMinHeight(dp(80));
                label.setLabelFor(address.getId());
                content.addView(address, fullWidth());
                button(content, R.string.review_trip, true, () -> {
                    String value = address.getText().toString();
                    if (value.trim().isEmpty()) {
                        address.setError(getString(R.string.address_error));
                        address.requestFocus();
                        return;
                    }
                    session.reviewManual(value);
                    ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(address.getWindowToken(), 0);
                    render();
                });
                break;
            case LOCATED:
                heading(content, R.string.pickup_found);
                int accuracy = (int) Math.ceil(session.position().accuracyMeters);
                paragraph(content, getResources().getQuantityString(R.plurals.pickup_accuracy, accuracy, accuracy), 20);
                button(content, R.string.review_trip, true, () -> {
                    if (!reader.permitted()) { session.permissionRevoked(); }
                    else { session.reviewLocation(); }
                    render();
                });
                button(content, R.string.manual_pickup, false, this::manual);
                break;
            case REVIEW:
                heading(content, R.string.review_title);
                paragraph(content, getString(R.string.pickup_label), 22);
                paragraph(content, session.address() != null ? session.address()
                        : getString(R.string.current_pickup), 20);
                paragraph(content, getString(R.string.destination_label), 22);
                paragraph(content, destinationName + "\n" + destinationAddress, 20);
                paragraph(content, getString(R.string.review_unavailable), 18);
                button(content, R.string.change_pickup, false, () -> {
                    reader.cancel();
                    session.reset();
                    render();
                });
                break;
        }
        button(content, R.string.finish_pickup, false, () -> {
            reader.cancel();
            session.reset();
            finish();
        });
        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private TextView paragraph(LinearLayout content, String value, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(INK);
        text.setTextSize(size);
        text.setPadding(0, dp(12), 0, dp(16));
        text.setSaveEnabled(false);
        content.addView(text, fullWidth());
        return text;
    }

    private void heading(LinearLayout content, int label) {
        TextView title = paragraph(content, getString(label), 30);
        title.setTypeface(null, Typeface.BOLD);
        if (android.os.Build.VERSION.SDK_INT >= 28) { title.setAccessibilityHeading(true); }
        setTitle(label);
    }

    private void button(LinearLayout content, int label, boolean primary, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(20);
        button.setAllCaps(false);
        button.setMinHeight(dp(64));
        button.setTextColor(primary ? Color.WHITE : INK);
        button.setPadding(dp(20), dp(18), dp(20), dp(18));
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(primary ? GREEN : Color.WHITE);
        shape.setCornerRadius(dp(20));
        shape.setStroke(dp(1), primary ? GREEN : Color.rgb(204, 217, 205));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33000000), shape, null));
        LinearLayout.LayoutParams params = fullWidth();
        params.topMargin = dp(16);
        content.addView(button, params);
        button.setOnClickListener(view -> action.run());
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
