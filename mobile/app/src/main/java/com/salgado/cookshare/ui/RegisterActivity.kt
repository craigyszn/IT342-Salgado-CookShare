package com.salgado.cookshare.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salgado.cookshare.R
import com.salgado.cookshare.api.RetrofitClient
import com.salgado.cookshare.model.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {


    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var tvSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        tvSignIn = findViewById(R.id.tvSignIn)
    }

    private fun setupListeners() {

        btnCreateAccount.setOnClickListener {
            handleRegister()
        }

        tvSignIn.setOnClickListener {
            finish() // return to LoginActivity
        }
    }

    private fun handleRegister() {

        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        btnCreateAccount.isEnabled = false
        btnCreateAccount.text = "Creating..."

        val request = RegisterRequest(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )

        RetrofitClient.instance.register(request).enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {

                btnCreateAccount.isEnabled = true
                btnCreateAccount.text = "Create Account"

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created successfully! Please log in.",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } else {

                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

                btnCreateAccount.isEnabled = true
                btnCreateAccount.text = "Create Account"

                Toast.makeText(
                    this@RegisterActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

}
