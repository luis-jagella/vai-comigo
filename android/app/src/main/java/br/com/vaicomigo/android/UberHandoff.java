package br.com.vaicomigo.android;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

/** Opens the rider app without sending a ride request or any trip data. */
final class UberHandoff {
    static Intent intent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("uber://riderequest"))
                .setPackage("com.ubercab");
    }

    static boolean open(Activity activity) {
        try {
            activity.startActivity(intent());
            return true;
        } catch (ActivityNotFoundException | SecurityException exception) {
            return false;
        }
    }
}
