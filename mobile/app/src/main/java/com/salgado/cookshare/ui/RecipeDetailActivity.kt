package com.salgado.cookshare.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.salgado.cookshare.R
import com.salgado.cookshare.api.RetrofitClient
import com.salgado.cookshare.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var ivRecipeImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvPrepTime: TextView
    private lateinit var tvCookTime: TextView
    private lateinit var tvServings: TextView
    private lateinit var tvRating: TextView
    private lateinit var tagsContainer: LinearLayout
    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var instructionsContainer: LinearLayout
    private lateinit var starsContainer: LinearLayout
    private lateinit var tvCommentsTitle: TextView
    private lateinit var etComment: EditText
    private lateinit var btnPostComment: Button
    private lateinit var commentsContainer: LinearLayout
    private lateinit var tvAuthor: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var btnFavorite: ImageView

    private var userRating = 0
    private var hasRated = false
    private var isFavorited = false
    private var recipeId = ""
    private var liveRating = 0.0
    private var liveReviewCount = 0
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)
        initViews()
        setupToolbar()
        loadRecipeData()
        setupStars()
        setupComments()
        loadComments()
        loadFavoriteStatus()
    }

    private fun initViews() {
        ivRecipeImage      = findViewById(R.id.ivRecipeImage)
        tvTitle            = findViewById(R.id.tvTitle)
        tvDifficulty       = findViewById(R.id.tvDifficulty)
        tvDescription      = findViewById(R.id.tvDescription)
        tvPrepTime         = findViewById(R.id.tvPrepTime)
        tvCookTime         = findViewById(R.id.tvCookTime)
        tvServings         = findViewById(R.id.tvServings)
        tvRating           = findViewById(R.id.tvRating)
        tagsContainer      = findViewById(R.id.tagsContainer)
        ingredientsContainer = findViewById(R.id.ingredientsContainer)
        instructionsContainer = findViewById(R.id.instructionsContainer)
        starsContainer     = findViewById(R.id.starsContainer)
        tvCommentsTitle    = findViewById(R.id.tvCommentsTitle)
        etComment          = findViewById(R.id.etComment)
        btnPostComment     = findViewById(R.id.btnPostComment)
        commentsContainer  = findViewById(R.id.commentsContainer)
        tvAuthor           = findViewById(R.id.tvAuthor)
        toolbar            = findViewById(R.id.toolbar)
        btnFavorite        = findViewById(R.id.btnFavorite)
        prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadRecipeData() {
        recipeId      = intent.getStringExtra("recipe_id") ?: ""
        val title     = intent.getStringExtra("recipe_title") ?: "Recipe"
        val imageUrl  = intent.getStringExtra("recipe_image") ?: ""
        val description = intent.getStringExtra("recipe_description") ?: ""
        val difficulty = intent.getStringExtra("recipe_difficulty") ?: "Easy"
        val prepTime  = intent.getStringExtra("recipe_prep_time") ?: "N/A"
        val cookTime  = intent.getStringExtra("recipe_cook_time") ?: "N/A"
        val servings  = intent.getIntExtra("recipe_servings", 4)
        val rating    = intent.getDoubleExtra("recipe_rating", 0.0)
        val reviewCount = intent.getIntExtra("recipe_review_count", 0)
        val author    = intent.getStringExtra("recipe_author") ?: "Spoonacular"
        val tags      = intent.getStringArrayListExtra("recipe_tags") ?: arrayListOf()
        val ingredients = intent.getStringArrayListExtra("recipe_ingredients") ?: arrayListOf()
        val instructions = intent.getStringArrayListExtra("recipe_instructions") ?: arrayListOf()

        liveRating = rating
        liveReviewCount = reviewCount

        Glide.with(this).load(imageUrl).centerCrop()
            .placeholder(R.drawable.ic_chef_hat).into(ivRecipeImage)

        tvTitle.text       = title
        tvDescription.text = description
        tvPrepTime.text    = prepTime
        tvCookTime.text    = cookTime
        tvServings.text    = servings.toString()
        updateRatingDisplay()
        tvAuthor.text = "Recipe by $author  •  $reviewCount likes"

        tvDifficulty.text = difficulty
        when (difficulty) {
            "Easy"   -> { tvDifficulty.setTextColor(0xFF15803D.toInt()); tvDifficulty.setBackgroundResource(R.drawable.bg_badge_easy) }
            "Medium" -> { tvDifficulty.setTextColor(0xFFA16207.toInt()); tvDifficulty.setBackgroundResource(R.drawable.bg_badge_medium) }
            "Hard"   -> { tvDifficulty.setTextColor(0xFFB91C1C.toInt()); tvDifficulty.setBackgroundResource(R.drawable.bg_badge_hard) }
        }

        tagsContainer.removeAllViews()
        tags.take(4).forEach { tag ->
            val tv = TextView(this).apply {
                text = tag; textSize = 12f
                setTextColor(0xFF6B7280.toInt())
                setBackgroundResource(R.drawable.bg_tag)
                setPadding(24, 8, 24, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = 8 }
            }
            tagsContainer.addView(tv)
        }

        ingredientsContainer.removeAllViews()
        if (ingredients.isEmpty()) addEmptyState(ingredientsContainer, "No ingredients listed.")
        else ingredients.forEach { ing ->
            val v = LayoutInflater.from(this).inflate(R.layout.item_ingredient, ingredientsContainer, false)
            v.findViewById<TextView>(R.id.tvIngredient).text = ing
            ingredientsContainer.addView(v)
        }

        instructionsContainer.removeAllViews()
        if (instructions.isEmpty()) addEmptyState(instructionsContainer, "No instructions available.")
        else instructions.forEachIndexed { i, step ->
            val v = LayoutInflater.from(this).inflate(R.layout.item_instruction, instructionsContainer, false)
            v.findViewById<TextView>(R.id.tvStepNumber).text = (i + 1).toString()
            v.findViewById<TextView>(R.id.tvStepText).text = step
            instructionsContainer.addView(v)
        }

        // Favorite button click
        btnFavorite.setOnClickListener { toggleFavorite() }
    }

    // ── Rating ─────────────────────────────────────────────────────────────────

    private fun updateRatingDisplay() {
        tvRating.text = String.format("%.1f", liveRating)
    }

    private fun setupStars() {
        starsContainer.removeAllViews()
        for (i in 1..5) {
            val star = ImageView(this).apply {
                setImageResource(R.drawable.ic_star)
                layoutParams = LinearLayout.LayoutParams(40, 40).also { it.marginEnd = 8 }
                setColorFilter(0xFFD1D5DB.toInt())
                setOnClickListener { if (!hasRated) onStarClicked(i) }
            }
            starsContainer.addView(star)
        }
    }

    private fun onStarClicked(rating: Int) {
        if (hasRated) return
        userRating = rating
        hasRated = true

        // Update star colors immediately
        for (i in 0 until starsContainer.childCount) {
            val star = starsContainer.getChildAt(i) as ImageView
            star.setColorFilter(if (i < rating) 0xFFF97316.toInt() else 0xFFD1D5DB.toInt())
        }

        // Send to backend
        RetrofitClient.instance.rateRecipe(recipeId, RateRequest(rating))
            .enqueue(object : Callback<DbRecipe> {
                override fun onResponse(call: Call<DbRecipe>, response: Response<DbRecipe>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            liveRating = it.rating
                            liveReviewCount = it.reviewCount
                            runOnUiThread { updateRatingDisplay() }
                        }
                    }
                    Toast.makeText(this@RecipeDetailActivity,
                        "Rated $rating star${if (rating > 1) "s" else ""}!", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<DbRecipe>, t: Throwable) {
                    Toast.makeText(this@RecipeDetailActivity, "Rating saved locally", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ── Favorites ──────────────────────────────────────────────────────────────

    private fun loadFavoriteStatus() {
        val user = getUser() ?: return
        RetrofitClient.instance.checkFavorite(user.email ?: "", recipeId)
            .enqueue(object : Callback<FavoriteCheckResponse> {
                override fun onResponse(call: Call<FavoriteCheckResponse>, response: Response<FavoriteCheckResponse>) {
                    isFavorited = response.body()?.favorited ?: false
                    runOnUiThread { updateFavoriteIcon() }
                }
                override fun onFailure(call: Call<FavoriteCheckResponse>, t: Throwable) {}
            })
    }

    private fun toggleFavorite() {
        val user = getUser() ?: return
        val email = user.email ?: return

        if (isFavorited) {
            RetrofitClient.instance.removeFavorite(email, recipeId)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        isFavorited = false
                        runOnUiThread {
                            updateFavoriteIcon()
                            Toast.makeText(this@RecipeDetailActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {}
                })
        } else {
            val title = intent.getStringExtra("recipe_title") ?: ""
            val image = intent.getStringExtra("recipe_image") ?: ""
            RetrofitClient.instance.addFavorite(FavoriteRequest(email, recipeId, title, image))
                .enqueue(object : Callback<FavoriteItem> {
                    override fun onResponse(call: Call<FavoriteItem>, response: Response<FavoriteItem>) {
                        isFavorited = true
                        runOnUiThread {
                            updateFavoriteIcon()
                            Toast.makeText(this@RecipeDetailActivity, "Added to favorites!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<FavoriteItem>, t: Throwable) {}
                })
        }
    }

    private fun updateFavoriteIcon() {
        btnFavorite.setColorFilter(
            if (isFavorited) 0xFFF97316.toInt() else 0xFF9CA3AF.toInt()
        )
    }

    // ── Comments ───────────────────────────────────────────────────────────────

    private fun loadComments() {
        RetrofitClient.instance.getComments(recipeId)
            .enqueue(object : Callback<List<CommentItem>> {
                override fun onResponse(call: Call<List<CommentItem>>, response: Response<List<CommentItem>>) {
                    val items = response.body() ?: emptyList()
                    runOnUiThread { renderComments(items) }
                }
                override fun onFailure(call: Call<List<CommentItem>>, t: Throwable) {}
            })
    }

    private fun setupComments() {
        btnPostComment.setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Please write a comment first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val user = getUser()
            val authorName = if (user != null) "${user.firstName} ${user.lastName}" else "Anonymous"
            val email = user?.email ?: ""

            btnPostComment.isEnabled = false
            RetrofitClient.instance.postComment(CommentRequest(email, authorName, recipeId, text))
                .enqueue(object : Callback<CommentItem> {
                    override fun onResponse(call: Call<CommentItem>, response: Response<CommentItem>) {
                        btnPostComment.isEnabled = true
                        if (response.isSuccessful) {
                            etComment.setText("")
                            loadComments()
                        }
                    }
                    override fun onFailure(call: Call<CommentItem>, t: Throwable) {
                        btnPostComment.isEnabled = true
                        Toast.makeText(this@RecipeDetailActivity, "Failed to post comment", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun renderComments(items: List<CommentItem>) {
        commentsContainer.removeAllViews()
        tvCommentsTitle.text = "Comments (${items.size})"
        items.forEach { c ->
            val v = LayoutInflater.from(this).inflate(R.layout.item_comment, commentsContainer, false)
            v.findViewById<TextView>(R.id.tvCommentAuthor).text = c.authorName
            v.findViewById<TextView>(R.id.tvCommentText).text = c.text
            v.findViewById<TextView>(R.id.tvCommentDate).text =
                c.createdAt?.let {
                    try { it.substring(0, 10) } catch (e: Exception) { "Just now" }
                } ?: "Just now"
            commentsContainer.addView(v)
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun getUser(): LoginResponse? {
        val data = prefs.getString("user_data", null) ?: return null
        return Gson().fromJson(data, LoginResponse::class.java)
    }

    private fun addEmptyState(container: LinearLayout, message: String) {
        container.addView(TextView(this).apply {
            text = message; textSize = 14f; setTextColor(0xFF9CA3AF.toInt())
        })
    }
}