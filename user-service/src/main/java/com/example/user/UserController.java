package com.example.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class UserController {
    private static final long ADMIN_ID = 1;

    private final UserRepository users;
    private final AttendanceRepository attendances;
    private final RestClient rest = RestClient.create();

    @Value("${pass.service.url}")
    private String passUrl;

    public UserController(UserRepository users, AttendanceRepository attendances) {
        this.users = users;
        this.attendances = attendances;
    }

    // first user ever created gets id 1 = admin
    @Bean
    CommandLineRunner seedAdmin() {
        return args -> {
            if (users.count() == 0) {
                User a = new User();
                a.username = "admin";
                a.password = "admin";
                users.save(a);
            }
        };
    }

    private User me(String token) {
        User u = token == null ? null : users.findByToken(token);
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
        return u;
    }

    private Map<String, Object> view(User u) {
        return Map.of("id", u.id, "username", u.username, "organizer", u.organizer,
                "admin", u.id == ADMIN_ID);
    }

    @PostMapping("/users/signup")
    public Map<String, Object> signup(@RequestBody Map<String, String> body) {
        if (users.findByUsername(body.get("username")) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username taken");
        User u = new User();
        u.username = body.get("username");
        u.password = body.get("password");
        return view(users.save(u));
    }

    @PostMapping("/users/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        User u = users.findByUsername(body.get("username"));
        if (u == null || !u.password.equals(body.get("password")))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials");
        u.token = UUID.randomUUID().toString();
        users.save(u);
        return Map.of("token", u.token, "user", view(u));
    }

    @GetMapping("/users/me")
    public Map<String, Object> me2(@RequestHeader(value = "Authorization", required = false) String token) {
        return view(me(token));
    }

    @GetMapping("/users")
    public List<Map<String, Object>> list(@RequestHeader(value = "Authorization", required = false) String token) {
        if (me(token).id != ADMIN_ID) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
        return users.findAll().stream().map(this::view).toList();
    }

    @PutMapping("/users/{id}/organizer")
    public Map<String, Object> makeOrganizer(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (me(token).id != ADMIN_ID) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        u.organizer = true;
        return view(users.save(u));
    }

    private User organizer(String token) {
        User u = me(token);
        if (!u.organizer && u.id != ADMIN_ID)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "organizers only");
        return u;
    }

    @PostMapping("/attendance/scan")
    public Attendance scan(@RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        User scanner = organizer(token);
        String passToken = body.get("token");
        Map<?, ?> pass;
        try {
            pass = rest.get().uri(passUrl + "/passes/" + passToken + "/verify").retrieve().body(Map.class);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid pass");
        }
        if (attendances.existsByPassToken(passToken))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "already checked in");
        Attendance a = new Attendance();
        a.passToken = passToken;
        a.registrationId = ((Number) pass.get("registrationId")).longValue();
        a.scannedBy = scanner.id;
        a.scannedAt = LocalDateTime.now();
        return attendances.save(a);
    }

    @GetMapping("/attendance")
    public List<Attendance> attendance(@RequestHeader(value = "Authorization", required = false) String token) {
        organizer(token);
        return attendances.findAll();
    }
}
