package br.com.vaicomigo.status;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    @GetMapping
    public ApplicationStatus status() {
        return new ApplicationStatus("vai-comigo", "UP", Instant.now());
    }

    public record ApplicationStatus(String service, String status, Instant timestamp) {
    }
}
