package br.com.eduardoenemark.rwt.app.server.resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConectivityResource {

    @GetMapping("/ping")
    public ResponseEntity<String> handlePingRequest() {
        // Respond to ping request
        return ResponseEntity.ok("pong!");
    }
}
