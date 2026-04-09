package edu.cit.salgado.cookshare.service;

import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.entity.User;
import edu.cit.salgado.cookshare.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User updateProfilePhoto(String email, String photoUrl) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) throw new RuntimeException("User not found");
        user.setProfilePhotoUrl(photoUrl);
        return userRepository.save(user);
    }
}
