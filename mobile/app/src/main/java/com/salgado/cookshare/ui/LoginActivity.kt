package com.salgado.cookshare.ui

import android.content.Intent
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
    private lateinit var tvSignUp: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }

    private fun setupListeners() {

        btnSignIn.setOnClickListener {
            handleLogin()
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleLogin() {

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        btnSignIn.isEnabled = false
        btnSignIn.text = "Signing in..."

        val request = LoginRequest(
            email = email,
            password = password
        )

        RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                btnSignIn.isEnabled = true
                btnSignIn.text = "Sign In"

                if (response.isSuccessful && response.body() != null) {

                    val loginResponse = response.body()!!

                    val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
                    val gson = Gson()

                    prefs.edit().apply {
                        putString("user_data", gson.toJson(loginResponse))
                        apply()
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome ${loginResponse.firstName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()

                } else {

                    Toast.makeText(
                        this@LoginActivity,
                        "Invalid email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

                btnSignIn.isEnabled = true
                btnSignIn.text = "Sign In"

                Toast.makeText(
                    this@LoginActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


}
