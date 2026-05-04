package edu.cit.salgado.cookshare.features.user;

public class UpdateProfileRequest {
    private String email;
    private String bio;
    private String location;
    private String favoriteFood;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getFavoriteFood() { return favoriteFood; }
    public void setFavoriteFood(String favoriteFood) { this.favoriteFood = favoriteFood; }
}
