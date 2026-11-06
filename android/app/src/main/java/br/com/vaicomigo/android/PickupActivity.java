package br.com.vaicomigo.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;
import java.util.Collections;
import java.util.Locale;

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
    private OfflineReviewSpeaker speaker;
    private AlertDialog confirmation;
    private boolean handoffFailed;
    private final PrefillSession prefill = new PrefillSession();
    AddressResolver resolver;
    private int prefillGeneration;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        destinationName = getIntent().getStringExtra(DESTINATION_NAME);
        destinationAddress = getIntent().getStringExtra(DESTINATION_ADDRESS);
        if (destinationName == null || destinationAddress == null) {
            finish();
            return;
        }
        reader = new PickupLocationReader(this);
        speaker = new OfflineReviewSpeaker(this);
        resolver = new AndroidAddressResolver(this);
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
            clearPrefill();
            speaker.stop();
            session.permissionRevoked();
            render();
        }
    }

    @Override protected void onStop() {
        visible = false;
        if (reader != null) { reader.cancel(); }
        if (speaker != null) { speaker.stop(); }
        if (confirmation != null) { confirmation.dismiss(); confirmation = null; }
        handoffFailed = false;
        clearPrefill();
        session.reset();
        super.onStop();
    }

    private void useLocation() {
        clearPrefill();
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
        clearPrefill();
        reader.cancel();
        session.manual();
        render();
    }

    private boolean canHandoff() {
        if (!visible || session.state() != PickupSession.State.REVIEW) { return false; }
        if (session.position() != null && !reader.permitted()) { session.permissionRevoked(); }
        if (!session.readyToHandoff()) {
            clearPrefill();
            speaker.stop();
            render();
            return false;
        }
        return true;
    }

    private void confirmUber() {
        if (!canHandoff()) { return; }
        if (prefill.stage() != PrefillSession.Stage.IDLE && !prefill.confirmed()) { return; }
        boolean filled = prefill.confirmed();
        speaker.stop();
        if (confirmation != null && confirmation.isShowing()) { return; }
        confirmation = new AlertDialog.Builder(this).setTitle(R.string.confirm_uber_title)
                .setMessage(filled ? R.string.confirm_uber_prefilled : R.string.confirm_uber_description)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.open_uber, (dialog, which) -> {
                    if (!canHandoff()) { return; }
                    if (filled != prefill.confirmed()) { return; }
                    boolean opened = filled ? UberHandoff.open(this, prefill.pickup(), prefill.destination())
                            : UberHandoff.open(this);
                    if (opened) {
                        clearPrefill();
                        session.reset();
                        finish();
                    } else {
                        handoffFailed = true;
                        render();
                    }
                }).create();
        confirmation.show();
    }

    private void readReview() {
        if (!canHandoff()) { return; }
        speaker.read(getString(R.string.review_spoken, prefill.confirmed() ? prefill.pickup().label
                : session.address() != null ? session.address()
                : getString(R.string.current_pickup), destinationName, destinationAddress,
                getString(prefill.confirmed() ? R.string.review_prefill_ready : R.string.review_unavailable))
                        + (prefill.confirmed() ? " " + prefill.destination().label : "")
                        + (handoffFailed ? " " + getString(R.string.uber_unavailable) : ""), () -> {
                    if (visible) { Toast.makeText(this, R.string.speech_unavailable, Toast.LENGTH_LONG).show(); }
                });
    }

    private void clearPrefill() {
        prefillGeneration++;
        if (resolver != null) { resolver.cancel(); }
        prefill.reset();
    }

    private void beginPrefill() {
        if (!canHandoff()) { return; }
        speaker.stop();
        clearPrefill();
        handoffFailed = false;
        prefill.begin();
        render();
    }

    private void searchPickup() {
        if (!canHandoff() || prefill.stage() != PrefillSession.Stage.CONSENT) { return; }
        prefill.startPickupSearch();
        if (session.position() != null) {
            PickupSession.Position point = session.position();
            prefill.pickupResults(Collections.singletonList(new ResolvedPlace(point.latitude,
                    point.longitude, getString(R.string.current_pickup))));
            render();
        } else {
            render();
            searchAddress(true);
        }
    }

    private void searchAddress(boolean pickup) {
        int request = prefillGeneration;
        resolver.search(pickup ? session.address() : destinationAddress, values -> {
            if (!visible || request != prefillGeneration || !canHandoff()) { return; }
            PrefillSession.Stage expected = pickup ? PrefillSession.Stage.SEARCHING_PICKUP
                    : PrefillSession.Stage.SEARCHING_DESTINATION;
            if (prefill.stage() != expected) { return; }
            if (pickup) { prefill.pickupResults(values); }
            else { prefill.destinationResults(values); }
            render();
        });
    }

    private void renderPrefill(LinearLayout content) {
        switch (prefill.stage()) {
            case IDLE:
                paragraph(content, getString(R.string.review_unavailable), 18);
                button(content, R.string.prepare_uber_points, true, this::beginPrefill);
                break;
            case CONSENT:
                paragraph(content, getString(R.string.prefill_consent), 20);
                button(content, R.string.allow_address_search, true, this::searchPickup);
                break;
            case SEARCHING_PICKUP:
            case SEARCHING_DESTINATION:
                TextView searching = paragraph(content, getString(R.string.searching_address), 20);
                searching.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                break;
            case PICKUP_CHOICES:
            case DESTINATION_CHOICES:
                boolean pickup = prefill.stage() == PrefillSession.Stage.PICKUP_CHOICES;
                TextView question = paragraph(content,
                        getString(pickup ? R.string.choose_pickup_point : R.string.choose_destination_point), 22);
                question.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                if (android.os.Build.VERSION.SDK_INT >= 28) { question.setAccessibilityHeading(true); }
                paragraph(content, getString(R.string.confirm_point_warning), 18);
                for (int index = 0; index < prefill.choices().size(); index++) {
                    int selected = index;
                    int generation = prefillGeneration;
                    ResolvedPlace value = prefill.choices().get(index);
                    button(content, getString(R.string.choose_resolved_point, value.label,
                            String.format(Locale.ROOT, "%.6f, %.6f", value.latitude, value.longitude)), false, () -> {
                        if (!canHandoff()) { return; }
                        if (generation != prefillGeneration || prefill.stage() != (pickup
                                ? PrefillSession.Stage.PICKUP_CHOICES : PrefillSession.Stage.DESTINATION_CHOICES)) { return; }
                        if (pickup) {
                            prefill.choosePickup(selected);
                            render();
                            searchAddress(false);
                        } else {
                            prefill.chooseDestination(selected);
                            render();
                        }
                    });
                }
                break;
            case READY:
                paragraph(content, getString(R.string.review_prefill_ready), 18);
                paragraph(content, getString(R.string.confirmed_point, getString(R.string.pickup_label), prefill.pickup().label), 20);
                paragraph(content, getString(R.string.confirmed_point, getString(R.string.destination_label), prefill.destination().label), 20);
                button(content, R.string.prepare_uber_points, false, this::beginPrefill);
                break;
            case FAILED:
                TextView failure = paragraph(content, getString(R.string.address_search_failed), 20);
                failure.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                button(content, R.string.retry_address_search, true, this::beginPrefill);
                break;
        }
        if (prefill.stage() != PrefillSession.Stage.IDLE) {
            button(content, R.string.prefill_manual_fallback, false, () -> {
                clearPrefill();
                render();
                confirmUber();
            });
        }
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
                address.setText(session.address() == null ? "" : session.address());
                address.setMinHeight(dp(80));
                label.setLabelFor(address.getId());
                content.addView(address, fullWidth());
                button(content, R.string.review_trip, true, () -> {
                    String value = FavoriteDestination.normalizeText(address.getText().toString());
                    if (value.isEmpty()) {
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
                renderPrefill(content);
                if (handoffFailed) {
                    TextView handoffError = paragraph(content, getString(R.string.uber_unavailable), 20);
                    handoffError.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
                }
                if (prefill.stage() == PrefillSession.Stage.IDLE || prefill.confirmed()) {
                    button(content, R.string.listen_review, false, this::readReview);
                    button(content, R.string.stop_reading, false, speaker::stop);
                    button(content, R.string.continue_uber, prefill.confirmed(), this::confirmUber);
                }
                if (session.address() != null) {
                    button(content, R.string.edit_pickup_address, false, () -> {
                        speaker.stop();
                        clearPrefill();
                        handoffFailed = false;
                        session.editManual();
                        render();
                    });
                }
                button(content, R.string.change_pickup, false, () -> {
                    speaker.stop();
                    clearPrefill();
                    handoffFailed = false;
                    reader.cancel();
                    session.reset();
                    render();
                });
                break;
        }
        button(content, R.string.finish_pickup, false, () -> {
            reader.cancel();
            clearPrefill();
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
        button(content, getString(label), primary, action);
    }

    private void button(LinearLayout content, String label, boolean primary, Runnable action) {
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
