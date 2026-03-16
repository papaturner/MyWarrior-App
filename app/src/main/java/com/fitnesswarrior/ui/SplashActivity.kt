package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.fitnesswarrior.R
import com.fitnesswarrior.services.GameManager

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "Splash layout set")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting splash layout", e)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val gameManager = GameManager.getInstance(this)
                Log.d(TAG, "GameManager loaded, player name=${gameManager.player.name}, level=${gameManager.player.level}, questionnaire=${gameManager.player.hasCompletedQuestionnaire}")

                val intent = when {
                    gameManager.player.name == "Warrior" && gameManager.player.level == 1 ->
                        Intent(this, OnboardingActivity::class.java)
                    !gameManager.player.hasCompletedQuestionnaire ->
                        Intent(this, FitnessQuestionnaireActivity::class.java)
                    else -> Intent(this, MainActivity::class.java)
                }

                Log.d(TAG, "Navigating to ${intent.component?.className}")
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error in splash routing", e)
                // fallback to onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }
        }, 2000)
    }
}
