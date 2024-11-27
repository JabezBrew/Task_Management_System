package taskmanagement.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import taskmanagement.model.*;
import taskmanagement.service.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
public class AccountController {

    private final UserService userService;
    private final JwtEncoder jwtEncoder;

    public AccountController(UserService userService, JwtEncoder jwtEncoder) {
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/api/auth/token")
    public Map<String, String> token(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        System.out.println("name: " + authentication.getName());

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(60, ChronoUnit.SECONDS))
                .claim("scope", authorities)
                .build();


        return Map.of("token", jwtEncoder.encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue());
    }

    @PostMapping("/api/accounts")
    public void register(@RequestBody @Valid User user) {
        userService.registerUser(user);
    }

    @GetMapping("/api/tasks")
    public List<Map<String, Object>> returnTasks(@RequestParam (required = false) String author,
                                     @RequestParam (required = false) String assignee) {
        return userService.getTasks(author, assignee);
    }

    @PostMapping("/api/tasks")
    public TaskDTO createTask(@RequestBody @Valid Task task) {
        return userService.createTask(task);
    }

    @PutMapping("/api/tasks/{taskId}/assign")
    public TaskDTO assignTask(@PathVariable String taskId, @RequestBody Map<String, String> assignee) {
        return userService.assignTask(taskId, assignee);
    }

    @PutMapping("/api/tasks/{taskId}/status")
    public TaskDTO setStatus(@PathVariable String taskId, @RequestBody Map<String, String> statusChange) {
        return userService.changeStatus(taskId, statusChange);
    }

    @PostMapping("/api/tasks/{taskId}/comments")
    public void sendComment(@PathVariable String taskId, @RequestBody @Valid Comment comment) {
        userService.createComment(taskId, comment);
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    public List<CommentDTO> returnComments(@PathVariable String taskId) {
        return userService.getComments(taskId);
    }
}
