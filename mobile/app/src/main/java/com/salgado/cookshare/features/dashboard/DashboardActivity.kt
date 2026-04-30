package com.salgado.cookshare.features.dashboard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.salgado.cookshare.R
import com.salgado.cookshare.features.shared.RetrofitClient
import com.salgado.cookshare.features.auth.LoginActivity

import com.salgado.cookshare.features.recipe.CreateRecipeActivity
import com.salgado.cookshare.features.recipe.Recipe
import com.salgado.cookshare.features.profile.ProfileActivity
import com.salgado.cookshare.features.recipe.RecipeAdapter
import com.salgado.cookshare.features.recipe.RecipeDetailActivity
import com.salgado.cookshare.features.recipe.SpoonacularRandomResponse
import com.salgado.cookshare.features.recipe.SpoonacularRecipe
import com.salgado.cookshare.features.recipe.SpoonacularResponse
import com.salgado.cookshare.features.shared.DbRecipe
import com.salgado.cookshare.features.shared.LoginResponse
import com.salgado.cookshare.features.shared.ProfilePhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvInitials: TextView
    private lateinit var ivDashboardAvatar: ImageView  // ← NEW
    private lateinit var btnAvatar: FrameLayout
    private lateinit var btnCreateRecipe: Button
    private lateinit var etSearch: EditText
    private lateinit var categoryContainer: LinearLayout
    private lateinit var tvTotalRecipes: TextView
    private lateinit var tvAvgRating: TextView
    private lateinit var rvRecipes: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutError: LinearLayout
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var adapter: RecipeAdapter
    private var activeCategory = "All"
    private var searchQuery = ""
    private var searchRunnable: Runnable? = null
    private val searchHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var dbRecipes: List<Recipe> = emptyList()

    private val categories = listOf("All", "Pasta", "Dessert", "Salad", "Main Course", "Asian", "Pizza")
    private val categoryMap = mapOf(
        "Pasta" to "pasta",
        "Dessert" to "dessert cake cookies",
        "Salad" to "salad",
        "Main Course" to "chicken beef dinner",
        "Asian" to "asian stir fry noodles",
        "Pizza" to "pizza"
    )

    private val apiKey = "f25cb7b672dd40e9b4ebdd66b0cf3988"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        initViews()
        setupUser()
        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        setupBottomNav()
        setupListeners()
        fetchDbRecipes()
    }

    override fun onResume() {
        super.onResume()
        setupUser()  // ← reload avatar when coming back from profile
        fetchDbRecipes()
    }

    private fun initViews() {
        tvInitials        = findViewById(R.id.tvInitials)
        ivDashboardAvatar = findViewById(R.id.ivDashboardAvatar)  // ← NEW
        btnAvatar         = findViewById(R.id.btnAvatar)
        btnCreateRecipe   = findViewById(R.id.btnCreateRecipe)
        etSearch          = findViewById(R.id.etSearch)
        categoryContainer = findViewById(R.id.categoryContainer)
        tvTotalRecipes    = findViewById(R.id.tvTotalRecipes)
        tvAvgRating       = findViewById(R.id.tvAvgRating)
        rvRecipes         = findViewById(R.id.rvRecipes)
        progressBar       = findViewById(R.id.progressBar)
        layoutError       = findViewById(R.id.layoutError)
        layoutEmpty       = findViewById(R.id.layoutEmpty)
        tvErrorMessage    = findViewById(R.id.tvErrorMessage)
        btnRetry          = findViewById(R.id.btnRetry)
        bottomNav         = findViewById(R.id.bottomNav)
    }

    private fun setupUser() {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        val userData = prefs.getString("user_data", null)
        if (userData != null) {
            val user = Gson().fromJson(userData, LoginResponse::class.java)
            tvInitials.text = user.firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

            // ── Load profile photo from backend ───────────────────────────────
            val email = user.email ?: return
            RetrofitClient.instance.getProfilePhoto(email)
                .enqueue(object : Callback<ProfilePhotoResponse> {
                    override fun onResponse(
                        call: Call<ProfilePhotoResponse>,
                        response: Response<ProfilePhotoResponse>
                    ) {
                        val photoUrl = response.body()?.profilePhotoUrl
                        if (!photoUrl.isNullOrEmpty()) {
                            runOnUiThread {
                                ivDashboardAvatar.visibility = View.VISIBLE
                                tvInitials.visibility = View.GONE
                                Glide.with(this@DashboardActivity)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(ivDashboardAvatar)
                            }
                        } else {
                            runOnUiThread {
                                ivDashboardAvatar.visibility = View.GONE
                                tvInitials.visibility = View.VISIBLE
                            }
                        }
                    }
                    override fun onFailure(call: Call<ProfilePhotoResponse>, t: Throwable) {
                        runOnUiThread {
                            ivDashboardAvatar.visibility = View.GONE
                            tvInitials.visibility = View.VISIBLE
                        }
                    }
                })
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra("recipe_id", recipe.id)
                putExtra("recipe_title", recipe.title)
                putExtra("recipe_image", recipe.imageUrl)
                putExtra("recipe_description", recipe.description)
                putExtra("recipe_difficulty", recipe.difficulty)
                putExtra("recipe_prep_time", recipe.prepTime)
                putExtra("recipe_cook_time", recipe.cookTime)
                putExtra("recipe_servings", recipe.servings)
                putExtra("recipe_rating", recipe.rating)
                putExtra("recipe_review_count", recipe.reviewCount)
                putExtra("recipe_author", recipe.author)
                putStringArrayListExtra("recipe_tags", ArrayList(recipe.tags))
                putStringArrayListExtra("recipe_ingredients", ArrayList(recipe.ingredients))
                putStringArrayListExtra("recipe_instructions", ArrayList(recipe.instructions))
            }
            startActivity(intent)
        }
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter
        rvRecipes.isNestedScrollingEnabled = false
    }

    private fun setupCategoryChips() {
        categories.forEach { category ->
            val chip = TextView(this).apply {
                text = category
                textSize = 13f
                setPadding(32, 16, 32, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = 8 }
                isClickable = true; isFocusable = true
                gravity = Gravity.CENTER
                setBackgroundResource(if (category == activeCategory) R.drawable.bg_chip_active else R.drawable.bg_chip_inactive)
                setTextColor(if (category == activeCategory) 0xFFFFFFFF.toInt() else 0xFF4B5563.toInt())
                setOnClickListener { onCategorySelected(category) }
            }
            categoryContainer.addView(chip)
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                searchRunnable = Runnable {
                    searchQuery = s?.toString()?.trim() ?: ""
                    fetchRecipes()
                }
                searchHandler.postDelayed(searchRunnable!!, 600)
            }
        })
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> { etSearch.requestFocus(); true }
                R.id.nav_create -> {
                    startActivity(Intent(this, CreateRecipeActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        btnAvatar.setOnClickListener { showUserMenu() }
        btnCreateRecipe.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
        }
        btnRetry.setOnClickListener { fetchDbRecipes() }
    }

    private fun fetchDbRecipes() {
        showLoading()
        RetrofitClient.instance.getAllRecipes().enqueue(object : Callback<List<DbRecipe>> {
            override fun onResponse(call: Call<List<DbRecipe>>, response: Response<List<DbRecipe>>) {
                dbRecipes = response.body()?.map { it.toRecipe() } ?: emptyList()
                fetchRecipes()
            }
            override fun onFailure(call: Call<List<DbRecipe>>, t: Throwable) {
                dbRecipes = emptyList()
                fetchRecipes()
            }
        })
    }

    private fun onCategorySelected(category: String) {
        activeCategory = category
        searchQuery = ""
        etSearch.setText("")
        for (i in 0 until categoryContainer.childCount) {
            val chip = categoryContainer.getChildAt(i) as TextView
            val isActive = chip.text == category
            chip.setBackgroundResource(if (isActive) R.drawable.bg_chip_active else R.drawable.bg_chip_inactive)
            chip.setTextColor(if (isActive) 0xFFFFFFFF.toInt() else 0xFF4B5563.toInt())
        }
        fetchRecipes()
    }

    private fun fetchRecipes() {
        showLoading()

        when {
            searchQuery.isNotEmpty() -> {
                val dbMatches = dbRecipes.filter { r ->
                    r.title.contains(searchQuery, ignoreCase = true) ||
                            r.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                            r.author.contains(searchQuery, ignoreCase = true)
                }
                RetrofitClient.spoonacular.searchRecipes(query = searchQuery, apiKey = apiKey)
                    .enqueue(object : Callback<SpoonacularResponse> {
                        override fun onResponse(c: Call<SpoonacularResponse>, r: Response<SpoonacularResponse>) {
                            val spoonResults = r.body()?.results?.map { it.toRecipe() } ?: emptyList()
                            val merged = (dbMatches + spoonResults).distinctBy { it.id }
                            if (merged.isEmpty()) fetchRandomRecipes()
                            else displayRecipes(merged)
                        }
                        override fun onFailure(c: Call<SpoonacularResponse>, t: Throwable) {
                            displayRecipes(dbMatches.ifEmpty { dbRecipes })
                        }
                    })
            }
            activeCategory != "All" -> {
                val dbCat = dbRecipes.filter { r ->
                    r.tags.any { it.equals(activeCategory, ignoreCase = true) } ||
                            r.category.equals(activeCategory, ignoreCase = true)
                }
                RetrofitClient.spoonacular.searchRecipes(
                    query = categoryMap[activeCategory] ?: activeCategory, apiKey = apiKey
                ).enqueue(object : Callback<SpoonacularResponse> {
                    override fun onResponse(c: Call<SpoonacularResponse>, r: Response<SpoonacularResponse>) {
                        val spoonResults = r.body()?.results?.map { it.toRecipe() } ?: emptyList()
                        val merged = (dbCat + spoonResults).distinctBy { it.id }
                        if (merged.isEmpty()) displayRecipes(dbRecipes)
                        else displayRecipes(merged)
                    }
                    override fun onFailure(c: Call<SpoonacularResponse>, t: Throwable) {
                        displayRecipes(dbCat.ifEmpty { dbRecipes })
                    }
                })
            }
            else -> fetchRandomRecipes()
        }
    }

    private fun fetchRandomRecipes() {
        RetrofitClient.spoonacular.getRandomRecipes(apiKey = apiKey)
            .enqueue(object : Callback<SpoonacularRandomResponse> {
                override fun onResponse(call: Call<SpoonacularRandomResponse>, response: Response<SpoonacularRandomResponse>) {
                    val spoon = response.body()?.recipes?.map { it.toRecipe() } ?: emptyList()
                    displayRecipes((dbRecipes + spoon).distinctBy { it.id })
                }
                override fun onFailure(call: Call<SpoonacularRandomResponse>, t: Throwable) {
                    if (dbRecipes.isNotEmpty()) displayRecipes(dbRecipes)
                    else showError("Network error: ${t.message}")
                }
            })
    }

    private fun displayRecipes(recipes: List<Recipe>) {
        hideLoading()
        if (recipes.isEmpty()) { showEmpty(); return }
        layoutError.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        rvRecipes.visibility = View.VISIBLE
        adapter.updateRecipes(recipes)
        tvTotalRecipes.text = recipes.size.toString()
        val avg = recipes.map { it.rating }.average()
        tvAvgRating.text = if (avg.isNaN()) "—" else String.format("%.1f", avg)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        rvRecipes.visibility = View.GONE
        layoutError.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
    }

    private fun hideLoading() { progressBar.visibility = View.GONE }

    private fun showError(message: String) {
        hideLoading()
        rvRecipes.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        layoutError.visibility = View.VISIBLE
        tvErrorMessage.text = message
    }

    private fun showEmpty() {
        rvRecipes.visibility = View.GONE
        layoutError.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
    }

    private fun showUserMenu() {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        val user = prefs.getString("user_data", null)
            ?.let { Gson().fromJson(it, LoginResponse::class.java) }
        val popup = PopupMenu(this, btnAvatar)
        popup.menu.add("${user?.firstName ?: ""} ${user?.lastName ?: ""}").isEnabled = false
        popup.menu.add(user?.email ?: "").isEnabled = false
        popup.menu.add("─────────────").isEnabled = false
        popup.menu.add("Profile")
        popup.menu.add("Logout")
        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Profile" -> startActivity(Intent(this, ProfileActivity::class.java))
                "Logout" -> {
                    prefs.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            }
            true
        }
        popup.show()
    }
}

// ── DB Recipe mapper ──────────────────────────────────────────────────────────
fun DbRecipe.toRecipe() = Recipe(
    id           = id,
    title        = title,
    description  = description ?: "",
    tags         = tags ?: emptyList(),
    rating       = rating,
    reviewCount  = reviewCount,
    prepTime     = prepTime ?: "N/A",
    cookTime     = cookTime ?: "N/A",
    difficulty   = difficulty ?: "Easy",
    author       = author ?: "CookShare User",
    servings     = servings,
    imageUrl     = image ?: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400",
    ingredients  = ingredients ?: emptyList(),
    instructions = instructions ?: emptyList(),
    category     = category ?: ""
)

// ── Spoonacular mapper ────────────────────────────────────────────────────────
fun SpoonacularRecipe.toRecipe(): Recipe {
    val difficulty = when {
        readyInMinutes <= 20 -> "Easy"
        readyInMinutes <= 45 -> "Medium"
        else -> "Hard"
    }
    val cleanSummary = summary
        ?.replace(Regex("<[^>]*>"), "")
        ?.replace("&amp;", "&")?.replace("&lt;", "<")
        ?.replace("&gt;", ">")?.replace("&nbsp;", " ")
        ?.trim()?.take(180) ?: ""
    return Recipe(
        id           = id.toString(),
        title        = title,
        description  = if (cleanSummary.length == 180) "$cleanSummary..." else cleanSummary,
        tags         = buildList {
            dishTypes?.take(2)?.forEach { add(it.replaceFirstChar { c -> c.uppercase() }) }
            diets?.take(1)?.forEach { add(it.replaceFirstChar { c -> c.uppercase() }) }
        },
        rating       = Math.round((spoonacularScore / 20.0) * 10.0) / 10.0,
        reviewCount  = aggregateLikes,
        prepTime     = if (preparationMinutes > 0) "$preparationMinutes mins" else "N/A",
        cookTime     = if (cookingMinutes > 0) "$cookingMinutes mins" else "$readyInMinutes mins",
        difficulty   = difficulty,
        author       = creditsText ?: "Spoonacular",
        servings     = servings,
        imageUrl     = image ?: "",
        ingredients  = extendedIngredients?.map { it.original } ?: emptyList(),
        instructions = analyzedInstructions?.firstOrNull()?.steps?.map { it.step }
            ?: listOf("See full recipe for instructions."),
        category     = dishTypes?.firstOrNull() ?: ""
    )
}
