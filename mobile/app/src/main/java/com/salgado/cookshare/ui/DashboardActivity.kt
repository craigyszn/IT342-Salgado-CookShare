package com.salgado.cookshare.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.salgado.cookshare.R
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvWelcomeUser: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnViewProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        loadUserData()
        setupListeners()
    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tvUsername)
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        btnLogout = findViewById(R.id.btnLogout)
        btnViewProfile = findViewById(R.id.btnViewProfile)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("cookshare_prefs", MODE_PRIVATE)
        val userData = prefs.getString("user_data", null)

        if (userData != null) {
            try {
                val user = JSONObject(userData)
                val firstName = user.optString("userFirstName", "")
                val lastName = user.optString("userLastName", "")
                val fullName = "$firstName $lastName".trim()

                tvUsername.text = if (fullName.isNotEmpty()) fullName else "Guest"
                tvWelcomeUser.text = if (fullName.isNotEmpty()) fullName else "Guest User"
            } catch (e: Exception) {
                tvUsername.text = "Guest"
                tvWelcomeUser.text = "Guest User"
            }
        } else {
            tvUsername.text = "Guest"
            tvWelcomeUser.text = "Guest User"
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            logout()
        }

        btnViewProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
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