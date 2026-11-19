package br.com.vaicomigo.mobility.uber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class UberDeepLinkBuilderTest {

    @Test
    void shouldBuildStandardDeepLinkWithEncodedLocations() {
        UberRideRequest request = new UberRideRequest(
                new UberLocation(-23.55052, -46.633308, "Embarque", "Praça da Sé, São Paulo - SP"),
                new UberLocation(-23.561414, -46.655881, "Consulta médica", "Av. Paulista, 1000 - São Paulo - SP")
        );

        URI deepLink = UberDeepLinkBuilder.build(request);

        assertThat(deepLink.toString())
                .startsWith("uber://riderequest?")
                .contains("pickup%5Blatitude%5D=-23.55052")
                .contains("pickup%5Bnickname%5D=Embarque")
                .contains("pickup%5Bformatted_address%5D=Pra%C3%A7a%20da%20S%C3%A9%2C%20S%C3%A3o%20Paulo%20-%20SP")
                .contains("dropoff%5Bnickname%5D=Consulta%20m%C3%A9dica")
                .contains("dropoff%5Bformatted_address%5D=Av.%20Paulista%2C%201000%20-%20S%C3%A3o%20Paulo%20-%20SP");
    }

    @Test
    void shouldRejectInvalidCoordinates() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UberLocation(-91, 0, "Casa", "Rua de teste, 1"))
                .withMessage("latitude must be between -90 and 90");
    }

    @Test
    void shouldRejectBlankAddressDetails() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UberLocation(0, 0, " ", "Rua de teste, 1"))
                .withMessage("nickname must not be blank");
    }

    @Test
    void shouldKeepReservedCharactersInsideExactlyEightLocationParameters() {
        String nickname = "Embarque & x=1+2#fim/%";
        String address = "Rua Fictícia, 1?porta=A&B";
        URI link = UberDeepLinkBuilder.build(new UberRideRequest(
                new UberLocation(0, 0, nickname, address),
                new UberLocation(1, 1, "Destino de teste", "Rua Fictícia, 2")));

        Map<String, String> parameters = Arrays.stream(link.getRawQuery().split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(
                        pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)));

        assertThat(link.getFragment()).isNull();
        assertThat(parameters).containsOnlyKeys("pickup[latitude]", "pickup[longitude]",
                "pickup[nickname]", "pickup[formatted_address]", "dropoff[latitude]",
                "dropoff[longitude]", "dropoff[nickname]", "dropoff[formatted_address]");
        assertThat(parameters.get("pickup[nickname]")).isEqualTo(nickname);
        assertThat(parameters.get("pickup[formatted_address]")).isEqualTo(address);
    }

    @Test
    void shouldRejectNonFiniteAndOutOfRangeCoordinates() {
        for (double latitude : new double[]{Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -90.1, 90.1}) {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> new UberLocation(latitude, 0, "Teste", "Rua Fictícia, 1"));
        }
        for (double longitude : new double[]{Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -180.1, 180.1}) {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> new UberLocation(0, longitude, "Teste", "Rua Fictícia, 1"));
        }
        assertThat(new UberLocation(-90, -180, "Teste", "Rua Fictícia, 1").latitude()).isEqualTo(-90);
        assertThat(new UberLocation(90, 180, "Teste", "Rua Fictícia, 1").longitude()).isEqualTo(180);
    }
}
