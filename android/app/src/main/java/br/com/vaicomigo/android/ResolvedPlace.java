package br.com.vaicomigo.android;

/** A suggestion, not proof of an address. Keep only in the foreground trip. */
final class ResolvedPlace {
    final double latitude, longitude;
    final String label;

    ResolvedPlace(double latitude, double longitude, String label) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid place coordinates");
        }
        String normalized = FavoriteDestination.normalizeText(label);
        if (normalized.isEmpty() || normalized.length() > 500) {
            throw new IllegalArgumentException("Invalid place label");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = normalized;
    }
}
