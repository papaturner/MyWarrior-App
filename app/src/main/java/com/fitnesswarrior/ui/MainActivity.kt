package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnesswarrior.R
import com.fitnesswarrior.models.Quest
import com.fitnesswarrior.models.QuestFactory
import com.fitnesswarrior.services.GameManager

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //handle errors
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "UNCAUGHT EXCEPTION in $thread", throwable)
        }
        
        Log.d(TAG, "onCreate start")
        setContentView(R.layout.activity_main)
        Log.d(TAG, "setContentView done")

        try {
            gameManager = GameManager.getInstance(this)
            Log.d(TAG, "GameManager OK, player=${gameManager.player.name}")
        } catch (e: Exception) {
            Log.e(TAG, "GameManager failed", e)
            gameManager = GameManager.getInstance(this)
        }

        //welcome text
        findViewById<TextView>(R.id.tvWelcome)?.text = "WELCOME ${gameManager.player.name.uppercase()}"
        findViewById<TextView>(R.id.tvStreak)?.text = "STREAK: ${gameManager.player.currentStreak} DAYS  |  ${gameManager.player.gold}⚡"

        //setup list
        val rv = findViewById<RecyclerView>(R.id.rvQuests)
        rv?.layoutManager = LinearLayoutManager(this)
        loadQuests(rv)

        //bottom nav
        findViewById<ImageButton>(R.id.btnHome)?.setOnClickListener {
            Log.d(TAG, "Home clicked")
            Toast.makeText(this, "Already home!", Toast.LENGTH_SHORT).show()
        }
        findViewById<ImageButton>(R.id.btnCharacter)?.setOnClickListener {
            Log.d(TAG, "Character clicked")
            startActivity(Intent(this, CharacterActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnBattle)?.setOnClickListener {
            Log.d(TAG, "Battle clicked")
            startActivity(Intent(this, WorldMapActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener {
            Log.d(TAG, "Settings clicked")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        Log.d(TAG, "onCreate complete")
    }

    private fun loadQuests(rv: RecyclerView?) {
        try {
            val quests = if (gameManager.dailyQuests.isNotEmpty()) {
                gameManager.dailyQuests
            } else {
                QuestFactory.getDailyQuests(gameManager.player)
            }
            Log.d(TAG, "Loading ${quests.size} quests")
            rv?.adapter = QuestAdapter(quests) { quest ->
                Log.d(TAG, "Quest clicked: ${quest.id}")
                val intent = Intent(this, QuestDetailActivity::class.java)
                intent.putExtra("quest_id", quest.id)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quests", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val rv = findViewById<RecyclerView>(R.id.rvQuests)
            loadQuests(rv)
            findViewById<TextView>(R.id.tvStreak)?.text = "STREAK: ${gameManager.player.currentStreak} DAYS  |  ${gameManager.player.gold}⚡"
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }
}

class QuestAdapter(
    private val quests: List<Quest>,
    private val onQuestClick: (Quest) -> Unit
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    class QuestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView? = view.findViewById(R.id.tvQuestTitle)
        val tvReward: TextView? = view.findViewById(R.id.tvReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        return QuestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false))
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]
        holder.tvTitle?.text = if (quest.isCompleted) "✅ ${quest.title}" else quest.title
        holder.tvReward?.text = quest.rewardXp.toString()
        if (quest.isCompleted) {
            holder.tvTitle?.paintFlags = (holder.tvTitle?.paintFlags ?: 0) or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTitle?.paintFlags = (holder.tvTitle?.paintFlags ?: 0) and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        //card click
        holder.itemView.setOnClickListener { onQuestClick(quest) }
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
    }

    override fun getItemCount() = quests.size
}
