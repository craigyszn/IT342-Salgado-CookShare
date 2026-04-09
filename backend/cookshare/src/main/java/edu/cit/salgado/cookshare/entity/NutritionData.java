package edu.cit.salgado.cookshare.entity;

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
@Table(name = "nutrition_data")
@Getter
@Setter
@NoArgsConstructor
public class NutritionData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String recipeId;

    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;

    public NutritionData(String recipeId, double calories, double protein,
                         double carbs, double fat, double fiber) {
        this.recipeId = recipeId;
        this.calories = calories;
        this.protein  = protein;
        this.carbs    = carbs;
        this.fat      = fat;
        this.fiber    = fiber;
    }
}