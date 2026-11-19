package br.com.vaicomigo.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.ArrayList;
import java.util.List;

/** Foreground-only, time-limited location request using Android's providers. */
final class PickupLocationReader {
    interface Callback {
        void found(double latitude, double longitude, float accuracy, long ageMillis);
        void unavailable();
        void denied();
    }

    private final Context context;
    private final LocationManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LocationListener listener;
    private Runnable timeout;
    private int generation;

    PickupLocationReader(Context context) {
        this.context = context;
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    boolean permitted() {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void start(Callback callback) {
        cancel();
        if (!permitted()) {
            callback.denied();
            return;
        }
        final int request = generation;
        List<String> providers = new ArrayList<>();
        try {
            if (manager == null) {
                callback.unavailable();
                return;
            }
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                providers.add(LocationManager.GPS_PROVIDER);
            }
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                providers.add(LocationManager.NETWORK_PROVIDER);
            }
            if (providers.isEmpty()) {
                callback.unavailable();
                return;
            }
            listener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    if (request != generation) { return; }
                    if (!permitted()) {
                        cancel();
                        callback.denied();
                        return;
                    }
                    long age = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1_000_000;
                    if (location.hasAccuracy() && PickupSession.usable(location.getLatitude(),
                            location.getLongitude(), location.getAccuracy(), age)) {
                        cancel();
                        callback.found(location.getLatitude(), location.getLongitude(), location.getAccuracy(), age);
                    }
                }
                @Override public void onProviderDisabled(String provider) {
                    // Keep the other provider active; the timeout offers the manual alternative.
                }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
            };
            timeout = () -> {
                if (request == generation) {
                    cancel();
                    callback.unavailable();
                }
            };
            handler.postDelayed(timeout, 30_000);
            for (String provider : providers) {
                manager.requestLocationUpdates(provider, 0, 0, listener, Looper.getMainLooper());
            }
        } catch (SecurityException exception) {
            cancel();
            callback.denied();
        } catch (IllegalArgumentException exception) {
            cancel();
            callback.unavailable();
        }
    }

    void cancel() {
        generation++;
        if (timeout != null) {
            handler.removeCallbacks(timeout);
            timeout = null;
        }
        if (listener != null && manager != null) {
            try {
                manager.removeUpdates(listener);
            } catch (SecurityException exception) {
                // A revoked permission must not prevent clearing local state.
            }
            listener = null;
        }
    }
}
