package com.example.pass;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
public class PassController {
    private final PassRepository passes;

    public PassController(PassRepository passes) {
        this.passes = passes;
    }

    @PostMapping("/passes")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Pass p = new Pass();
        p.registrationId = ((Number) body.get("registrationId")).longValue();
        p.token = UUID.randomUUID().toString();
        passes.save(p);
        return Map.of("token", p.token, "registrationId", p.registrationId);
    }

    private Pass find(String token) {
        Pass p = passes.findByToken(token);
        if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no such pass");
        return p;
    }

    @GetMapping("/passes/{token}/verify")
    public Map<String, Object> verify(@PathVariable String token) {
        return Map.of("registrationId", find(token).registrationId);
    }

    @GetMapping(value = "/passes/{token}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable String token) throws Exception {
        find(token);
        BitMatrix m = new QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(m, "PNG", out);
        return out.toByteArray();
    }
}
