package edu.cit.salgado.cookshare.features.nutrition;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/recipes")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    // GET /api/recipes/{id}/nutrition
    @GetMapping("/{id}/nutrition")
    public ResponseEntity<NutritionData> getNutrition(@PathVariable String id) {
        try {
            NutritionData data = nutritionService.getNutrition(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}