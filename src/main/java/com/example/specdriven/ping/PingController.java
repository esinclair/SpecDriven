package com.example.specdriven.ping;

import com.example.specdriven.api.PingApi;
import com.example.specdriven.api.model.PingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController implements PingApi {

    @Override
    public ResponseEntity<PingResponse> ping() {
        PingResponse response = new PingResponse();
        response.setMessage("pong");
        return ResponseEntity.ok(response);
    }
}
