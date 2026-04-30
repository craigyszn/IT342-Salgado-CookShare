package edu.cit.salgado.cookshare.features.rating;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ratings")
@Getter
@Setter
@NoArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String recipeId;

    @Column(nullable = false)
    private int ratingValue;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Rating(String userEmail, String recipeId, int ratingValue) {
        this.userEmail  = userEmail;
        this.recipeId   = recipeId;
        this.ratingValue = ratingValue;
        this.createdAt  = LocalDateTime.now();
    }
}