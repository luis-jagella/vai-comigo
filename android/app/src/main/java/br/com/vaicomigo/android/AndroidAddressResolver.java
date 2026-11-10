package br.com.vaicomigo.android;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Main-thread results, bounded waiting, no logs. Cancellation cannot recall server data. */
final class AndroidAddressResolver implements AddressResolver {
    private final AddressResolver backend;
    private final Handler handler;
    private final long timeoutMillis;
    private final LongSupplier clock;
    private long startedAtMillis;
    private volatile int generation;
    private Callback callback;

    AndroidAddressResolver(Context context) {
        this(new PlatformLookup(context.getApplicationContext()), new Handler(Looper.getMainLooper()), 30_000);
    }

    AndroidAddressResolver(AddressResolver backend, Handler handler, long timeoutMillis) {
        this(backend, handler, timeoutMillis, SystemClock::elapsedRealtime);
    }

    AndroidAddressResolver(AddressResolver backend, Handler handler, long timeoutMillis, LongSupplier clock) {
        if (timeoutMillis <= 0) { throw new IllegalArgumentException("Positive lookup timeout required"); }
        this.backend = Objects.requireNonNull(backend);
        this.handler = Objects.requireNonNull(handler);
        this.clock = Objects.requireNonNull(clock);
        this.timeoutMillis = timeoutMillis;
    }

    @Override public void search(String address, Callback callback) {
        cancel();
        int request = generation;
        this.callback = callback;
        startedAtMillis = clock.getAsLong();
        String query = FavoriteDestination.normalizeText(address);
        if (query.isEmpty() || query.length() > 240) {
            deliver(request, Collections.emptyList());
            return;
        }
        handler.postDelayed(() -> finish(request, Collections.emptyList()), timeoutMillis);
        try {
            backend.search(query, values -> deliver(request, values));
        } catch (RuntimeException exception) {
            deliver(request, Collections.emptyList());
        }
    }

    private void deliver(int request, List<ResolvedPlace> values) {
        if (request != generation) { return; }
        handler.post(() -> finish(request, values));
    }

    /** Main thread only. Queue ordering must not extend the response deadline. */
    private void finish(int request, List<ResolvedPlace> values) {
        if (request != generation || callback == null) { return; }
        long elapsedMillis = clock.getAsLong() - startedAtMillis;
        boolean expired = elapsedMillis < 0 || elapsedMillis >= timeoutMillis;
        Callback current = callback;
        cancel();
        current.completed(expired || values == null ? Collections.emptyList() : values);
    }

    @Override public void cancel() {
        generation++;
        callback = null;
        startedAtMillis = 0;
        handler.removeCallbacksAndMessages(null);
        backend.cancel();
    }

    static List<ResolvedPlace> map(List<Address> addresses) {
        List<ResolvedPlace> result = new ArrayList<>();
        if (addresses == null) { return result; }
        for (Address address : addresses) {
            if (result.size() == 3) { break; }
            if (address == null || !address.hasLatitude() || !address.hasLongitude()
                    || address.getMaxAddressLineIndex() < 0) { continue; }
            try {
                result.add(new ResolvedPlace(address.getLatitude(), address.getLongitude(), address.getAddressLine(0)));
            } catch (IllegalArgumentException exception) {
                // Unusable provider data must not become a selectable point.
            }
        }
        return result;
    }

    private static final class PlatformLookup implements AddressResolver {
        // No queue: a stuck legacy binder call cannot accumulate addresses or spawn more threads.
        private static final ThreadPoolExecutor LEGACY = new ThreadPoolExecutor(0, 1, 30, TimeUnit.SECONDS,
                new SynchronousQueue<>(), task -> {
                    Thread thread = new Thread(task, "address-lookup");
                    thread.setDaemon(true);
                    return thread;
                });
        private final Context context;
        private Future<?> pending;

        PlatformLookup(Context context) { this.context = context; }

        @Override public void search(String address, Callback callback) {
            if (!Geocoder.isPresent()) {
                callback.completed(Collections.emptyList());
                return;
            }
            Geocoder geocoder = new Geocoder(context, Locale.forLanguageTag("pt-BR"));
            if (Build.VERSION.SDK_INT >= 33) {
                geocoder.getFromLocationName(address, 3, new Geocoder.GeocodeListener() {
                    @Override public void onGeocode(List<Address> values) { callback.completed(map(values)); }
                    @Override public void onError(String message) { callback.completed(Collections.emptyList()); }
                });
            } else {
                pending = LEGACY.submit(() -> legacySearch(geocoder, address, callback));
            }
        }

        @SuppressWarnings("deprecation")
        private void legacySearch(Geocoder geocoder, String address, Callback callback) {
            try {
                callback.completed(map(geocoder.getFromLocationName(address, 3)));
            } catch (IOException | RuntimeException exception) {
                callback.completed(Collections.emptyList());
            }
        }

        @Override public void cancel() {
            if (pending != null) { pending.cancel(true); pending = null; }
        }
    }
}
