package com.salgado.cookshare.model

// ── DB Recipe ─────────────────────────────────────────────────────────────────

data class DbRecipe(
    val id: String,
    val title: String,
    val description: String?,
    val author: String?,
    val userEmail: String?,
    val image: String?,
    val prepTime: String?,
    val cookTime: String?,
    val servings: Int,
    val difficulty: String?,
    val rating: Double,
    val reviewCount: Int,
    val category: String?,
    val createdAt: String?,
    val tags: List<String>?,
    val ingredients: List<String>?,
    val instructions: List<String>?
)

data class CreateRecipeRequest(
    val id: String,
    val title: String,
    val description: String,
    val author: String,
    val userEmail: String,
    val image: String,
    val prepTime: String,
    val cookTime: String,
    val servings: Int,
    val difficulty: String,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val category: String,
    val tags: List<String>,
    val ingredients: List<String>,
    val instructions: List<String>,
    val createdAt: String
)

data class RateRequest(val stars: Int)

// ── Favorites ─────────────────────────────────────────────────────────────────

data class FavoriteItem(
    val id: Long,
    val userEmail: String,
    val recipeId: String,
    val recipeTitle: String?,
    val recipeImage: String?
)

data class FavoriteCheckResponse(val favorited: Boolean)

data class FavoriteRequest(
    val email: String,
    val recipeId: String,
    val recipeTitle: String,
    val recipeImage: String
)

// ── Comments ──────────────────────────────────────────────────────────────────

data class CommentItem(
    val id: Long,
    val userEmail: String,
    val authorName: String,
    val recipeId: String,
    val text: String,
    val createdAt: String?
)

data class CommentRequest(
    val email: String,
    val authorName: String,
    val recipeId: String,
    val text: String
)

// ── Stats ─────────────────────────────────────────────────────────────────────

data class UserStats(
    val recipesShared: Long,
    val favorites: Long,
    val comments: Long
)

data class UserCountResponse(val count: Long)