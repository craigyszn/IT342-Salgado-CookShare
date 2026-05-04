package com.salgado.cookshare.features.recipe

// ── Random Recipes Response ───────────────────────────────────────────────────

data class SpoonacularRandomResponse(
    val recipes: List<SpoonacularRecipe>
)

// ── Complex Search Response ───────────────────────────────────────────────────

data class SpoonacularResponse(
    val results: List<SpoonacularRecipe>
)

// ── Recipe ────────────────────────────────────────────────────────────────────

data class SpoonacularRecipe(
    val id: Int,
    val title: String,
    val image: String?,
    val readyInMinutes: Int,
    val preparationMinutes: Int,
    val cookingMinutes: Int,
    val servings: Int,
    val summary: String?,
    val dishTypes: List<String>?,
    val diets: List<String>?,
    val spoonacularScore: Double,
    val aggregateLikes: Int,
    val extendedIngredients: List<SpoonacularIngredient>?,
    val analyzedInstructions: List<SpoonacularInstructionBlock>?,
    val creditsText: String?
)

data class SpoonacularIngredient(
    val original: String
)

data class SpoonacularInstructionBlock(
    val steps: List<SpoonacularStep>
)

data class SpoonacularStep(
    val number: Int,
    val step: String
)