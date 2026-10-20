package com.example.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class EventController {
    private final EventRepository events;
    private final RegistrationRepository registrations;
    private final RestClient rest = RestClient.create();

    @Value("${user.service.url}")
    private String userUrl;
    @Value("${pass.service.url}")
    private String passUrl;

    public EventController(EventRepository events, RegistrationRepository registrations) {
        this.events = events;
        this.registrations = registrations;
    }

    private Map<?, ?> me(String token) {
        try {
            return rest.get().uri(userUrl + "/users/me").header("Authorization", token == null ? "" : token)
                    .retrieve().body(Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
        }
    }

    @GetMapping("/events")
    public List<Event> list() {
        return events.findAll();
    }

    @PostMapping("/events")
    public Event create(@RequestBody Event e, @RequestHeader(value = "Authorization", required = false) String token) {
        if (!Boolean.TRUE.equals(me(token).get("admin")))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
        e.id = null;
        return events.save(e);
    }

    @PostMapping("/events/{id}/register")
    public Registration register(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = ((Number) me(token).get("id")).longValue();
        events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no such event"));
        if (registrations.existsByEventIdAndUserId(id, userId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already registered");
        Registration r = new Registration();
        r.eventId = id;
        r.userId = userId;
        registrations.save(r);
        Map<?, ?> pass = rest.post().uri(passUrl + "/passes").body(Map.of("registrationId", r.id))
                .retrieve().body(Map.class);
        r.passToken = (String) pass.get("token");
        return registrations.save(r);
    }

    @GetMapping("/registrations/mine")
    public List<Map<String, Object>> mine(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = ((Number) me(token).get("id")).longValue();
        return registrations.findByUserId(userId).stream().map(r -> Map.<String, Object>of(
                "registrationId", r.id,
                "eventName", events.findById(r.eventId).map(e -> e.name).orElse("?"),
                "passToken", r.passToken)).toList();
    }
}
