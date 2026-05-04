package edu.cit.salgado.cookshare.features.user;

import org.springframework.stereotype.Service;

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