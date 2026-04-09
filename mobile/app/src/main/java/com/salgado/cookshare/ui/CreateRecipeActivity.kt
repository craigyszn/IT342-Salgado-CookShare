package com.salgado.cookshare.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.salgado.cookshare.R
import com.salgado.cookshare.api.RetrofitClient
import com.salgado.cookshare.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var btnBack: Button
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrepTime: EditText
    private lateinit var etCookTime: EditText
    private lateinit var etServings: EditText
    private lateinit var spinnerDifficulty: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var etTags: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etInstructions: EditText
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnUploadImage: Button
    private lateinit var ivImagePreview: ImageView
    private lateinit var tvImageStatus: TextView

    private var uploadedImageUrl: String = ""

    // ── Image picker launcher ─────────────────────────────────────────────────
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            ivImagePreview.setImageURI(uri)
            ivImagePreview.visibility = View.VISIBLE
            uploadImageToSupabase(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)
        prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        initViews()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        btnBack           = findViewById(R.id.btnBack)
        etTitle           = findViewById(R.id.etTitle)
        etDescription     = findViewById(R.id.etDescription)
        etPrepTime        = findViewById(R.id.etPrepTime)
        etCookTime        = findViewById(R.id.etCookTime)
        etServings        = findViewById(R.id.etServings)
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty)
        spinnerCategory   = findViewById(R.id.spinnerCategory)
        etTags            = findViewById(R.id.etTags)
        etIngredients     = findViewById(R.id.etIngredients)
        etInstructions    = findViewById(R.id.etInstructions)
        btnPublish        = findViewById(R.id.btnPublish)
        progressBar       = findViewById(R.id.progressBar)
        btnUploadImage    = findViewById(R.id.btnUploadImage)
        ivImagePreview    = findViewById(R.id.ivImagePreview)
        tvImageStatus     = findViewById(R.id.tvImageStatus)
    }

    private fun setupSpinners() {
        val difficulties = listOf("Easy", "Medium", "Hard")
        spinnerDifficulty.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, difficulties)

        val categories = listOf("Pasta", "Dessert", "Salad", "Main Course",
            "Asian", "Pizza", "Breakfast", "Appetizer", "Soup", "Other")
        spinnerCategory.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, categories)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnPublish.setOnClickListener { handlePublish() }
        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }
    }

    // ── Upload image to Supabase via Spring Boot ──────────────────────────────
    private fun uploadImageToSupabase(uri: Uri) {
        tvImageStatus.text = "Uploading..."
        tvImageStatus.visibility = View.VISIBLE
        btnPublish.isEnabled = false

        val file = uriToFile(uri)
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        RetrofitClient.instance.uploadRecipeImage(part)
            .enqueue(object : Callback<ImageUploadResponse> {
                override fun onResponse(
                    call: Call<ImageUploadResponse>,
                    response: Response<ImageUploadResponse>
                ) {
                    if (response.isSuccessful) {
                        uploadedImageUrl = response.body()?.imageUrl ?: ""
                        tvImageStatus.text = "✓ Image uploaded!"
                        btnPublish.isEnabled = true
                    } else {
                        tvImageStatus.text = "Upload failed. Try again."
                        btnPublish.isEnabled = true
                    }
                }
                override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                    tvImageStatus.text = "Upload failed: ${t.message}"
                    btnPublish.isEnabled = true
                }
            })
    }

    private fun handlePublish() {
        val title        = etTitle.text.toString().trim()
        val description  = etDescription.text.toString().trim()
        val prepTime     = etPrepTime.text.toString().trim()
        val cookTime     = etCookTime.text.toString().trim()
        val servings     = etServings.text.toString().trim()
        val tags         = etTags.text.toString().trim()
        val ingredients  = etIngredients.text.toString().trim()
        val instructions = etInstructions.text.toString().trim()
        val difficulty   = spinnerDifficulty.selectedItem.toString()
        val category     = spinnerCategory.selectedItem.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in title and description", Toast.LENGTH_SHORT).show()
            return
        }
        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
            return
        }
        if (instructions.isEmpty()) {
            Toast.makeText(this, "Please add at least one instruction", Toast.LENGTH_SHORT).show()
            return
        }

        val user       = getUser()
        val authorName = if (user != null) "${user.firstName} ${user.lastName}" else "Unknown"
        val email      = user?.email ?: ""
        val date       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val tagList         = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val ingredientList  = ingredients.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val instructionList = instructions.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val request = CreateRecipeRequest(
            id           = System.currentTimeMillis().toString(),
            title        = title,
            description  = description,
            author       = authorName,
            userEmail    = email,
            image        = uploadedImageUrl.ifEmpty {
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400"
            },
            prepTime     = prepTime.ifEmpty { "N/A" },
            cookTime     = cookTime.ifEmpty { "N/A" },
            servings     = servings.toIntOrNull() ?: 4,
            difficulty   = difficulty,
            category     = category,
            tags         = tagList,
            ingredients  = ingredientList,
            instructions = instructionList,
            createdAt    = date
        )

        btnPublish.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val token = "Bearer ${prefs.getString("access_token", "")}"

        RetrofitClient.instance.createRecipe(token, request).enqueue(object : Callback<DbRecipe> {
            override fun onResponse(call: Call<DbRecipe>, response: Response<DbRecipe>) {
                progressBar.visibility = View.GONE
                btnPublish.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateRecipeActivity,
                        "Recipe published successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateRecipeActivity,
                        "Failed to publish recipe (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DbRecipe>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnPublish.isEnabled = true
                Toast.makeText(this@CreateRecipeActivity,
                    "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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