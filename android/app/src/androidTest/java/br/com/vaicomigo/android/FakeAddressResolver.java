package br.com.vaicomigo.android;

import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Fictitious, manually delivered responses. Never accesses a geocoding service. */
final class FakeAddressResolver implements AddressResolver {
    final List<String> queries = new ArrayList<>();
    Callback pending;
    int cancellations;

    @Override public void search(String address, Callback callback) {
        queries.add(address);
        pending = callback;
    }

    @Override public void cancel() { pending = null; cancellations++; }

    void complete(List<ResolvedPlace> values) {
        Callback current = pending;
        pending = null;
        if (current == null) { throw new AssertionError("A test response needs an explicit search"); }
        current.completed(values);
    }

    static String choice(ResolvedPlace value) {
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getString(
                R.string.choose_resolved_point, value.label,
                String.format(Locale.ROOT, "%.6f, %.6f", value.latitude, value.longitude));
    }
}
