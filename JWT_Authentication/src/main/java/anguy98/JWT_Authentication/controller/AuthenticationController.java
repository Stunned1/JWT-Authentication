package anguy98.JWT_Authentication.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import anguy98.JWT_Authentication.service.AuthenticationService;
import anguy98.JWT_Authentication.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import anguy98.JWT_Authentication.dto.RegisterUserDto;
import anguy98.JWT_Authentication.model.User;
import anguy98.JWT_Authentication.dto.LoginUserDto;
import anguy98.JWT_Authentication.responses.LoginResponse;
import anguy98.JWT_Authentication.dto.VerifyUserDto;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/auth")    
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User user = authenticationService.signUp(registerUserDto);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDto loginUserDto) {
        User user = authenticationService.authenticate(loginUserDto);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new LoginResponse(token, "1h"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("User verified successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<?> resendVerificationEmail(@RequestParam String email) {
        try {
            authenticationService.resendVerificationEmail(email);
            return ResponseEntity.ok("Verification email resent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
