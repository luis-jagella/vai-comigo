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
        if (value == null || value.trim().isEmpty() || value.trim().length() > limit) {
            throw new IllegalArgumentException("Invalid destination text");
        }
        return value.trim();
    }
}
