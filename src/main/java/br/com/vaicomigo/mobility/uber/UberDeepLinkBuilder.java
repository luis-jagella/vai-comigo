package br.com.vaicomigo.mobility.uber;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builds a standard Uber ride-request deep link without requesting or confirming a ride.
 */
public final class UberDeepLinkBuilder {

    private static final String DEEP_LINK_BASE = "uber://riderequest";

    private UberDeepLinkBuilder() {
    }

    public static URI build(UberRideRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        Map<String, String> parameters = new LinkedHashMap<>();
        addLocation(parameters, "pickup", request.pickup());
        addLocation(parameters, "dropoff", request.dropoff());

        String query = parameters.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));

        return URI.create(DEEP_LINK_BASE + "?" + query);
    }

    private static void addLocation(Map<String, String> parameters, String prefix, UberLocation location) {
        parameters.put(prefix + "[latitude]", Double.toString(location.latitude()));
        parameters.put(prefix + "[longitude]", Double.toString(location.longitude()));
        parameters.put(prefix + "[nickname]", location.nickname());
        parameters.put(prefix + "[formatted_address]", location.formattedAddress());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
