package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.fitnesswarrior.R
import com.fitnesswarrior.services.GameManager

class FitnessQuestionnaireActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private var currentStep = 0

    // views
    private lateinit var tvStepTitle: TextView
    private lateinit var tvStepDescription: TextView
    private lateinit var containerStep1: LinearLayout
    private lateinit var containerStep2: LinearLayout
    private lateinit var containerStep3: LinearLayout
    private lateinit var containerStep4: LinearLayout
    private lateinit var containerStep5: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var tvProgress: TextView
// all the questions needed for survey will expand later
    //step1 - Height
    private lateinit var npFeet: NumberPicker
    private lateinit var npInches: NumberPicker

    //step 2 - Weight
    private lateinit var etWeight: EditText

    //step 3 - Sex
    private lateinit var rgSex: RadioGroup

    //step 4 - Gym frequency
    private lateinit var spinnerGymFreq: Spinner

    //step 5 - Limitations
    private lateinit var etLimitations: EditText
    private lateinit var cbNone: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness_questionnaire)

        gameManager = GameManager.getInstance(this)
        initViews()
        showStep(0)
    }

    private fun initViews() {
        tvStepTitle = findViewById(R.id.tvStepTitle)
        tvStepDescription = findViewById(R.id.tvStepDescription)
        containerStep1 = findViewById(R.id.containerStep1)
        containerStep2 = findViewById(R.id.containerStep2)
        containerStep3 = findViewById(R.id.containerStep3)
        containerStep4 = findViewById(R.id.containerStep4)
        containerStep5 = findViewById(R.id.containerStep5)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)
        tvProgress = findViewById(R.id.tvProgress)

        // step 1
        npFeet = findViewById(R.id.npFeet)
        npInches = findViewById(R.id.npInches)
        npFeet.minValue = 3; npFeet.maxValue = 7; npFeet.value = 5
        npInches.minValue = 0; npInches.maxValue = 11; npInches.value = 10

        // step 2
        etWeight = findViewById(R.id.etWeight)

        // step 3
        rgSex = findViewById(R.id.rgSex)

        // step 4
        spinnerGymFreq = findViewById(R.id.spinnerGymFreq)
        val frequencies = arrayOf("Never", "1-2 times/week", "3-4 times/week", "5-6 times/week", "Daily")
        spinnerGymFreq.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
        spinnerGymFreq.setSelection(2) // default

        // step 5
        etLimitations = findViewById(R.id.etLimitations)
        cbNone = findViewById(R.id.cbNone)
        cbNone.setOnCheckedChangeListener { _, isChecked ->
            etLimitations.isEnabled = !isChecked
            if (isChecked) etLimitations.setText("")
        }

        btnNext.setOnClickListener { onNextClicked() }
        btnBack.setOnClickListener { onBackClicked() }
    }

    private fun showStep(step: Int) {
        currentStep = step
        containerStep1.visibility = View.GONE
        containerStep2.visibility = View.GONE
        containerStep3.visibility = View.GONE
        containerStep4.visibility = View.GONE
        containerStep5.visibility = View.GONE
        btnBack.visibility = if (step > 0) View.VISIBLE else View.INVISIBLE
        tvProgress.text = "Step ${step + 1} of 5"

        when (step) {
            0 -> {
                tvStepTitle.text = "How tall are you?"
                tvStepDescription.text = "This helps us scale your daily objectives appropriately."
                containerStep1.visibility = View.VISIBLE
                btnNext.text = "NEXT →"
            }
            1 -> {
                tvStepTitle.text = "How much do you weigh?"
                tvStepDescription.text = "Enter your current weight in pounds (lbs)."
                containerStep2.visibility = View.VISIBLE
                btnNext.text = "NEXT →"
            }
            2 -> {
                tvStepTitle.text = "What is your sex?"
                tvStepDescription.text = "This helps us calculate appropriate exercise intensity."
                containerStep3.visibility = View.VISIBLE
                btnNext.text = "NEXT →"
            }
            3 -> {
                tvStepTitle.text = "How often do you exercise?"
                tvStepDescription.text = "How frequently do you currently work out with free weights?"
                containerStep4.visibility = View.VISIBLE
                btnNext.text = "NEXT →"
            }
            4 -> {
                tvStepTitle.text = "Any physical limitations?"
                tvStepDescription.text = "List any disabilities, injuries, or conditions we should know about (e.g., amputee, bad knee, shoulder injury)."
                containerStep5.visibility = View.VISIBLE
                btnNext.text = "START ADVENTURE ⚔️"
            }
        }
    }

    private fun onNextClicked() {
        when (currentStep) {
            0 -> {
                gameManager.player.heightFeet = npFeet.value
                gameManager.player.heightInches = npInches.value
                showStep(1)
            }
            1 -> {
                val weightStr = etWeight.text.toString().trim()
                if (weightStr.isEmpty()) {
                    Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show()
                    return
                }
                val weight = weightStr.toIntOrNull()
                if (weight == null || weight < 50 || weight > 600) {
                    Toast.makeText(this, "Please enter a valid weight (50-600 lbs)", Toast.LENGTH_SHORT).show()
                    return
                }
                gameManager.player.weightLbs = weight
                showStep(2)
            }
            2 -> {
                val selectedId = rgSex.checkedRadioButtonId
                if (selectedId == -1) {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    return
                }
                val selectedRadio = findViewById<RadioButton>(selectedId)
                gameManager.player.sex = selectedRadio.text.toString()
                showStep(3)
            }
            3 -> {
                gameManager.player.gymFrequency = spinnerGymFreq.selectedItem.toString()
                showStep(4)
            }
            4 -> {
                //save data
                gameManager.player.limitations = if (cbNone.isChecked) "None" else etLimitations.text.toString().trim().ifEmpty { "None" }

                //calculate level
                val mult = gameManager.player.getIntensityMultiplier()
                gameManager.player.fitnessLevel = when {
                    mult < 0.65 -> "Beginner"
                    mult < 1.1 -> "Intermediate"
                    else -> "Advanced"
                }

                gameManager.player.hasCompletedQuestionnaire = true
                gameManager.saveGameState()

                //reset quests
                gameManager.resetDailyQuests()

                //next screen
                startActivity(Intent(this, QuestionnaireActivity::class.java))
                finish()
            }
        }
    }

    private fun onBackClicked() {
        if (currentStep > 0) showStep(currentStep - 1)
    }
}
