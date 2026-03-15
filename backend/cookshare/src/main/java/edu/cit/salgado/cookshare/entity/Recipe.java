package edu.cit.salgado.cookshare.entity;

import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String cookTime;

    private String difficulty;

    private String author;

    private int servings;

    private double rating;

    private int reviewCount;

    private String imageUrl;

    @ElementCollection
    private List<String> tags;

    // getters and setters
}
