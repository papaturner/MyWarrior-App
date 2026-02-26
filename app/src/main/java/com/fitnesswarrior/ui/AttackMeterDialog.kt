package com.fitnesswarrior.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.fitnesswarrior.R

// dialog that shows the attack timing meter during battle
class AttackMeterDialog(
    context: Context,
    private val onResult: (label: String, multiplier: Float) -> Unit
) {
    private val dialog = Dialog(context)
    private val meterView: AttackMeterView
    private val btnStop: Button
    private val tvResult: TextView
    private val tvMultiplier: TextView
    private val tvAttackName: TextView
    private val tvInstruction: TextView
    private val handler = Handler(Looper.getMainLooper())

    // speed in ms for one sweep, lower means faster and harder
    var sweepSpeed = 800L

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_attack_meter)
        dialog.setCancelable(false)

        // make dialog background transparent
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#DD1A1A2E")))
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        // find all the views
        meterView = dialog.findViewById(R.id.attackMeterView)
        btnStop = dialog.findViewById(R.id.btnStop)
        tvResult = dialog.findViewById(R.id.tvResult)
        tvMultiplier = dialog.findViewById(R.id.tvMultiplier)
        tvAttackName = dialog.findViewById(R.id.tvAttackName)
        tvInstruction = dialog.findViewById(R.id.tvInstruction)

        // when player taps stop
        btnStop.setOnClickListener {
            if (meterView.isRunning) {
                val result = meterView.stopMeter()

                // show result text
                tvResult.visibility = View.VISIBLE
                tvResult.text = result.label
                tvResult.setTextColor(result.color)

                tvMultiplier.visibility = View.VISIBLE
                tvMultiplier.text = "${result.multiplier}x Damage!"

                tvInstruction.visibility = View.GONE
                btnStop.isEnabled = false
                btnStop.text = "..."

                // wait a moment then close and send result back
                handler.postDelayed({
                    dialog.dismiss()
                    onResult(result.label, result.multiplier)
                }, 1200)
            }
        }
    }

    // show the dialog with a specific attack name
    fun show(attackName: String) {
        tvAttackName.text = attackName
        tvResult.visibility = View.GONE
        tvMultiplier.visibility = View.GONE
        tvInstruction.visibility = View.VISIBLE
        btnStop.isEnabled = true
        btnStop.text = "STOP!"
        meterView.sweepDuration = sweepSpeed

        dialog.show()

        // small delay before starting so player can see the bar
        handler.postDelayed({ meterView.startMeter() }, 400)
    }
}
