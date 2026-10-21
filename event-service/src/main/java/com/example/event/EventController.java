package com.example.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
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

    // demo events for the product review
    @Bean
    CommandLineRunner seedEvents() {
        return args -> {
            if (events.count() > 0) return;
            String[][] d = {
                {"Hack the Campus 2026", "Main Auditorium", "2026-11-14T09:00", "24-hour student hackathon with mentors, snacks and prizes. Teams of up to four."},
                {"AI & Robotics Expo", "Engineering Block, Hall B", "2026-11-21T10:30", "Live robot demos, student project showcase and a keynote on applied AI."},
                {"Cultural Night", "Open Air Theatre", "2026-12-05T18:00", "Music, dance and food stalls. Bring friends, doors open at 5:30 PM."},
                {"Career Fair", "Convention Centre", "2026-12-12T11:00", "40+ companies, resume reviews and on-the-spot interviews."}
            };
            for (String[] x : d) {
                Event e = new Event();
                e.name = x[0]; e.location = x[1]; e.dateTime = x[2]; e.description = x[3];
                events.save(e);
            }
        };
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
                "eventId", r.eventId,
                "location", events.findById(r.eventId).map(e -> e.location == null ? "" : e.location).orElse(""),
                "dateTime", events.findById(r.eventId).map(e -> e.dateTime == null ? "" : e.dateTime).orElse(""),
                "passToken", r.passToken)).toList();
    }
}
