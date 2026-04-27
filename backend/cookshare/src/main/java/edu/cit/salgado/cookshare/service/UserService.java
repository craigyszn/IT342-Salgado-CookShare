package edu.cit.salgado.cookshare.service;

import org.springframework.stereotype.Service;

import edu.cit.salgado.cookshare.dto.UpdateProfileRequest;
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

    // ── Update profile fields ─────────────────────────────────────────────
    public User updateProfile(UpdateProfileRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) throw new RuntimeException("User not found");
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getFavoriteFood() != null) user.setFavoriteFood(request.getFavoriteFood());
        return userRepository.save(user);
    }
}