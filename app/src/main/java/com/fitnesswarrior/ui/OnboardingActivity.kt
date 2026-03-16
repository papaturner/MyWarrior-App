package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fitnesswarrior.R
import com.fitnesswarrior.services.GameManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        gameManager = GameManager.getInstance(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            if (username.isNotEmpty()) {
                gameManager.player.name = username
                gameManager.saveGameState()

                //go to questionnaire
                startActivity(Intent(this, FitnessQuestionnaireActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        tvCreateAccount.setOnClickListener { btnLogin.performClick() }
    }
}
