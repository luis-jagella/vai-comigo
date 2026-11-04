package br.com.vaicomigo.android;

/** A locally entered address; coordinates will be resolved in a later increment. */
public final class FavoriteDestination {
    public final String id;
    public final String name;
    public final String address;

    public FavoriteDestination(String id, String name, String address) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id is required");
        }
        this.id = id;
        this.name = requireText(name, 40);
        this.address = requireText(address, 240);
    }

    private static String requireText(String value, int limit) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty() || normalized.length() > limit) {
            throw new IllegalArgumentException("Invalid destination text");
        }
        return normalized;
    }

    /** Same boundary normalization for the model and the favorite form, including NBSP. */
    static String normalizeText(String value) {
        if (value == null) { return ""; }
        int start = 0, end = value.length();
        while (start < end && isBoundarySpace(value.charAt(start))) { start++; }
        while (end > start && isBoundarySpace(value.charAt(end - 1))) { end--; }
        return value.substring(start, end);
    }

    private static boolean isBoundarySpace(char value) {
        return value <= ' ' || Character.isWhitespace(value) || Character.isSpaceChar(value);
    }
}
