package br.com.vaicomigo.android;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Small local collection. No address is transmitted or written to logs. */
public final class FavoriteStore {
    public static final int MAX_FAVORITES = 5;
    private final SharedPreferences preferences;

    public FavoriteStore(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public List<FavoriteDestination> load() {
        try {
            JSONArray values = new JSONArray(preferences.getString("destinations", "[]"));
            List<FavoriteDestination> result = new ArrayList<>();
            if (values.length() > MAX_FAVORITES) {
                throw new IllegalStateException("Invalid favorite collection");
            }
            for (int index = 0; index < values.length(); index++) {
                JSONObject item = values.getJSONObject(index);
                FavoriteDestination destination = new FavoriteDestination(
                        item.getString("id"), item.getString("name"), item.getString("address"));
                for (FavoriteDestination existing : result) {
                    if (existing.id.equals(destination.id)) {
                        throw new IllegalStateException("Duplicate favorite identity");
                    }
                }
                result.add(destination);
            }
            return result;
        } catch (JSONException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to read favorites", exception);
        }
    }

    public void save(FavoriteDestination destination) {
        List<FavoriteDestination> values = load();
        for (int index = 0; index < values.size(); index++) {
            if (values.get(index).id.equals(destination.id)) {
                values.set(index, destination);
                write(values);
                return;
            }
        }
        if (values.size() >= MAX_FAVORITES) {
            throw new IllegalStateException("Favorite limit reached");
        }
        values.add(destination);
        write(values);
    }

    public void remove(String id) {
        List<FavoriteDestination> values = load();
        values.removeIf(destination -> destination.id.equals(id));
        write(values);
    }

    private void write(List<FavoriteDestination> values) {
        JSONArray json = new JSONArray();
        try {
            for (FavoriteDestination value : values) {
                json.put(new JSONObject().put("id", value.id).put("name", value.name)
                        .put("address", value.address));
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Unable to encode favorites", exception);
        }
        if (!preferences.edit().putString("destinations", json.toString()).commit()) {
            throw new IllegalStateException("Unable to persist favorites");
        }
    }
}
