package com.fitnesswarrior.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fitnesswarrior.R
import com.fitnesswarrior.game.*
import com.fitnesswarrior.services.GameManager

class BattleActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private var battleSystem: BattleSystem? = null

    private lateinit var tvEnemyName: TextView
    private lateinit var tvEnemyHp: TextView
    private lateinit var tvEnemyEmoji: TextView
    private lateinit var pbEnemyHp: ProgressBar
    private lateinit var tvPlayerHp: TextView
    private lateinit var tvPlayerMp: TextView
    private lateinit var pbPlayerHp: ProgressBar
    private lateinit var pbPlayerMp: ProgressBar
    private lateinit var tvBattleLog: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val battleLogMessages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)
        gameManager = GameManager.getInstance(this)
        initViews()
        setupBattle()
    }

    private fun initViews() {
        tvEnemyName = findViewById(R.id.tvEnemyName)
        tvEnemyHp = findViewById(R.id.tvEnemyHP)
        tvEnemyEmoji = findViewById(R.id.tvEnemyEmoji)
        pbEnemyHp = findViewById(R.id.pbEnemyHp) ?: ProgressBar(this)
        tvPlayerHp = findViewById(R.id.tvPlayerHp) ?: TextView(this)
        tvPlayerMp = findViewById(R.id.tvPlayerMp) ?: TextView(this)
        pbPlayerHp = findViewById(R.id.pbPlayerHp) ?: ProgressBar(this)
        pbPlayerMp = findViewById(R.id.pbPlayerMp) ?: ProgressBar(this)
        tvBattleLog = findViewById(R.id.tvBattleLog) ?: TextView(this)

        findViewById<Button>(R.id.btnAttack)?.setOnClickListener { onAttackClicked() }
        findViewById<Button>(R.id.btnSkills)?.setOnClickListener { onSkillsClicked() }
        findViewById<Button>(R.id.btnHeal)?.setOnClickListener { onHealClicked() }
        findViewById<Button>(R.id.btnDefend)?.setOnClickListener { onDefendClicked() }
        findViewById<Button>(R.id.btnRun)?.setOnClickListener { onRunClicked() }
    }

    private fun setupBattle() {
        val level = intent.getIntExtra("level", 1)
        val enemyId = intent.getStringExtra("enemy_id") ?: getEnemyForLevel(level)

        val playerParticipant = BattleFactory.createPlayerParticipant(gameManager.player)
        val enemyParticipant = BattleFactory.createEnemy(enemyId)

        battleSystem = BattleSystem(playerParticipant, enemyParticipant)
        battleSystem?.onBattleEvent = { event -> handleBattleEvent(event) }
        battleSystem?.onBattleEnd = { result -> handleBattleEnd(result) }

        updateUI()
        addToLog("⚔️ Battle Start!")
        addToLog("A wild ${enemyParticipant.name} appeared!")

        tvEnemyName.text = enemyParticipant.name
        tvEnemyEmoji.text = getEnemyEmoji(enemyId)
    }

    private fun getEnemyForLevel(level: Int): String = when (level) {
        1 -> listOf("slime", "goblin", "skeleton").random()
        2 -> listOf("wolf", "orc", "troll").random()
        3 -> listOf("bandit", "golem", "dark_knight").random()
        4 -> "sloth_king"
        else -> "ancient_dragon"
    }

    private fun getEnemyEmoji(id: String): String = when (id) {
        "slime" -> "🟢"; "goblin" -> "👺"; "skeleton", "skeleton_boss" -> "💀"
        "wolf" -> "🐺"; "orc" -> "👹"; "troll", "troll_boss" -> "🧌"
        "bandit" -> "🗡️"; "golem" -> "🗿"; "dark_knight", "dark_knight_boss" -> "⚔️"
        "dragon_young", "ancient_dragon" -> "🐉"; "demon" -> "😈"; "sloth_king" -> "🦥"
        else -> "👾"
    }

    // get meter speed based on enemy level, harder enemies are faster
    private fun getMeterSpeed(): Long {
        val level = battleSystem?.enemyLevel ?: 1
        return when {
            level >= 20 -> 400L
            level >= 15 -> 500L
            level >= 10 -> 600L
            level >= 5 -> 700L
            else -> 850L
        }
    }

    // show the attack meter dialog then apply the result
    private fun showAttackMeter(attackName: String, onResult: (Float) -> Unit) {
        val meter = AttackMeterDialog(this) { label, multiplier ->
            addToLog("⏱️ $label (${multiplier}x)")
            onResult(multiplier)
        }
        meter.sweepSpeed = getMeterSpeed()
        meter.show(attackName)
    }

    // player actions section

    private fun onAttackClicked() {
        if (battleSystem?.currentTurn != Turn.PLAYER) return
        disableActions()

        // show meter then apply damage with multiplier
        showAttackMeter("⚔️ Basic Attack") { multiplier ->
            val result = battleSystem?.playerAttackWithMultiplier(multiplier)
            result?.let { addToLog(it.message) }
            handler.postDelayed({ updateUI(); enableActions() }, 500)
        }
    }

    private fun onSkillsClicked() {
        if (battleSystem?.currentTurn != Turn.PLAYER) return
        val skills = battleSystem?.getPlayerSkills() ?: emptyList()
        if (skills.isEmpty()) {
            Toast.makeText(this, "No skills available! Level up to unlock skills.", Toast.LENGTH_SHORT).show()
            return
        }
        showSkillDialog(skills)
    }

    private fun onHealClicked() {
        if (battleSystem?.currentTurn != Turn.PLAYER) return

        //check heals
        val healSkills = battleSystem?.getPlayerSkills()?.filter { it.type == SkillType.HEAL }
        val potions = battleSystem?.getPlayerItems()?.filter { it.type == BattleItemType.HEALING && it.quantity > 0 }

        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        //add heals
        healSkills?.forEach { skill ->
            options.add("🌿 ${skill.name} (${skill.mpCost} MP) - ${skill.description}")
            actions.add {
                disableActions()
                val result = battleSystem?.playerUseSkill(skill)
                result?.let { addToLog(it.message) }
                handler.postDelayed({ updateUI(); enableActions() }, 500)
            }
        }

        // add potions
        potions?.forEach { potion ->
            options.add("🧪 ${potion.name} x${potion.quantity} (+${potion.power} HP)")
            actions.add {
                disableActions()
                potion.quantity--
                val result = battleSystem?.playerUseItem(potion)
                result?.let { addToLog(it.message) }
                handler.postDelayed({ updateUI(); enableActions() }, 500)
            }
        }

        //add mp potions
        val mpPotions = battleSystem?.getPlayerItems()?.filter { it.type == BattleItemType.MP_RESTORE && it.quantity > 0 }
        mpPotions?.forEach { potion ->
            options.add("💙 ${potion.name} x${potion.quantity} (+${potion.power} MP)")
            actions.add {
                disableActions()
                potion.quantity--
                val result = battleSystem?.playerUseItem(potion)
                result?.let { addToLog(it.message) }
                handler.postDelayed({ updateUI(); enableActions() }, 500)
            }
        }

        if (options.isEmpty()) {
            Toast.makeText(this, "No healing available!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Heal / Items")
            .setItems(options.toTypedArray()) { _, which -> actions[which]() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun onDefendClicked() {
        if (battleSystem?.currentTurn != Turn.PLAYER) return
        disableActions()
        val result = battleSystem?.playerDefend()
        result?.let { addToLog(it.message) }
        handler.postDelayed({ updateUI(); enableActions() }, 500)
    }

    private fun onRunClicked() {
        if (battleSystem?.currentTurn != Turn.PLAYER) return
        disableActions()
        val result = battleSystem?.playerRun()
        result?.let { addToLog(it.message) }
        if (result?.success == true) {
            handler.postDelayed({ finish() }, 1000)
        } else {
            handler.postDelayed({ updateUI(); enableActions() }, 500)
        }
    }

    private fun showSkillDialog(skills: List<Skill>) {
        val skillNames = skills.map { skill ->
            val typeIcon = when (skill.type) {
                SkillType.DAMAGE -> "⚔️"
                SkillType.HEAL -> "💚"
                SkillType.BUFF -> "⬆️"
                SkillType.DEBUFF -> "⬇️"
                SkillType.STATUS -> "💫"
            }
            "$typeIcon ${skill.name} (${skill.mpCost} MP) - ${skill.description}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Choose Skill")
            .setItems(skillNames) { _, which ->
                val skill = skills[which]
                disableActions()

                // damage skills use the meter, others go straight through
                if (skill.type == SkillType.DAMAGE) {
                    showAttackMeter("✨ ${skill.name}") { multiplier ->
                        val result = battleSystem?.playerUseSkillWithMultiplier(skill, multiplier)
                        result?.let { addToLog(it.message) }
                        handler.postDelayed({ updateUI(); enableActions() }, 500)
                    }
                } else {
                    val result = battleSystem?.playerUseSkill(skill)
                    result?.let { addToLog(it.message) }
                    handler.postDelayed({ updateUI(); enableActions() }, 500)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //events section

    private fun handleBattleEvent(event: BattleEvent) {
        when (event) {
            is BattleEvent.Damage -> animateDamage(!event.isPlayer)
            is BattleEvent.Heal -> animateHeal(event.isPlayer)
            is BattleEvent.CriticalHit -> addToLog("💥 CRITICAL HIT!")
            is BattleEvent.Miss -> addToLog("Attack missed!")
            is BattleEvent.StatusApplied -> addToLog("${event.status.type.emoji} Status applied!")
            else -> {}
        }
        updateUI()
    }

    private fun handleBattleEnd(result: BattleResult) {
        disableActions()
        when (result) {
            BattleResult.VICTORY -> {
                addToLog("🎉 VICTORY!")
                val xp = battleSystem?.enemyXpReward ?: 50
                val gold = battleSystem?.enemyGoldReward ?: 30
                gameManager.completeBattleReward(xp, gold)
                handler.postDelayed({
                    val intent = Intent(this, VictoryActivity::class.java)
                    intent.putExtra("xp_reward", xp)
                    intent.putExtra("gold_reward", gold)
                    intent.putExtra("enemy_name", battleSystem?.enemyName ?: "Enemy")
                    startActivity(intent)
                    finish()
                }, 1500)
            }
            BattleResult.DEFEAT -> {
                addToLog("💀 DEFEAT...")
                handler.postDelayed({
                    AlertDialog.Builder(this)
                        .setTitle("Defeated!")
                        .setMessage("You were defeated in battle...")
                        .setPositiveButton("Try Again") { _, _ -> recreate() }
                        .setNegativeButton("Return") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                }, 1500)
            }
            BattleResult.FLED -> addToLog("🏃 Escaped!")
        }
    }

    //ui section

    private fun updateUI() {
        val s = battleSystem?.getBattleSummary() ?: return
        tvEnemyHp.text = "HP: ${s.enemyHp}/${s.enemyMaxHp}"
        pbEnemyHp.max = s.enemyMaxHp; pbEnemyHp.progress = s.enemyHp
        tvPlayerHp.text = "HP: ${s.playerHp}/${s.playerMaxHp}"
        tvPlayerMp.text = "MP: ${s.playerMp}/${s.playerMaxMp}"
        pbPlayerHp.max = s.playerMaxHp; pbPlayerHp.progress = s.playerHp
        pbPlayerMp.max = s.playerMaxMp; pbPlayerMp.progress = s.playerMp
        tvBattleLog.text = battleLogMessages.takeLast(5).joinToString("\n")
    }

    private fun addToLog(msg: String) {
        battleLogMessages.add(msg)
        tvBattleLog.text = battleLogMessages.takeLast(5).joinToString("\n")
    }

    private fun animateDamage(isEnemy: Boolean) {
        val target = if (isEnemy) tvEnemyEmoji else tvPlayerHp
        ObjectAnimator.ofFloat(target, "translationX", 0f, 25f, -25f, 15f, -15f, 0f).apply { duration = 400; start() }
    }

    private fun animateHeal(isPlayer: Boolean) {
        val target = if (isPlayer) tvPlayerHp else tvEnemyHp
        ObjectAnimator.ofFloat(target, "alpha", 1f, 0.5f, 1f).apply { duration = 300; start() }
    }

    private fun disableActions() {
        findViewById<Button>(R.id.btnAttack)?.isEnabled = false
        findViewById<Button>(R.id.btnSkills)?.isEnabled = false
        findViewById<Button>(R.id.btnHeal)?.isEnabled = false
        findViewById<Button>(R.id.btnDefend)?.isEnabled = false
        findViewById<Button>(R.id.btnRun)?.isEnabled = false
    }

    private fun enableActions() {
        if (battleSystem?.currentTurn == Turn.PLAYER && battleSystem?.battleState == BattleState.ONGOING) {
            findViewById<Button>(R.id.btnAttack)?.isEnabled = true
            findViewById<Button>(R.id.btnSkills)?.isEnabled = true
            findViewById<Button>(R.id.btnHeal)?.isEnabled = true
            findViewById<Button>(R.id.btnDefend)?.isEnabled = true
            findViewById<Button>(R.id.btnRun)?.isEnabled = true
        }
    }
}
