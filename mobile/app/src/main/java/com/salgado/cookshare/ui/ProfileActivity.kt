package com.salgado.cookshare.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.salgado.cookshare.R
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: TextView
    private lateinit var tvInitials: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvFirstName: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvInitials = findViewById(R.id.tvInitials)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvFirstName = findViewById(R.id.tvFirstName)
        tvLastName = findViewById(R.id.tvLastName)
        tvEmail = findViewById(R.id.tvEmail)
        btnLogout = findViewById(R.id.btnLogout)
        btnSignOut = findViewById(R.id.btnSignOut)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        val userData = prefs.getString("user_data", null)

        if (userData != null) {
            try {
                val user = JSONObject(userData)
                val firstName = user.optString("userFirstName", "")
                val lastName = user.optString("userLastName", "")
                val email = user.optString("userEmail", "")
                val fullName = "$firstName $lastName".trim()

                // Set initials
                val initials = if (fullName.isNotEmpty()) {
                    fullName.split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .take(2)
                        .joinToString("")
                } else {
                    "GU"
                }

                tvInitials.text = initials
                tvProfileName.text = if (fullName.isNotEmpty()) fullName else "Guest User"
                tvProfileEmail.text = email.ifEmpty { "—" }
                tvFirstName.text = firstName.ifEmpty { "—" }
                tvLastName.text = lastName.ifEmpty { "—" }
                tvEmail.text = email.ifEmpty { "—" }

            } catch (e: Exception) {
                setDefaultValues()
            }
        } else {
            setDefaultValues()
        }
    }

    private fun setDefaultValues() {
        tvInitials.text = "GU"
        tvProfileName.text = "Guest User"
        tvProfileEmail.text = "—"
        tvFirstName.text = "—"
        tvLastName.text = "—"
        tvEmail.text = "—"
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnLogout.setOnClickListener {
            logout()
        }

        btnSignOut.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}