package br.com.vaicomigo.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Explicit, transient choices. Even a single result needs a user selection. */
final class PrefillSession {
    enum Stage { IDLE, CONSENT, SEARCHING_PICKUP, PICKUP_CHOICES,
        SEARCHING_DESTINATION, DESTINATION_CHOICES, READY, FAILED }
    private Stage stage = Stage.IDLE;
    private List<ResolvedPlace> choices = Collections.emptyList();
    private ResolvedPlace pickup, destination;

    Stage stage() { return stage; }
    List<ResolvedPlace> choices() { return choices; }
    ResolvedPlace pickup() { return pickup; }
    ResolvedPlace destination() { return destination; }
    boolean confirmed() { return stage == Stage.READY && pickup != null && destination != null; }

    void begin() { reset(); stage = Stage.CONSENT; }

    void startPickupSearch() {
        require(Stage.CONSENT);
        stage = Stage.SEARCHING_PICKUP;
    }

    void pickupResults(List<ResolvedPlace> values) {
        require(Stage.SEARCHING_PICKUP);
        accept(values, Stage.PICKUP_CHOICES);
    }

    void choosePickup(int index) {
        require(Stage.PICKUP_CHOICES);
        pickup = choices.get(index);
        choices = Collections.emptyList();
        stage = Stage.SEARCHING_DESTINATION;
    }

    void destinationResults(List<ResolvedPlace> values) {
        require(Stage.SEARCHING_DESTINATION);
        accept(values, Stage.DESTINATION_CHOICES);
    }

    void chooseDestination(int index) {
        require(Stage.DESTINATION_CHOICES);
        destination = choices.get(index);
        choices = Collections.emptyList();
        stage = Stage.READY;
    }

    void reset() {
        choices = Collections.emptyList();
        pickup = null;
        destination = null;
        stage = Stage.IDLE;
    }

    private void accept(List<ResolvedPlace> values, Stage next) {
        List<ResolvedPlace> valid = new ArrayList<>();
        if (values != null) {
            for (ResolvedPlace value : values) {
                if (value != null && valid.size() < 3) { valid.add(value); }
            }
        }
        if (valid.isEmpty()) {
            reset();
            stage = Stage.FAILED;
        } else {
            choices = Collections.unmodifiableList(valid);
            stage = next;
        }
    }

    private void require(Stage expected) {
        if (stage != expected) { throw new IllegalStateException("Unexpected prefill stage"); }
    }
}
