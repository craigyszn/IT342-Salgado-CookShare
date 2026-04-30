package com.salgado.cookshare.features.profile

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.salgado.cookshare.R
import com.salgado.cookshare.features.shared.RetrofitClient
import com.salgado.cookshare.features.recipe.CreateRecipeActivity
import com.salgado.cookshare.features.shared.DbRecipe
import com.salgado.cookshare.features.shared.LoginResponse
import com.salgado.cookshare.features.shared.ProfilePhotoResponse
import com.salgado.cookshare.features.shared.UserStats

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    private lateinit var btnBack: Button
    private lateinit var tvAvatarInitials: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var btnUploadPhoto: Button
    private lateinit var tvRecipesShared: TextView
    private lateinit var tvFavorites: TextView
    private lateinit var tvComments: TextView
    private lateinit var tabProfile: TextView
    private lateinit var tabRecipes: TextView
    private lateinit var profileContent: LinearLayout
    private lateinit var recipesContent: LinearLayout

    private lateinit var tvFirstName: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvProfileFirstName: TextView
    private lateinit var tvProfileLastName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvMemberSince: TextView

    private lateinit var recipesContainer: LinearLayout
    private lateinit var tvRecipesEmpty: TextView
    private lateinit var progressRecipes: ProgressBar
    private lateinit var btnCreateNew: Button

    // ── Photo picker launcher ─────────────────────────────────────────────────
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            // Show preview immediately
            ivAvatar.setImageURI(uri)
            ivAvatar.visibility = View.VISIBLE
            tvAvatarInitials.visibility = View.GONE
            uploadProfilePhoto(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        initViews()
        loadUserInfo()
        loadStats()
        loadProfilePhoto()
        setupTabs()
    }

    private fun initViews() {
        btnBack             = findViewById(R.id.btnBack)
        tvAvatarInitials    = findViewById(R.id.tvAvatarInitials)
        ivAvatar            = findViewById(R.id.ivAvatar)
        btnUploadPhoto      = findViewById(R.id.btnUploadPhoto)
        tvRecipesShared     = findViewById(R.id.tvRecipesShared)
        tvFavorites         = findViewById(R.id.tvFavorites)
        tvComments          = findViewById(R.id.tvComments)
        tabProfile          = findViewById(R.id.tabProfile)
        tabRecipes          = findViewById(R.id.tabRecipes)
        profileContent      = findViewById(R.id.profileContent)
        recipesContent      = findViewById(R.id.recipesContent)
        tvFirstName         = findViewById(R.id.tvFirstName)
        tvLastName          = findViewById(R.id.tvLastName)
        tvEmail             = findViewById(R.id.tvEmail)
        tvProfileFirstName  = findViewById(R.id.tvProfileFirstName)
        tvProfileLastName   = findViewById(R.id.tvProfileLastName)
        tvProfileEmail      = findViewById(R.id.tvProfileEmail)
        tvMemberSince       = findViewById(R.id.tvMemberSince)
        recipesContainer    = findViewById(R.id.recipesContainer)
        tvRecipesEmpty      = findViewById(R.id.tvRecipesEmpty)
        progressRecipes     = findViewById(R.id.progressRecipes)
        btnCreateNew        = findViewById(R.id.btnCreateNew)

        btnBack.setOnClickListener { finish() }
        btnCreateNew.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
        }
        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoPickerLauncher.launch(intent)
        }
    }

    private fun loadUserInfo() {
        val user = getUser() ?: return
        tvFirstName.text = user.firstName ?: ""
        tvLastName.text  = user.lastName ?: ""
        tvEmail.text     = user.email ?: ""

        val initials = "${user.firstName?.firstOrNull() ?: ""}${user.lastName?.firstOrNull() ?: ""}".uppercase()
        tvAvatarInitials.text = initials.ifEmpty { "U" }

        tvProfileFirstName.text = user.firstName ?: ""
        tvProfileLastName.text  = user.lastName ?: ""
        tvProfileEmail.text     = user.email ?: ""
        tvMemberSince.text      = "CookShare Member"
    }

    // ── Load profile photo from backend ───────────────────────────────────────
    private fun loadProfilePhoto() {
        val user = getUser() ?: return
        RetrofitClient.instance.getProfilePhoto(user.email ?: "")
            .enqueue(object : Callback<ProfilePhotoResponse> {
                override fun onResponse(
                    call: Call<ProfilePhotoResponse>,
                    response: Response<ProfilePhotoResponse>
                ) {
                    val photoUrl = response.body()?.profilePhotoUrl
                    if (!photoUrl.isNullOrEmpty()) {
                        ivAvatar.visibility = View.VISIBLE
                        tvAvatarInitials.visibility = View.GONE
                        Glide.with(this@ProfileActivity)
                            .load(photoUrl)
                            .circleCrop()
                            .into(ivAvatar)
                    }
                }
                override fun onFailure(call: Call<ProfilePhotoResponse>, t: Throwable) {}
            })
    }

    // ── Upload profile photo to Supabase via Spring Boot ──────────────────────
    private fun uploadProfilePhoto(uri: Uri) {
        val user = getUser() ?: return
        btnUploadPhoto.isEnabled = false
        btnUploadPhoto.text = "Uploading..."

        val file = uriToFile(uri)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val emailBody = (user.email ?: "").toRequestBody("text/plain".toMediaTypeOrNull())

        RetrofitClient.instance.uploadProfilePhoto(part, emailBody)
            .enqueue(object : Callback<ProfilePhotoResponse> {
                override fun onResponse(
                    call: Call<ProfilePhotoResponse>,
                    response: Response<ProfilePhotoResponse>
                ) {
                    btnUploadPhoto.isEnabled = true
                    btnUploadPhoto.text = "Change Photo"
                    if (response.isSuccessful) {
                        val photoUrl = response.body()?.profilePhotoUrl
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(photoUrl)
                                .circleCrop()
                                .into(ivAvatar)
                        }
                        Toast.makeText(this@ProfileActivity,
                            "Profile photo updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity,
                            "Upload failed (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ProfilePhotoResponse>, t: Throwable) {
                    btnUploadPhoto.isEnabled = true
                    btnUploadPhoto.text = "Upload Photo"
                    Toast.makeText(this@ProfileActivity,
                        "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadStats() {
        val user = getUser() ?: return
        RetrofitClient.instance.getUserStats(user.email ?: "")
            .enqueue(object : Callback<UserStats> {
                override fun onResponse(call: Call<UserStats>, response: Response<UserStats>) {
                    response.body()?.let {
                        tvRecipesShared.text = it.recipesShared.toString()
                        tvFavorites.text     = it.favorites.toString()
                        tvComments.text      = it.comments.toString()
                    }
                }
                override fun onFailure(call: Call<UserStats>, t: Throwable) {}
            })
    }

    private fun setupTabs() {
        setTabActive(tabProfile)
        setTabInactive(tabRecipes)
        profileContent.visibility = View.VISIBLE
        recipesContent.visibility = View.GONE

        tabProfile.setOnClickListener {
            setTabActive(tabProfile)
            setTabInactive(tabRecipes)
            profileContent.visibility = View.VISIBLE
            recipesContent.visibility = View.GONE
        }

        tabRecipes.setOnClickListener {
            setTabInactive(tabProfile)
            setTabActive(tabRecipes)
            profileContent.visibility = View.GONE
            recipesContent.visibility = View.VISIBLE
            loadMyRecipes()
        }
    }

    private fun setTabActive(tab: TextView) {
        tab.setTextColor(0xFFF97316.toInt())
        tab.setBackgroundResource(R.drawable.bg_tab_active)
    }

    private fun setTabInactive(tab: TextView) {
        tab.setTextColor(0xFF6B7280.toInt())
        tab.setBackgroundResource(R.drawable.bg_tab_inactive)
    }

    private fun loadMyRecipes() {
        val user = getUser() ?: return
        progressRecipes.visibility = View.VISIBLE
        tvRecipesEmpty.visibility  = View.GONE
        recipesContainer.removeAllViews()

        RetrofitClient.instance.getRecipesByUser(user.email ?: "")
            .enqueue(object : Callback<List<DbRecipe>> {
                override fun onResponse(
                    call: Call<List<DbRecipe>>,
                    response: Response<List<DbRecipe>>
                ) {
                    progressRecipes.visibility = View.GONE
                    val recipes = response.body() ?: emptyList()
                    if (recipes.isEmpty()) {
                        tvRecipesEmpty.visibility = View.VISIBLE
                    } else {
                        recipes.forEach { recipe -> addRecipeCard(recipe) }
                    }
                }
                override fun onFailure(call: Call<List<DbRecipe>>, t: Throwable) {
                    progressRecipes.visibility = View.GONE
                    tvRecipesEmpty.visibility = View.VISIBLE
                }
            })
    }

    private fun addRecipeCard(recipe: DbRecipe) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_profile_recipe, recipesContainer, false)

        view.findViewById<TextView>(R.id.tvRecipeTitle).text = recipe.title
        view.findViewById<TextView>(R.id.tvRecipeCategory).text = recipe.category ?: ""
        view.findViewById<TextView>(R.id.tvRecipeCookTime).text = recipe.cookTime ?: "N/A"
        view.findViewById<TextView>(R.id.tvRecipeDate).text = "Posted ${recipe.createdAt ?: "recently"}"

        val tvDiff = view.findViewById<TextView>(R.id.tvRecipeDifficulty)
        tvDiff.text = recipe.difficulty ?: "Easy"
        when (recipe.difficulty) {
            "Easy"   -> { tvDiff.setTextColor(0xFF15803D.toInt()); tvDiff.setBackgroundResource(R.drawable.bg_badge_easy) }
            "Medium" -> { tvDiff.setTextColor(0xFFA16207.toInt()); tvDiff.setBackgroundResource(R.drawable.bg_badge_medium) }
            "Hard"   -> { tvDiff.setTextColor(0xFFB91C1C.toInt()); tvDiff.setBackgroundResource(R.drawable.bg_badge_hard) }
        }

        val ivImage = view.findViewById<ImageView>(R.id.ivRecipeThumb)
        Glide.with(this).load(recipe.image)
            .placeholder(R.drawable.ic_chef_hat).centerCrop().into(ivImage)

        view.findViewById<Button>(R.id.btnDeleteRecipe).setOnClickListener {
            deleteRecipe(recipe.id, view)
        }

        recipesContainer.addView(view)
    }

    private fun deleteRecipe(id: String, view: View) {
        val token = "Bearer ${prefs.getString("access_token", "")}"
        RetrofitClient.instance.deleteRecipe(token, id)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        recipesContainer.removeView(view)
                        loadStats()
                        Toast.makeText(this@ProfileActivity, "Recipe deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ── Convert Uri to File ───────────────────────────────────────────────────
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    private fun getUser(): LoginResponse? {
        val data = prefs.getString("user_data", null) ?: return null
        return Gson().fromJson(data, LoginResponse::class.java)
    }
}
