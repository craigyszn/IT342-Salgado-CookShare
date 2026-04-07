package com.salgado.cookshare.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.salgado.cookshare.R
import com.salgado.cookshare.api.RetrofitClient
import com.salgado.cookshare.model.LoginRequest
import com.salgado.cookshare.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
        // Handle deep link if app opened via OAuth redirect
        handleOAuthDeepLink(intent)
    }

    private fun initViews() {
        etEmail         = findViewById(R.id.etEmail)
        etPassword      = findViewById(R.id.etPassword)
        btnSignIn       = findViewById(R.id.btnSignIn)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvSignUp        = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener { handleLogin() }
        btnGoogleSignIn.setOnClickListener { handleGoogleSignIn() }
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Email/Password Login ───────────────────────────────────────────────────

    private fun handleLogin() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignIn.isEnabled = false
        btnSignIn.text = "Signing in..."

        RetrofitClient.instance.login(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    btnSignIn.isEnabled = true
                    btnSignIn.text = "Sign In"
                    if (response.isSuccessful && response.body() != null) {
                        saveUserAndNavigate(response.body()!!)
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnSignIn.isEnabled = true
                    btnSignIn.text = "Sign In"
                    Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    // ── Google Login ──────────────────────────────────────────────────────────

    private fun handleGoogleSignIn() {
        val googleAuthUrl = "http://10.0.2.2:8081/oauth2/authorization/google"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(googleAuthUrl)))
    }

    // ── Save + Navigate ───────────────────────────────────────────────────────

    private fun saveUserAndNavigate(user: LoginResponse) {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        prefs.edit().putString("user_data", Gson().toJson(user)).apply()
        Toast.makeText(this, "Welcome ${user.firstName}!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }

    // ── Deep Link Handler (called after Google OAuth redirects back) ───────────

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthDeepLink(intent)
    }

    private fun handleOAuthDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "cookshare" && data.host == "oauth-success") {
            val fakeResponse = LoginResponse(
                message   = "Login successful",
                firstName = data.getQueryParameter("firstName") ?: "",
                lastName  = data.getQueryParameter("lastName") ?: "",
                email     = data.getQueryParameter("email") ?: "",
                role      = data.getQueryParameter("role") ?: "USER"
            )
            saveUserAndNavigate(fakeResponse)
        }
    }
}