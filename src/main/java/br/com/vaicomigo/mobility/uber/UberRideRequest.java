package br.com.vaicomigo.mobility.uber;

import java.util.Objects;

public record UberRideRequest(UberLocation pickup, UberLocation dropoff) {

    public UberRideRequest {
        Objects.requireNonNull(pickup, "pickup must not be null");
        Objects.requireNonNull(dropoff, "dropoff must not be null");
    }
}
