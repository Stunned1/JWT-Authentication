package anguy98.JWT_Authentication.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import anguy98.JWT_Authentication.repository.UserRepository;
import anguy98.JWT_Authentication.dto.RegisterUserDto;
import anguy98.JWT_Authentication.dto.LoginUserDto;
import anguy98.JWT_Authentication.dto.VerifyUserDto;
import anguy98.JWT_Authentication.model.User;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Optional;
import jakarta.mail.MessagingException;

@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    public AuthenticationService(
        UserRepository userRepository, 
        PasswordEncoder passwordEncoder, 
        AuthenticationManager authenticationManager, 
        EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }
    
    public User signUp(RegisterUserDto registerUserDto) {
        User user = new User(registerUserDto.getUsername(), registerUserDto.getEmail(), passwordEncoder.encode(registerUserDto.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(10));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto loginUserDto) {
        User user = userRepository.findByEmail(loginUserDto.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("User is not verified");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword()));
        return user;
    }

    public void verifyUser(VerifyUserDto verifyUserDto) {
        Optional<User> user = userRepository.findByEmail(verifyUserDto.getEmail());
        if (user.isPresent()) {
            User existingUser = user.get();
            if (existingUser.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (existingUser.getVerificationCode().equals(verifyUserDto.getCode())) {
                existingUser.setVerificationCode(null);
                existingUser.setVerificationExpiration(null);
                existingUser.setEnabled(true);
                userRepository.save(existingUser);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            User existingUser = user.get();
            if (existingUser.isEnabled()) {
                throw new RuntimeException("User is already verified");
            }
            existingUser.setVerificationCode(generateVerificationCode());
            existingUser.setVerificationExpiration(LocalDateTime.now().plusMinutes(10));
            sendVerificationEmail(existingUser);
            userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(1000000)); // Generate a 6-digit verification code
    }

    private void sendVerificationEmail(User user) {
        String subject = "Verify your email";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>" +
                            "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px; text-align: center;'>" +
                            "  <div style='max-width: 400px; margin: auto; background: #ffffff; border-radius: 8px; " +
                            "              padding: 30px; box-shadow: 0 4px 12px rgba(0,0,0,0.08);'>" +
                            "    <h2 style='color: #333333; margin-bottom: 10px;'>Email Verification</h2>" +
                            "    <p style='color: #555555; font-size: 15px; margin-bottom: 20px;'>Use the code below to verify your email address:</p>" +
                            "    <div style='font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 4px; " +
                            "                padding: 12px 20px; background: #f9f9f9; border-radius: 6px; display: inline-block;'>" +
                            "      " + verificationCode + 
                            "    </div>" +
                            "    <p style='color: #888888; font-size: 12px; margin-top: 20px;'>This code will expire in 10 minutes.</p>" +
                            "  </div>" +
                            "</body>" +
                            "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();    
            throw new RuntimeException("Failed to send verification email");
        }
    }
    
}
