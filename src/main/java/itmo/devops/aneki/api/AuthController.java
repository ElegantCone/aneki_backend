package itmo.devops.aneki.api;

import itmo.devops.aneki.model.User;
import itmo.devops.aneki.service.AuthService;
import itmo.devops.aneki.service.AuthService.AuthResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        AuthResult result = authService.login(request.email(), request.password());
        return new AuthResponse(result.token(), toUserDto(result.user()));
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request) {
        AuthResult result = authService.signup(request.name(), request.email(), request.password());
        return new AuthResponse(result.token(), toUserDto(result.user()));
    }

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        User user = authService.requireUserFromAuthorizationHeader(authorizationHeader);
        return new MeResponse(toUserDto(user));
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.id(), user.name(), user.email());
    }
}
