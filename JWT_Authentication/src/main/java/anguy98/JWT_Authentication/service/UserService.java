package anguy98.JWT_Authentication.service;

import org.springframework.stereotype.Service;

import anguy98.JWT_Authentication.repository.UserRepository;
import anguy98.JWT_Authentication.model.User;
import java.util.List;
import java.util.ArrayList;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;   
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
}
