package accidentreporting;

import accidentreporting.model.User;
import accidentreporting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Map<String,String> login(@RequestBody Map<String,String> body) {
        String email = body.getOrDefault("email", "");
        String password = body.getOrDefault("password", "");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return Map.of("email", user.getEmail(), "role", user.getRole());
    }

    @GetMapping("/guest")
    public Map<String,String> guest() {
        return Map.of("message", "This is a guest endpoint. No auth required.");
    }
}
