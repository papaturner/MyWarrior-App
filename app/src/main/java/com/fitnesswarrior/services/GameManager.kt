package com.fitnesswarrior.services

import android.content.Context
import android.util.Log
import com.fitnesswarrior.models.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GameManager private constructor(private val context: Context) {

    private val TAG = "GameManager"

    var player: Player = loadPlayer()
        private set

    var dailyQuests: MutableList<Quest> = mutableListOf()
        private set

    var currentLevel: Int = 1
        private set

    init {
        try {
            loadGameState()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading game state, using defaults", e)
            dailyQuests = QuestFactory.getDailyQuests(player).toMutableList()
        }
    }

    private fun loadPlayer(): Player {
        return try {
            Player.load(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading player, creating new", e)
            Player()
        }
    }

    private fun loadGameState() {
        val prefs = context.getSharedPreferences("game_state", Context.MODE_PRIVATE)
        currentLevel = prefs.getInt("current_level", 1)
        val questsJson = prefs.getString("daily_quests", null)
        if (questsJson != null) {
            try {
                val type = object : TypeToken<MutableList<Quest>>() {}.type
                val loaded: MutableList<Quest>? = Gson().fromJson(questsJson, type)
                dailyQuests = loaded ?: QuestFactory.getDailyQuests(player).toMutableList()
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing quests", e)
                dailyQuests = QuestFactory.getDailyQuests(player).toMutableList()
            }
        } else {
            dailyQuests = QuestFactory.getDailyQuests(player).toMutableList()
        }
    }

    fun saveGameState() {
        try {
            player.save(context)
            context.getSharedPreferences("game_state", Context.MODE_PRIVATE).edit()
                .putInt("current_level", currentLevel)
                .putString("daily_quests", Gson().toJson(dailyQuests))
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving game state", e)
        }
    }

    fun updateQuestProgress(questId: String, progress: Int) {
        dailyQuests.find { it.id == questId }?.let { quest ->
            quest.progress = progress
            if (quest.checkCompletion()) {
                player.addExperience(quest.rewardXp)
                player.gold += quest.rewardGold
                saveGameState()
            }
        }
    }

    fun addExerciseProgress(exerciseType: ExerciseType, count: Int) {
        dailyQuests.filter { it.exerciseType == exerciseType && !it.isCompleted }.forEach { quest ->
            quest.progress += count
            quest.checkCompletion()
        }
        player.totalWorkouts++
        player.totalCalories += (exerciseType.caloriesPerRep * count).toInt()
        saveGameState()
    }

    fun addSteps(steps: Int) { player.totalSteps += steps; saveGameState() }

    //our shop system will need to add more to our shop ui

    fun buyPotion(item: InventoryItem): Boolean {
        if (player.gold < item.price) return false
        player.gold -= item.price
        val existing = player.inventory.find { it.id == item.id }
        if (existing != null) {
            existing.quantity++
        } else {
            player.inventory.add(item.copy(quantity = 1))
        }
        saveGameState()
        return true
    }

    fun buyEquipment(item: Equipment): Boolean {
        if (player.gold < item.price) return false
        if (player.level < item.levelRequired) return false
        if (player.ownedEquipment.any { it.id == item.id }) return false
        player.gold -= item.price
        player.ownedEquipment.add(item)
        saveGameState()
        return true
    }

    fun equipItem(item: Equipment) {
        when (item.type) {
            EquipmentType.WEAPON -> player.equippedWeapon = item
            EquipmentType.ARMOR, EquipmentType.SHIELD -> player.equippedArmor = item
            EquipmentType.ACCESSORY -> player.equippedAccessory = item
            else -> {}
        }
        saveGameState()
    }

    fun completeBattle(enemy: Enemy, won: Boolean) {
        if (won) {
            player.addExperience(enemy.xpReward)
            player.gold += enemy.goldReward
            if (enemy.isBoss) currentLevel++
        }
        saveGameState()
    }

    fun completeBattleReward(xp: Int, gold: Int) {
        player.addExperience(xp)
        player.gold += gold
        saveGameState()
    }

    fun changeClass(newClass: CharacterClass, cost: Int): Boolean {
        if (player.gold >= cost) {
            player.gold -= cost
            player.characterClass = newClass
            saveGameState()
            return true
        }
        return false
    }

    fun resetDailyQuests() {
        dailyQuests = QuestFactory.getDailyQuests(player).toMutableList()
        player.currentStreak++
        saveGameState()
    }

    //clear all data
    fun clearAllData() {
        context.getSharedPreferences("player_data", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("game_state", Context.MODE_PRIVATE).edit().clear().apply()
        player = Player()
        dailyQuests = mutableListOf()
        currentLevel = 1
        INSTANCE = null
    }

    companion object {
        @Volatile private var INSTANCE: GameManager? = null
        fun getInstance(context: Context): GameManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GameManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
