package br.com.vaicomigo.mobility.uber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.net.URI;

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
}
