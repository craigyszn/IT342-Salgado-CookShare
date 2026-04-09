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
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnUploadImage: Button
    private lateinit var ivImagePreview: ImageView
    private lateinit var tvImageStatus: TextView

    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var instructionsContainer: LinearLayout
    private lateinit var btnAddIngredient: Button
    private lateinit var btnAddInstruction: Button

    private var uploadedImageUrl: String = ""

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
        addIngredientRow()
        addInstructionRow()
    }

    private fun initViews() {
        btnBack               = findViewById(R.id.btnBack)
        etTitle               = findViewById(R.id.etTitle)
        etDescription         = findViewById(R.id.etDescription)
        etPrepTime            = findViewById(R.id.etPrepTime)
        etCookTime            = findViewById(R.id.etCookTime)
        etServings            = findViewById(R.id.etServings)
        spinnerDifficulty     = findViewById(R.id.spinnerDifficulty)
        spinnerCategory       = findViewById(R.id.spinnerCategory)
        etTags                = findViewById(R.id.etTags)
        btnPublish            = findViewById(R.id.btnPublish)
        progressBar           = findViewById(R.id.progressBar)
        btnUploadImage        = findViewById(R.id.btnUploadImage)
        ivImagePreview        = findViewById(R.id.ivImagePreview)
        tvImageStatus         = findViewById(R.id.tvImageStatus)
        ingredientsContainer  = findViewById(R.id.ingredientsContainer)
        instructionsContainer = findViewById(R.id.instructionsContainer)
        btnAddIngredient      = findViewById(R.id.btnAddIngredient)
        btnAddInstruction     = findViewById(R.id.btnAddInstruction)
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
        btnAddIngredient.setOnClickListener { addIngredientRow() }
        btnAddInstruction.setOnClickListener { addInstructionRow() }
    }

    // ── Add ingredient row ────────────────────────────────────────────────────
    private fun addIngredientRow(text: String = "") {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 8 }
        }

        val et = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 120, 1f)
            background = getDrawable(R.drawable.bg_input)
            hint = "e.g. 2 cups flour"
            setText(text)
            textSize = 14f
            setTextColor(0xFF111827.toInt())
            setHintTextColor(0xFF9CA3AF.toInt())
            setPadding(28, 0, 28, 0)
        }

        val btnRemove = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 120).also { it.marginStart = 8 }
            setText("X")
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            background = getDrawable(R.drawable.bg_badge_hard)
            stateListAnimator = null
            setOnClickListener {
                if (ingredientsContainer.childCount > 1) {
                    ingredientsContainer.removeView(row)
                }
            }
        }

        row.addView(et)
        row.addView(btnRemove)
        ingredientsContainer.addView(row)
    }

    // ── Add instruction row ───────────────────────────────────────────────────
    private fun addInstructionRow(text: String = "") {
        val stepNumber = instructionsContainer.childCount + 1
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 8 }
            gravity = android.view.Gravity.TOP
        }

        val tvStep = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72).also { it.marginEnd = 8; it.topMargin = 8 }
            setText(stepNumber.toString())
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            background = getDrawable(R.drawable.bg_logo_circle)
            gravity = android.view.Gravity.CENTER
        }

        val et = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also {
                it.bottomMargin = 4
            }
            background = getDrawable(R.drawable.bg_input)
            hint = "Step $stepNumber"
            setText(text)
            textSize = 14f
            minLines = 2
            setTextColor(0xFF111827.toInt())
            setHintTextColor(0xFF9CA3AF.toInt())
            setPadding(28, 20, 28, 20)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        val btnRemove = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(80, 80).also { it.marginStart = 8; it.topMargin = 8 }
            setText("X")
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            background = getDrawable(R.drawable.bg_badge_hard)
            stateListAnimator = null
            setOnClickListener {
                if (instructionsContainer.childCount > 1) {
                    instructionsContainer.removeView(row)
                    updateStepNumbers()
                }
            }
        }

        row.addView(tvStep)
        row.addView(et)
        row.addView(btnRemove)
        instructionsContainer.addView(row)
    }

    // ── Update step numbers after removal ─────────────────────────────────────
    private fun updateStepNumbers() {
        for (i in 0 until instructionsContainer.childCount) {
            val row = instructionsContainer.getChildAt(i) as LinearLayout
            val tvStep = row.getChildAt(0) as TextView
            tvStep.text = (i + 1).toString()
        }
    }

    // ── Get all ingredients from dynamic rows ─────────────────────────────────
    private fun getIngredients(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until ingredientsContainer.childCount) {
            val row = ingredientsContainer.getChildAt(i) as LinearLayout
            val et = row.getChildAt(0) as EditText
            val text = et.text.toString().trim()
            if (text.isNotEmpty()) list.add(text)
        }
        return list
    }

    // ── Get all instructions from dynamic rows ────────────────────────────────
    private fun getInstructions(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until instructionsContainer.childCount) {
            val row = instructionsContainer.getChildAt(i) as LinearLayout
            val et = row.getChildAt(1) as EditText
            val text = et.text.toString().trim()
            if (text.isNotEmpty()) list.add(text)
        }
        return list
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
                        tvImageStatus.text = "Image uploaded!"
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
        val title       = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val prepTime    = etPrepTime.text.toString().trim()
        val cookTime    = etCookTime.text.toString().trim()
        val servings    = etServings.text.toString().trim()
        val tags        = etTags.text.toString().trim()
        val difficulty  = spinnerDifficulty.selectedItem.toString()
        val category    = spinnerCategory.selectedItem.toString()

        val ingredientList  = getIngredients()
        val instructionList = getInstructions()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in title and description", Toast.LENGTH_SHORT).show()
            return
        }
        if (ingredientList.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
            return
        }
        if (instructionList.isEmpty()) {
            Toast.makeText(this, "Please add at least one instruction", Toast.LENGTH_SHORT).show()
            return
        }

        val user       = getUser()
        val authorName = if (user != null) "${user.firstName} ${user.lastName}" else "Unknown"
        val email      = user?.email ?: ""
        val date       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tagList    = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

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

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }
        return tempFile
    }

    private fun getUser(): LoginResponse? {
        val data = prefs.getString("user_data", null) ?: return null
        return Gson().fromJson(data, LoginResponse::class.java)
    }
}