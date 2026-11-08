package br.com.vaicomigo.mobility.uber;

import java.util.Objects;

public record UberLocation(
        double latitude,
        double longitude,
        String nickname,
        String formattedAddress
) {

    public UberLocation {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }

        nickname = requireText(nickname, "nickname");
        formattedAddress = requireText(formattedAddress, "formattedAddress");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
