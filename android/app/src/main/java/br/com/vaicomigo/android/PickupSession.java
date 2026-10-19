package br.com.vaicomigo.android;

import java.util.Objects;
import java.util.function.LongSupplier;

/** Transient pickup state. Never serialize this object or its position. */
public final class PickupSession {
    private static final long MAX_LOCATION_AGE_MILLIS = 120_000;
    public enum State { EXPLANATION, PERMISSION, LOCATING, DENIED, UNAVAILABLE, LOCATED, MANUAL, REVIEW }
    private State state = State.EXPLANATION;
    private Position position;
    private String address;
    private final LongSupplier clock;
    private long positionReceivedAtMillis;
    private long positionAgeMillis;

    public PickupSession() {
        this(() -> System.nanoTime() / 1_000_000);
    }

    PickupSession(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    public static final class Position {
        public final double latitude, longitude;
        public final float accuracyMeters;

        private Position(double latitude, double longitude, float accuracyMeters) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracyMeters = accuracyMeters;
        }
    }

    public State state() { return state; }
    public Position position() { return position; }
    public String address() { return address; }

    public void useLocation(boolean permissionGranted) {
        clear();
        state = permissionGranted ? State.LOCATING : State.PERMISSION;
    }

    public void permissionResult(boolean granted) {
        if (state == State.PERMISSION) {
            state = granted ? State.LOCATING : State.DENIED;
        }
    }

    public void permissionRevoked() {
        clear();
        state = State.DENIED;
    }

    public void unavailable() {
        if (state == State.LOCATING) {
            clear();
            state = State.UNAVAILABLE;
        }
    }

    public static boolean usable(double latitude, double longitude, float accuracy, long ageMillis) {
        return Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
                && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180
                && Float.isFinite(accuracy) && accuracy > 0 && accuracy <= 100
                && ageMillis >= 0 && ageMillis <= MAX_LOCATION_AGE_MILLIS;
    }

    public void located(double latitude, double longitude, float accuracy, long ageMillis) {
        if (state == State.LOCATING && usable(latitude, longitude, accuracy, ageMillis)) {
            position = new Position(latitude, longitude, accuracy);
            positionReceivedAtMillis = clock.getAsLong();
            positionAgeMillis = ageMillis;
            state = State.LOCATED;
        }
    }

    public void manual() {
        clear();
        state = State.MANUAL;
    }

    public void reviewManual(String value) {
        if (state != State.MANUAL || value == null || value.trim().isEmpty() || value.trim().length() > 240) {
            throw new IllegalArgumentException("Invalid pickup address");
        }
        address = value.trim();
        state = State.REVIEW;
    }

    public boolean reviewLocation() {
        if (state != State.LOCATED || position == null) {
            throw new IllegalStateException("Pickup position unavailable");
        }
        long waitingMillis = clock.getAsLong() - positionReceivedAtMillis;
        if (waitingMillis < 0 || waitingMillis > MAX_LOCATION_AGE_MILLIS - positionAgeMillis) {
            clear();
            state = State.UNAVAILABLE;
            return false;
        }
        state = State.REVIEW;
        return true;
    }

    public void reset() {
        clear();
        state = State.EXPLANATION;
    }

    private void clear() {
        position = null;
        address = null;
        positionReceivedAtMillis = 0;
        positionAgeMillis = 0;
    }
}
