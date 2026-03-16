package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fitnesswarrior.R
import com.fitnesswarrior.models.LeaderboardData
import com.fitnesswarrior.models.LeaderboardEntry
import com.fitnesswarrior.services.GameManager
import java.text.NumberFormat

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        gameManager = GameManager.getInstance(this)

        setupViews()
        loadLeaderboard()
    }

    private fun setupViews() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        
        findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        findViewById<Button>(R.id.btnChampions).setOnClickListener {
            // show details
        }
    }

    private fun loadLeaderboard() {
        val leaderboard = LeaderboardData.getWeeklyLeaderboard(gameManager.player)
        
        if (leaderboard.size >= 3) {
            findViewById<TextView>(R.id.tv1stName).text = leaderboard[0].playerName
            findViewById<TextView>(R.id.tv1stSteps).text = "◆ ${formatSteps(leaderboard[0].steps)} Steps"
            
            findViewById<TextView>(R.id.tv2ndName).text = leaderboard[1].playerName
            findViewById<TextView>(R.id.tv2ndSteps).text = "◆ ${formatSteps(leaderboard[1].steps)} Steps"
            
            findViewById<TextView>(R.id.tv3rdName).text = leaderboard[2].playerName
            findViewById<TextView>(R.id.tv3rdSteps).text = "◆ ${formatSteps(leaderboard[2].steps)} Steps"
        }
        
        val otherRanks = if (leaderboard.size > 3) leaderboard.subList(3, leaderboard.size) else emptyList()
        
        val rvLeaderboard = findViewById<RecyclerView>(R.id.rvLeaderboard)
        rvLeaderboard.layoutManager = LinearLayoutManager(this)
        rvLeaderboard.adapter = LeaderboardAdapter(otherRanks)
    }
    
    private fun formatSteps(steps: Int): String {
        return NumberFormat.getNumberInstance().format(steps)
    }
}

class LeaderboardAdapter(
    private val entries: List<LeaderboardEntry>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPlayerName)
        val tvSteps: TextView = view.findViewById(R.id.tvSteps)
        val tvRank: TextView = view.findViewById(R.id.tvRank)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvName.text = entry.playerName
        holder.tvSteps.text = "${NumberFormat.getNumberInstance().format(entry.steps)} Steps"
        holder.tvRank.text = "${entry.rank}${getRankSuffix(entry.rank)}"
    }

    override fun getItemCount() = entries.size
    
    private fun getRankSuffix(rank: Int): String {
        return when {
            rank in 11..13 -> "th"
            rank % 10 == 1 -> "st"
            rank % 10 == 2 -> "nd"
            rank % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}
