package br.com.vaicomigo.android;

import static org.junit.Assert.*;
import android.app.Instrumentation;
import android.location.Address;
import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Exercises the real Handler wrapper with a fake backend. No service or network calls. */
@RunWith(AndroidJUnit4.class)
public class AndroidAddressResolverTest {
    private static final long WINDOW = 60_000;
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final FakeAddressResolver fake = new FakeAddressResolver();
    private final AtomicLong clock = new AtomicLong(10_000);
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicBoolean onMain = new AtomicBoolean();
    private final AtomicReference<List<ResolvedPlace>> delivered = new AtomicReference<>();
    private final ResolvedPlace point = new ResolvedPlace(-23.55, -46.63, "Rua Fictícia, 10");
    private AndroidAddressResolver resolver;

    @Before public void setUp() {
        resolver = new AndroidAddressResolver(fake, new Handler(Looper.getMainLooper()), WINDOW, clock::get);
    }

    @After public void tearDown() {
        instrumentation.runOnMainSync(resolver::cancel);
        instrumentation.waitForIdleSync();
    }

    private void search(String query) {
        instrumentation.runOnMainSync(() -> resolver.search(query, this::record));
    }

    private void record(List<ResolvedPlace> values) {
        onMain.set(Looper.myLooper() == Looper.getMainLooper());
        calls.incrementAndGet();
        delivered.set(values);
    }

    @Test public void normalizedQueryDeliversOnceOnMainDespiteDuplicateReplies() {
        search("\u00a0 Rua Fictícia, 10 \u2003");
        assertEquals(Collections.singletonList("Rua Fictícia, 10"), fake.queries);
        instrumentation.runOnMainSync(() -> {
            AddressResolver.Callback reply = fake.pending;
            reply.completed(Collections.singletonList(point));
            reply.completed(Collections.singletonList(point));
        });
        instrumentation.waitForIdleSync();
        assertEquals(1, calls.get());
        assertTrue(onMain.get());
        assertSame(point, delivered.get().get(0));
        assertNull(fake.pending);
    }

    @Test public void newSearchIgnoresOldReplyAndKeepsCurrentReply() {
        search("Rua Fictícia, 10");
        AddressResolver.Callback old = fake.pending;
        search("Rua Fictícia, 20");
        instrumentation.runOnMainSync(() -> {
            old.completed(Collections.singletonList(point));
            fake.complete(Collections.emptyList());
        });
        instrumentation.waitForIdleSync();
        assertEquals(1, calls.get());
        assertTrue(delivered.get().isEmpty());
        assertEquals(2, fake.queries.size());
    }

    @Test public void explicitCancellationIgnoresLateReply() {
        search("Rua Fictícia, 10");
        AddressResolver.Callback old = fake.pending;
        instrumentation.runOnMainSync(() -> {
            resolver.cancel();
            old.completed(Collections.singletonList(point));
        });
        instrumentation.waitForIdleSync();
        assertEquals(0, calls.get());
        assertNull(delivered.get());
        assertNull(fake.pending);
    }

    @Test public void replyJustBeforeDeadlineIsAcceptedButExactDeadlineExpires() {
        search("Rua Fictícia, 10");
        instrumentation.runOnMainSync(() -> {
            clock.addAndGet(WINDOW - 1);
            fake.complete(Collections.singletonList(point));
        });
        instrumentation.waitForIdleSync();
        assertSame(point, delivered.get().get(0));
        search("Rua Fictícia, 20");
        instrumentation.runOnMainSync(() -> {
            clock.addAndGet(WINDOW);
            fake.complete(Collections.singletonList(point));
        });
        instrumentation.waitForIdleSync();
        assertEquals(2, calls.get());
        assertTrue(delivered.get().isEmpty());
    }

    @Test public void queuedFreshReplyCannotExtendDeadlineBeforeMainDispatch() {
        instrumentation.runOnMainSync(() -> {
            resolver.search("Rua Fictícia, 10", this::record);
            fake.complete(Collections.singletonList(point));
            // The reply is queued, not dispatched, while this main-thread action is running.
            clock.addAndGet(WINDOW + 1);
        });
        instrumentation.waitForIdleSync();
        assertEquals(1, calls.get());
        assertTrue(delivered.get().isEmpty());
    }

    @Test public void backwardsClockFailsClosed() {
        search("Rua Fictícia, 10");
        instrumentation.runOnMainSync(() -> {
            clock.decrementAndGet();
            fake.complete(Collections.singletonList(point));
        });
        instrumentation.waitForIdleSync();
        assertTrue(delivered.get().isEmpty());
    }

    @Test public void actualHandlerTimeoutDeliversEmptyOnceAndCancelsBackend() throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<AddressResolver.Callback> late = new AtomicReference<>();
        instrumentation.runOnMainSync(() -> {
            resolver.cancel();
            Handler handler = new Handler(Looper.getMainLooper());
            assertThrows(IllegalArgumentException.class, () -> new AndroidAddressResolver(fake, handler, 0));
            resolver = new AndroidAddressResolver(fake, handler, 100);
            resolver.search("Rua Fictícia, 10", values -> { record(values); completed.countDown(); });
            late.set(fake.pending);
        });
        assertTrue("The local Handler timeout must complete", completed.await(5, TimeUnit.SECONDS));
        instrumentation.runOnMainSync(() -> late.get().completed(Collections.singletonList(point)));
        instrumentation.waitForIdleSync();
        assertEquals(1, calls.get());
        assertTrue(onMain.get());
        assertTrue(delivered.get().isEmpty());
        assertNull(fake.pending);
    }

    @Test public void invalidQueriesNeverReachBackend() {
        for (String query : new String[]{null, "\u00a0\u2003", new String(new char[241]).replace('\0', 'x')}) {
            search(query);
            instrumentation.waitForIdleSync();
            assertTrue(delivered.get().isEmpty());
        }
        assertEquals(3, calls.get());
        assertTrue(fake.queries.isEmpty());
    }

    @Test public void providerExceptionBecomesAnEmptyMainThreadResponse() {
        AddressResolver throwing = new AddressResolver() {
            @Override public void search(String address, Callback callback) {
                throw new IllegalStateException("Simulated provider failure");
            }
            @Override public void cancel() { }
        };
        instrumentation.runOnMainSync(() -> {
            resolver.cancel();
            resolver = new AndroidAddressResolver(throwing, new Handler(Looper.getMainLooper()), WINDOW, clock::get);
        });
        search("Rua Fictícia, 10");
        instrumentation.waitForIdleSync();
        assertEquals(1, calls.get());
        assertTrue(onMain.get());
        assertTrue(delivered.get().isEmpty());
    }

    @Test public void mapFiltersIncompleteRecordsAndCapsValidSuggestions() {
        Address missingLatitude = new Address(Locale.ROOT);
        missingLatitude.setLongitude(-46.63);
        missingLatitude.setAddressLine(0, "Lugar fictício");
        Address missingLongitude = new Address(Locale.ROOT);
        missingLongitude.setLatitude(-23.55);
        missingLongitude.setAddressLine(0, "Lugar fictício");
        Address missingLabel = new Address(Locale.ROOT);
        missingLabel.setLatitude(-23.55);
        missingLabel.setLongitude(-46.63);
        List<Address> source = new ArrayList<>(Arrays.asList(null, missingLatitude, missingLongitude,
                missingLabel, address("\u00a0", 0), address(new String(new char[501]).replace('\0', 'x'), 0)));
        for (int index = 0; index < 4; index++) { source.add(address("Lugar fictício " + index, index)); }
        List<ResolvedPlace> result = AndroidAddressResolver.map(source);
        assertEquals(3, result.size());
        assertEquals("Lugar fictício 0", result.get(0).label);
        assertEquals("Lugar fictício 2", result.get(2).label);
        assertEquals(-23.53, result.get(2).latitude, 0.000001);
    }

    @Test public void absentProviderResultsMapToEmptySuggestions() {
        assertTrue(AndroidAddressResolver.map(null).isEmpty());
        assertTrue(AndroidAddressResolver.map(Collections.emptyList()).isEmpty());
    }

    private Address address(String label, int offset) {
        Address address = new Address(Locale.ROOT);
        address.setLatitude(-23.55 + offset * 0.01);
        address.setLongitude(-46.63);
        address.setAddressLine(0, label);
        return address;
    }
}
