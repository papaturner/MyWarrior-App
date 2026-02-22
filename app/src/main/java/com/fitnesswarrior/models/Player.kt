package com.fitnesswarrior.models

import android.content.Context
import com.google.gson.Gson

data class Player(
    var name: String = "Warrior",
    var level: Int = 1,
    var experience: Int = 0,
    var gold: Int = 500,
    var health: Int = 100,
    var maxHealth: Int = 100,
    var strength: Int = 10,
    var agility: Int = 10,
    var intelligence: Int = 10,
    var defense: Int = 10,
    var characterClass: CharacterClass = CharacterClass.WARRIOR,
    var equippedWeapon: Equipment? = null,
    var equippedArmor: Equipment? = null,
    var equippedAccessory: Equipment? = null,
    var inventory: MutableList<InventoryItem> = mutableListOf(),
    var ownedEquipment: MutableList<Equipment> = mutableListOf(),
    var completedQuests: MutableList<String> = mutableListOf(),
    var currentStreak: Int = 0,
    var totalSteps: Int = 0,
    var totalCalories: Int = 0,
    var totalWorkouts: Int = 0,
    // fitness profile
    var heightFeet: Int = 5,
    var heightInches: Int = 10,
    var weightLbs: Int = 170,
    var sex: String = "Male",
    var gymFrequency: String = "3-4 times/week",
    var limitations: String = "None",
    var fitnessLevel: String = "Intermediate",
    var hasCompletedQuestionnaire: Boolean = false
) {
    fun getTotalMaxHealth(): Int = 100 + (level * 10) + (equippedArmor?.healthBonus ?: 0)
    fun getTotalStrength(): Int = strength + (equippedWeapon?.strengthBonus ?: 0) + (level * 2)
    fun getTotalDefense(): Int = defense + (equippedArmor?.defenseBonus ?: 0) + level

    fun experienceForNextLevel(): Int = level * 100

    fun addExperience(amount: Int) {
        experience += amount
        while (experience >= experienceForNextLevel()) {
            experience -= experienceForNextLevel()
            levelUp()
        }
    }

    private fun levelUp() {
        level++
        maxHealth += 10
        health = maxHealth
        strength += 2; agility += 2; intelligence += 2; defense += 2
    }

    fun getBMI(): Double {
        val totalInches = (heightFeet * 12) + heightInches
        if (totalInches <= 0) return 0.0
        return (weightLbs * 703.0) / (totalInches * totalInches)
    }

    fun getIntensityMultiplier(): Double {
        var m = when (gymFrequency) {
            "Never" -> 0.5
            "1-2 times/week" -> 0.7
            "3-4 times/week" -> 1.0
            "5-6 times/week" -> 1.2
            "Daily" -> 1.4
            else -> 1.0
        }
        if (limitations != "None" && limitations.isNotEmpty()) m *= 0.6
        val bmi = getBMI()
        if (bmi > 35) m *= 0.7 else if (bmi > 30) m *= 0.8 else if (bmi < 18.5) m *= 0.8
        return m.coerceIn(0.4, 1.5)
    }

    fun save(context: Context) {
        context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
            .edit().putString("player", Gson().toJson(this)).apply()
    }

    companion object {
        fun load(context: Context): Player {
            val json = context.getSharedPreferences("player_data", Context.MODE_PRIVATE)
                .getString("player", null)
            if (json != null) {
                try {
                    return Gson().fromJson(json, Player::class.java) ?: Player()
                } catch (e: Exception) {
                    android.util.Log.e("Player", "Error loading player from JSON", e)
                    return Player()
                }
            }
            return Player()
        }
    }
}

enum class CharacterClass(val displayName: String, val emoji: String, val description: String) {
    WARRIOR("Warrior", "⚔️", "A brave fighter with high strength and defense"),
    MAGE("Mage", "🔮", "A mystical spellcaster with powerful magic"),
    NINJA("Ninja", "🥷", "A swift assassin with high agility and critical hits"),
    PALADIN("Paladin", "🛡️", "A holy knight with healing abilities"),
    RANGER("Ranger", "🏹", "A skilled archer with ranged attacks"),
    MONK("Monk", "👊", "A martial artist with chi-powered strikes")
}

data class Equipment(
    val id: String,
    val name: String,
    val type: EquipmentType,
    val rarity: Rarity,
    val strengthBonus: Int = 0,
    val defenseBonus: Int = 0,
    val healthBonus: Int = 0,
    val agilityBonus: Int = 0,
    val intelligenceBonus: Int = 0,
    val price: Int,
    val levelRequired: Int = 1,
    val description: String = "",
    val emoji: String = "🗡️"
)

enum class EquipmentType { WEAPON, ARMOR, ACCESSORY, POTION, SHIELD }

enum class Rarity(val displayName: String, val color: Int) {
    COMMON("Common", 0xFF808080.toInt()),
    UNCOMMON("Uncommon", 0xFF2E7D32.toInt()),
    RARE("Rare", 0xFF1976D2.toInt()),
    EPIC("Epic", 0xFF7B1FA2.toInt()),
    LEGENDARY("Legendary", 0xFFFF8F00.toInt())
}

data class InventoryItem(
    val id: String, val name: String, val type: ItemType,
    val effect: Int, var quantity: Int = 1, val price: Int = 0,
    val emoji: String = "🧪", val description: String = ""
)

enum class ItemType { HEALTH_POTION, MANA_POTION, BUFF, ATTACK_ITEM }
typealias Item = InventoryItem

// shop catalog
object ShopCatalog {
    fun getPotions(): List<InventoryItem> = listOf(
        InventoryItem("small_potion", "Small Potion", ItemType.HEALTH_POTION, 50, price = 30, emoji = "🧪", description = "Restores 50 HP in battle"),
        InventoryItem("medium_potion", "Medium Potion", ItemType.HEALTH_POTION, 150, price = 80, emoji = "🧪", description = "Restores 150 HP in battle"),
        InventoryItem("large_potion", "Large Potion", ItemType.HEALTH_POTION, 400, price = 200, emoji = "🧴", description = "Restores 400 HP in battle"),
        InventoryItem("mega_potion", "Mega Potion", ItemType.HEALTH_POTION, 999, price = 500, emoji = "💎", description = "Full heal! Restores 999 HP"),
        InventoryItem("small_ether", "Small Ether", ItemType.MANA_POTION, 30, price = 40, emoji = "💧", description = "Restores 30 MP in battle"),
        InventoryItem("large_ether", "Large Ether", ItemType.MANA_POTION, 80, price = 120, emoji = "💙", description = "Restores 80 MP in battle"),
        InventoryItem("fire_bomb", "Fire Bomb", ItemType.ATTACK_ITEM, 100, price = 60, emoji = "💣", description = "Deals 100 fire damage"),
        InventoryItem("ice_bomb", "Ice Bomb", ItemType.ATTACK_ITEM, 100, price = 60, emoji = "🧊", description = "Deals 100 ice damage"),
        InventoryItem("strength_elixir", "Strength Elixir", ItemType.BUFF, 50, price = 150, emoji = "💪", description = "+50% ATK for 3 turns")
    )

    fun getWeapons(): List<Equipment> = listOf(
        // weapon list
        Equipment("wooden_db", "Wooden Dumbbells", EquipmentType.WEAPON, Rarity.COMMON, strengthBonus = 3, price = 100, description = "Light training weights", emoji = "🏋️"),
        Equipment("iron_db", "Iron Dumbbells", EquipmentType.WEAPON, Rarity.UNCOMMON, strengthBonus = 7, price = 300, levelRequired = 3, description = "Solid iron free weights", emoji = "🏋️"),
        Equipment("steel_barbell", "Steel Barbell", EquipmentType.WEAPON, Rarity.RARE, strengthBonus = 12, price = 600, levelRequired = 5, description = "A heavy steel barbell for serious lifts", emoji = "🏋️‍♂️"),
        Equipment("enchanted_kb", "Enchanted Kettlebell", EquipmentType.WEAPON, Rarity.RARE, strengthBonus = 15, agilityBonus = 5, price = 900, levelRequired = 8, description = "A glowing kettlebell of power", emoji = "🔮"),
        Equipment("titan_plates", "Titan Plates", EquipmentType.WEAPON, Rarity.EPIC, strengthBonus = 22, price = 1500, levelRequired = 12, description = "Legendary weight plates forged in fire", emoji = "🔥"),
        Equipment("dragon_db", "Dragon Dumbbells", EquipmentType.WEAPON, Rarity.EPIC, strengthBonus = 30, price = 2500, levelRequired = 15, description = "Free weights infused with dragon fire", emoji = "🐉"),
        Equipment("excalibar", "Excali-Bar", EquipmentType.WEAPON, Rarity.LEGENDARY, strengthBonus = 45, agilityBonus = 10, price = 5000, levelRequired = 20, description = "THE legendary barbell of kings", emoji = "⚔️")
    )

    fun getShields(): List<Equipment> = listOf(
        Equipment("wooden_shield", "Wooden Buckler", EquipmentType.ARMOR, Rarity.COMMON, defenseBonus = 3, healthBonus = 10, price = 80, description = "Basic wooden shield", emoji = "🪵"),
        Equipment("iron_guard", "Iron Guard", EquipmentType.ARMOR, Rarity.UNCOMMON, defenseBonus = 6, healthBonus = 25, price = 250, levelRequired = 3, description = "Sturdy iron armor", emoji = "🛡️"),
        Equipment("steel_plate", "Steel Plate", EquipmentType.ARMOR, Rarity.RARE, defenseBonus = 10, healthBonus = 50, price = 500, levelRequired = 5, description = "Heavy steel plate armor", emoji = "🛡️"),
        Equipment("mithril_vest", "Mithril Vest", EquipmentType.ARMOR, Rarity.RARE, defenseBonus = 14, healthBonus = 75, agilityBonus = 3, price = 850, levelRequired = 8, description = "Light but incredibly strong", emoji = "✨"),
        Equipment("titan_armor", "Titan Armor", EquipmentType.ARMOR, Rarity.EPIC, defenseBonus = 20, healthBonus = 120, price = 1400, levelRequired = 12, description = "Armor forged from titan bones", emoji = "💀"),
        Equipment("dragon_scale", "Dragon Scale Mail", EquipmentType.ARMOR, Rarity.EPIC, defenseBonus = 28, healthBonus = 150, price = 2200, levelRequired = 15, description = "Scales of an ancient dragon", emoji = "🐉"),
        Equipment("immortal_aegis", "Immortal Aegis", EquipmentType.ARMOR, Rarity.LEGENDARY, defenseBonus = 40, healthBonus = 250, price = 4500, levelRequired = 20, description = "Grants near-invincibility", emoji = "👑")
    )
}
