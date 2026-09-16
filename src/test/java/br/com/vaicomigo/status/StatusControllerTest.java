package br.com.vaicomigo.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StatusControllerTest {

    private final StatusController controller = new StatusController();

    @Test
    void shouldReportApplicationAsAvailable() {
        var response = controller.status();

        assertThat(response.service()).isEqualTo("vai-comigo");
        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.timestamp()).isNotNull();
    }
}
