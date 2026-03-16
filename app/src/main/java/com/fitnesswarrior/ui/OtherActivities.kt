package com.fitnesswarrior.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fitnesswarrior.R
import com.fitnesswarrior.models.*
import com.fitnesswarrior.services.GameManager

class QuestionnaireActivity : AppCompatActivity() {
    private lateinit var gameManager: GameManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)
        gameManager = GameManager.getInstance(this)
        findViewById<Button>(R.id.btnWarrior)?.setOnClickListener { selectClass(CharacterClass.WARRIOR) }
        findViewById<Button>(R.id.btnMage)?.setOnClickListener { selectClass(CharacterClass.MAGE) }
        findViewById<Button>(R.id.btnNinja)?.setOnClickListener { selectClass(CharacterClass.NINJA) }
        findViewById<Button>(R.id.btnPaladin)?.setOnClickListener { selectClass(CharacterClass.PALADIN) }
        findViewById<Button>(R.id.btnRanger)?.setOnClickListener { selectClass(CharacterClass.RANGER) }
        findViewById<Button>(R.id.btnMonk)?.setOnClickListener { selectClass(CharacterClass.MONK) }
    }
    private fun selectClass(c: CharacterClass) {
        gameManager.player.characterClass = c; gameManager.saveGameState()
        startActivity(Intent(this, MainActivity::class.java)); finish()
    }
}

class CharacterActivity : AppCompatActivity() {
    private lateinit var gm: GameManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character)
        gm = GameManager.getInstance(this)
        setupNav(); updateUI()
    }
    override fun onResume() { super.onResume(); updateUI() }
    private fun updateUI() {
        val p = gm.player
        findViewById<TextView>(R.id.tvHP)?.text = "${p.getTotalMaxHealth()} HP"
        findViewById<TextView>(R.id.tvGoldDisplay)?.text = "${p.gold} ⚡"
        findViewById<TextView>(R.id.tvLevel)?.text = "LVL ${p.level} ${p.characterClass.displayName.uppercase()}"
        findViewById<TextView>(R.id.tvStr)?.text = "STR: ${p.getTotalStrength()}"
        findViewById<TextView>(R.id.tvAgi)?.text = "AGI: ${p.agility}"
        findViewById<TextView>(R.id.tvInt)?.text = "INT: ${p.intelligence}"
        findViewById<TextView>(R.id.tvDef)?.text = "DEF: ${p.getTotalDefense()}"
        findViewById<TextView>(R.id.tvCharacterEmoji)?.text = p.characterClass.emoji
        // show equipment
        val weaponName = p.equippedWeapon?.let { "${it.emoji} ${it.name} (+${it.strengthBonus} STR)" } ?: "None equipped"
        val armorName = p.equippedArmor?.let { "${it.emoji} ${it.name} (+${it.defenseBonus} DEF)" } ?: "None equipped"
        findViewById<TextView>(R.id.tvEquippedWeapon)?.text = "⚔️ Weapon: $weaponName"
        findViewById<TextView>(R.id.tvEquippedArmor)?.text = "🛡️ Armor: $armorName"
        // xp bar
        val xpNeeded = p.experienceForNextLevel()
        findViewById<TextView>(R.id.tvXpProgress)?.text = "XP: ${p.experience}/${xpNeeded}"

        findViewById<Button>(R.id.btnOpenArmory)?.setOnClickListener { startActivity(Intent(this, ArmoryActivity::class.java)) }
        findViewById<Button>(R.id.btnClassChange)?.setOnClickListener {
            if (gm.player.gold >= 1000) {
                gm.player.gold -= 1000; gm.saveGameState()
                startActivity(Intent(this, QuestionnaireActivity::class.java))
            } else Toast.makeText(this, "Need 1000⚡ for class change!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupNav() {
        findViewById<ImageButton>(R.id.btnHome)?.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<ImageButton>(R.id.btnCharacter)?.setOnClickListener { }
        findViewById<ImageButton>(R.id.btnBattle)?.setOnClickListener { startActivity(Intent(this, WorldMapActivity::class.java)) }
        findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
}

class ArmoryActivity : AppCompatActivity() {
    private lateinit var gm: GameManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_armory)
        gm = GameManager.getInstance(this)
        updateGold()
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }
        findViewById<Button>(R.id.btnPotions)?.setOnClickListener { startActivity(Intent(this, ShopListActivity::class.java).putExtra("category", "potions")) }
        findViewById<Button>(R.id.btnShields)?.setOnClickListener { startActivity(Intent(this, ShopListActivity::class.java).putExtra("category", "shields")) }
        findViewById<Button>(R.id.btnWeapons)?.setOnClickListener { startActivity(Intent(this, ShopListActivity::class.java).putExtra("category", "weapons")) }
    }
    override fun onResume() { super.onResume(); updateGold() }
    private fun updateGold() { findViewById<TextView>(R.id.tvGold)?.text = "${gm.player.gold} ⚡" }

    private fun showPotionShop() {
        val potions = ShopCatalog.getPotions()
        val items = potions.map { p ->
            val owned = gm.player.inventory.find { it.id == p.id }?.quantity ?: 0
            "${p.emoji} ${p.name} - ${p.price}⚡ (x$owned)\n   ${p.description}"
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle("🧪 Potions & Items").setItems(items) { _, w ->
            val p = potions[w]
            if (gm.buyPotion(p)) { Toast.makeText(this, "Bought ${p.name}! 🎉", Toast.LENGTH_SHORT).show(); updateGold() }
            else Toast.makeText(this, "Need ${p.price}⚡!", Toast.LENGTH_SHORT).show()
        }.setNegativeButton("Close", null).show()
    }

    private fun showEquipShop(items: List<Equipment>, title: String) {
        val display = items.map { item ->
            val owned = gm.player.ownedEquipment.any { it.id == item.id }
            val eq = gm.player.equippedWeapon?.id == item.id || gm.player.equippedArmor?.id == item.id
            val status = when { eq -> " ✅ EQUIPPED"; owned -> " ✓ OWNED"; gm.player.level < item.levelRequired -> " 🔒 Lvl ${item.levelRequired}"; else -> " - ${item.price}⚡" }
            val stats = if (item.type == EquipmentType.WEAPON) "+${item.strengthBonus} STR" else "+${item.defenseBonus} DEF +${item.healthBonus} HP"
            "${item.emoji} ${item.name}$status\n   ${"★".repeat(item.rarity.ordinal + 1)} ${item.rarity.displayName} | $stats"
        }.toTypedArray()
        AlertDialog.Builder(this).setTitle(title).setItems(display) { _, w ->
            val item = items[w]
            val owned = gm.player.ownedEquipment.any { it.id == item.id }
            val eq = gm.player.equippedWeapon?.id == item.id || gm.player.equippedArmor?.id == item.id
            when {
                eq -> Toast.makeText(this, "Already equipped!", Toast.LENGTH_SHORT).show()
                owned -> { gm.equipItem(item); Toast.makeText(this, "Equipped ${item.name}! ⚔️", Toast.LENGTH_SHORT).show() }
                gm.player.level < item.levelRequired -> Toast.makeText(this, "Need level ${item.levelRequired}!", Toast.LENGTH_SHORT).show()
                else -> {
                    AlertDialog.Builder(this).setTitle("Buy ${item.name}?")
                        .setMessage("${item.description}\n\nCost: ${item.price}⚡\nYour Gold: ${gm.player.gold}⚡")
                        .setPositiveButton("BUY") { _, _ ->
                            if (gm.buyEquipment(item)) { gm.equipItem(item); Toast.makeText(this, "Bought & equipped! 🎉", Toast.LENGTH_SHORT).show(); updateGold() }
                            else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
                        }.setNegativeButton("Cancel", null).show()
                }
            }
        }.setNegativeButton("Close", null).show()
    }
}

class WorldMapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_map)
        findViewById<Button>(R.id.btnLevel1)?.setOnClickListener { startBattle(1) }
        findViewById<Button>(R.id.btnLevel2)?.setOnClickListener { startBattle(2) }
        findViewById<Button>(R.id.btnLevel3)?.setOnClickListener { startBattle(3) }
        findViewById<Button>(R.id.btnLevel4)?.setOnClickListener { startBattle(4) }
        findViewById<ImageButton>(R.id.btnHome)?.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<ImageButton>(R.id.btnCharacter)?.setOnClickListener { startActivity(Intent(this, CharacterActivity::class.java)) }
        findViewById<ImageButton>(R.id.btnSettings)?.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }
    private fun startBattle(level: Int) { startActivity(Intent(this, BattleActivity::class.java).apply { putExtra("level", level) }) }
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val gm = GameManager.getInstance(this)

        // retake questionnaire
        findViewById<Button>(R.id.btnRetakeQuestionnaire)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Retake Fitness Questionnaire?")
                .setMessage("This will update your exercise intensity based on your new answers.")
                .setPositiveButton("Retake") { _, _ ->
                    gm.player.hasCompletedQuestionnaire = false
                    gm.saveGameState()
                    startActivity(Intent(this, FitnessQuestionnaireActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //logout
        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout?")
                .setMessage("You can log back in with the same name to resume your progress.")
                .setPositiveButton("Logout") { _, _ ->
                    gm.clearAllData()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //delete data do we can rest with/ questionaire
        findViewById<Button>(R.id.btnDeleteAllData)?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("⚠️ Delete ALL Data?")
                .setMessage("This will permanently erase your warrior, level, gold, equipment, quests, and fitness profile. You will start completely over.\n\nThis cannot be undone!")
                .setPositiveButton("DELETE EVERYTHING") { _, _ ->
                    gm.clearAllData()
                    Toast.makeText(this, "All data deleted!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        //show profile
        updateProfile(gm)
    }

    private fun updateProfile(gm: GameManager) {
        val p = gm.player
        val profileText = "Height: ${p.heightFeet}'${p.heightInches}\" \u2022 Weight: ${p.weightLbs} lbs\n" +
            "Fitness Level: ${p.fitnessLevel} \u2022 Gym: ${p.gymFrequency}\n" +
            "Limitations: ${p.limitations}"
        findViewById<TextView>(R.id.tvFitnessProfile)?.text = profileText
    }
}

class QuestDetailActivity : AppCompatActivity() {
    private lateinit var gm: GameManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quest_detail)
        gm = GameManager.getInstance(this)
        val questId = intent.getStringExtra("quest_id") ?: return
        val quest = gm.dailyQuests.find { it.id == questId } ?: return

        findViewById<TextView>(R.id.tvQuestName)?.text = quest.title
        findViewById<TextView>(R.id.tvQuestDesc)?.text = quest.description
        findViewById<TextView>(R.id.tvQuestProgress)?.text = "${quest.progress}/${quest.target}"
        findViewById<TextView>(R.id.tvQuestReward)?.text = "Reward: ${quest.rewardXp} XP + ${quest.rewardGold}⚡"

        findViewById<Button>(R.id.btnAddProgress)?.setOnClickListener {
            val addAmount = when {
                quest.target <= 5 -> 1
                quest.target <= 20 -> 5
                else -> 10
            }
            quest.progress = (quest.progress + addAmount).coerceAtMost(quest.target)
            if (quest.checkCompletion()) {
                gm.player.addExperience(quest.rewardXp)
                gm.player.gold += quest.rewardGold
                gm.saveGameState()
                Toast.makeText(this, "🎉 Quest Complete! +${quest.rewardXp}XP +${quest.rewardGold}⚡", Toast.LENGTH_LONG).show()
            }
            gm.saveGameState()
            findViewById<TextView>(R.id.tvQuestProgress)?.text = "${quest.progress}/${quest.target}"
        }

        findViewById<Button>(R.id.btnBackToQuests)?.setOnClickListener { finish() }
    }
}

class VictoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)
        val xp = intent.getIntExtra("xp_reward", 50)
        val gold = intent.getIntExtra("gold_reward", 30)
        val enemyName = intent.getStringExtra("enemy_name") ?: "Enemy"

        findViewById<TextView>(R.id.tvTitle)?.text = "$enemyName Defeated!"
        findViewById<TextView>(R.id.tvReward)?.text = "+${xp}XP  +${gold}⚡"
        findViewById<Button>(R.id.btnContinue)?.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }
        findViewById<Button>(R.id.btnShop)?.setOnClickListener { startActivity(Intent(this, ArmoryActivity::class.java)) }
    }
}
