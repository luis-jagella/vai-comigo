package br.com.vaicomigo.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.provider.Settings;
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
import java.util.List;
import java.util.UUID;

public final class MainActivity extends Activity {
    private static final int HOME = 0, HELP = 1, SETTINGS = 2, MANAGE = 3, FORM = 4, SELECTED = 5;
    private static final int INK = Color.rgb(26, 51, 44), GREEN = Color.rgb(25, 91, 70);
    private static final int MUTED = Color.rgb(79, 99, 89), PAPER = Color.rgb(247, 247, 240);
    private static final int BORDER = Color.rgb(204, 217, 205);
    private int screen = HOME;
    private String selectedId;
    private FavoriteStore store;
    private EditText nameInput, addressInput;
    private String draftName = "", draftAddress = "";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new FavoriteStore(getSharedPreferences("favorites", MODE_PRIVATE));
        if (state != null) {
            screen = state.getInt("screen", HOME);
            selectedId = state.getString("selected");
            draftName = state.getString("draftName", "");
            draftAddress = state.getString("draftAddress", "");
        }
        show(screen);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt("screen", screen);
        state.putString("selected", selectedId);
        state.putString("draftName", screen == FORM ? nameInput.getText().toString() : "");
        state.putString("draftAddress", screen == FORM ? addressInput.getText().toString() : "");
        super.onSaveInstanceState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (screen != HOME) {
            show(screen == FORM ? MANAGE : HOME);
        } else {
            super.onBackPressed();
        }
    }

    private void show(int next) {
        screen = next;
        LinearLayout content = column();
        content.setPadding(dp(24), dp(24), dp(24), dp(24));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(PAPER);
        scroll.setFillViewport(true);
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
        TextView brand = text(getString(R.string.app_name), 22, GREEN);
        brand.setTypeface(null, Typeface.BOLD);
        content.addView(brand);
        TextView caption = text(getString(R.string.brand_caption), 12, MUTED);
        caption.setLetterSpacing(0.14f);
        content.addView(caption);
        space(content, 20);
        if (next == HOME || next == MANAGE) {
            favorites(content, next == MANAGE);
        } else if (next == FORM) {
            form(content);
        } else if (next == SELECTED) {
            selection(content);
        } else {
            heading(content, getString(next == HELP ? R.string.help : R.string.settings));
            content.addView(text(getString(next == HELP ? R.string.help_description
                    : R.string.settings_description), 20, MUTED));
            if (next == SETTINGS) {
                button(content, getString(R.string.open_accessibility), true, this::openAccessibility);
            }
            button(content, getString(R.string.back_to_destinations), false, () -> show(HOME));
        }
        space(content, 20);
        content.addView(text(getString(R.string.prototype_notice), 14, MUTED));
        setContentView(scroll);
        scroll.requestApplyInsets();
    }

    private void favorites(LinearLayout content, boolean managing) {
        heading(content, getString(managing ? R.string.manage_favorites : R.string.home_title));
        content.addView(text(getString(R.string.destinations_description), 19, MUTED));
        List<FavoriteDestination> values;
        try {
            values = store.load();
        } catch (IllegalStateException exception) {
            content.addView(text(getString(R.string.storage_error), 20, INK));
            button(content, getString(R.string.back_to_destinations), false, () -> show(HOME));
            return;
        }
        space(content, 12);
        TextView section = text(getString(R.string.favorites_heading), 22, INK);
        section.setTypeface(null, Typeface.BOLD);
        content.addView(section);
        if (values.isEmpty()) {
            LinearLayout empty = card(content);
            TextView title = text(getString(R.string.empty_title), 22, INK);
            title.setTypeface(null, Typeface.BOLD);
            empty.addView(title);
            empty.addView(text(getString(R.string.empty_description), 18, MUTED));
        }
        for (FavoriteDestination destination : values) {
            if (managing) {
                LinearLayout item = card(content);
                item.addView(text(destination.name, 24, INK));
                item.addView(text(destination.address, 18, MUTED));
                button(item, getString(R.string.edit_named, destination.name), false, () -> edit(destination));
                button(item, getString(R.string.delete_named, destination.name), false,
                        () -> confirmDelete(destination));
            } else {
                Button place = button(content, destination.name + "\n" + destination.address, false, () -> {
                    selectedId = destination.id;
                    show(SELECTED);
                });
                place.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            }
        }
        content.addView(text(getString(R.string.favorite_count, values.size()), 15, MUTED));
        if (values.size() < FavoriteStore.MAX_FAVORITES) {
            button(content, getString(R.string.add_favorite), true, () -> edit(null));
        } else {
            content.addView(text(getString(R.string.favorite_limit), 18, MUTED));
        }
        if (managing) {
            button(content, getString(R.string.back_to_destinations), false, () -> show(HOME));
        } else {
            if (!values.isEmpty()) {
                button(content, getString(R.string.manage_favorites), false, () -> show(MANAGE));
            }
            space(content, 8);
            button(content, getString(R.string.help), false, () -> show(HELP));
            button(content, getString(R.string.settings), false, () -> show(SETTINGS));
        }
    }

    private void edit(FavoriteDestination destination) {
        selectedId = destination == null ? null : destination.id;
        draftName = destination == null ? "" : destination.name;
        draftAddress = destination == null ? "" : destination.address;
        show(FORM);
    }

    private void form(LinearLayout content) {
        heading(content, getString(selectedId == null ? R.string.add_favorite : R.string.edit_favorite));
        content.addView(text(getString(R.string.local_note), 18, MUTED));
        nameInput = field(content, R.string.name_label, R.string.name_hint, draftName, 40, false);
        addressInput = field(content, R.string.address_label, R.string.address_hint, draftAddress, 240, true);
        button(content, getString(R.string.save_favorite), true, this::save);
        button(content, getString(R.string.cancel), false, () -> show(HOME));
    }

    private EditText field(LinearLayout content, int label, int hint, String value, int limit, boolean address) {
        TextView title = text(getString(label), 18, INK);
        title.setTypeface(null, Typeface.BOLD);
        content.addView(title);
        EditText input = new EditText(this);
        input.setId(View.generateViewId());
        input.setSaveEnabled(false);
        title.setLabelFor(input.getId());
        input.setTextSize(20);
        input.setTextColor(INK);
        input.setHintTextColor(MUTED);
        input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | (address ? InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limit)});
        input.setText(value);
        input.setMinHeight(dp(64));
        input.setPadding(dp(16), dp(16), dp(16), dp(16));
        input.setBackground(shape(Color.WHITE));
        LinearLayout.LayoutParams params = fullWidth();
        params.bottomMargin = dp(24);
        content.addView(input, params);
        return input;
    }

    private void save() {
        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        if (name.isEmpty() || name.length() > 40) {
            nameInput.setError(getString(R.string.name_error));
            nameInput.requestFocus();
            return;
        }
        if (address.isEmpty() || address.length() > 240) {
            addressInput.setError(getString(R.string.address_error));
            addressInput.requestFocus();
            return;
        }
        try {
            store.save(new FavoriteDestination(selectedId == null ? UUID.randomUUID().toString()
                    : selectedId, name, address));
            ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(addressInput.getWindowToken(), 0);
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            show(HOME);
        } catch (IllegalStateException exception) {
            Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDelete(FavoriteDestination destination) {
        new AlertDialog.Builder(this).setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, destination.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) -> {
                    try {
                        store.remove(destination.id);
                        show(MANAGE);
                    } catch (IllegalStateException exception) {
                        Toast.makeText(this, R.string.storage_error, Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

    private void selection(LinearLayout content) {
        heading(content, getString(R.string.selected_title));
        try {
            for (FavoriteDestination destination : store.load()) {
                if (destination.id.equals(selectedId)) {
                    LinearLayout selected = card(content);
                    selected.addView(text(destination.name, 26, INK));
                    selected.addView(text(destination.address, 20, MUTED));
                    content.addView(text(getString(R.string.selected_note), 19, MUTED));
                    break;
                }
            }
        } catch (IllegalStateException exception) {
            content.addView(text(getString(R.string.storage_error), 20, INK));
        }
        button(content, getString(R.string.back_to_destinations), true, () -> show(HOME));
    }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout card = column();
        card.setPadding(dp(20), dp(20), dp(20), dp(8));
        card.setBackground(shape(Color.WHITE));
        LinearLayout.LayoutParams params = fullWidth();
        params.bottomMargin = dp(16);
        parent.addView(card, params);
        return card;
    }

    private void heading(LinearLayout content, String label) {
        TextView title = text(label, 34, INK);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            title.setAccessibilityHeading(true);
        }
        content.addView(title);
        setTitle(label);
    }

    private TextView text(String value, int size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setPadding(0, 0, 0, dp(12));
        return text;
    }

    private Button button(LinearLayout parent, String label, boolean primary, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(20);
        button.setAllCaps(false);
        button.setMinHeight(dp(64));
        button.setTextColor(primary ? Color.WHITE : INK);
        button.setPadding(dp(20), dp(18), dp(20), dp(18));
        button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33000000),
                shape(primary ? GREEN : Color.WHITE), null));
        LinearLayout.LayoutParams params = fullWidth();
        params.bottomMargin = dp(12);
        parent.addView(button, params);
        button.setOnClickListener(view -> action.run());
        return button;
    }

    private GradientDrawable shape(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(20));
        shape.setStroke(dp(1), color == GREEN ? GREEN : BORDER);
        return shape;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void space(LinearLayout parent, int height) {
        View spacer = new View(this);
        spacer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(height)));
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
