package br.com.vaicomigo.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

/** Opens the rider app. Optional points are sent only after explicit confirmation. */
final class UberHandoff {
    static Intent intent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("uber://riderequest"))
                .setPackage("com.ubercab");
    }

    static boolean open(Activity activity) {
        return open(activity, intent());
    }

    static Intent intent(ResolvedPlace pickup, ResolvedPlace destination) {
        if (pickup == null || destination == null) {
            throw new IllegalArgumentException("Both confirmed points are required");
        }
        Uri.Builder uri = Uri.parse("uber://riderequest").buildUpon();
        point(uri, "pickup", pickup);
        point(uri, "dropoff", destination);
        return new Intent(Intent.ACTION_VIEW, uri.build()).setPackage("com.ubercab");
    }

    private static void point(Uri.Builder uri, String prefix, ResolvedPlace value) {
        uri.appendQueryParameter(prefix + "[latitude]", Double.toString(value.latitude));
        uri.appendQueryParameter(prefix + "[longitude]", Double.toString(value.longitude));
        uri.appendQueryParameter(prefix + "[formatted_address]", value.label);
    }

    static boolean open(Activity activity, ResolvedPlace pickup, ResolvedPlace destination) {
        return open(activity, intent(pickup, destination));
    }

    private static boolean open(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return false;
        }
    }
}
