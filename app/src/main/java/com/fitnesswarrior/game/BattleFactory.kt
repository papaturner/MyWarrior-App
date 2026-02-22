package com.fitnesswarrior.game

import com.fitnesswarrior.models.CharacterClass
import com.fitnesswarrior.models.Player

// battle factory helper
object BattleFactory {
    
    fun createPlayerParticipant(player: Player): BattleParticipant {
        val skills = ClassSkills.getSkillsForClass(player.characterClass, player.level)
        val basicAttack = ClassSkills.getBasicAttack(player.characterClass)
        val element = getClassElement(player.characterClass)
        val stats = calculatePlayerStats(player)
        
        return BattleParticipant(
            name = player.name,
            level = player.level,
            currentHp = player.health,
            maxHp = player.maxHealth,
            currentMp = calculateMaxMp(player),
            maxMp = calculateMaxMp(player),
            stats = stats,
            element = element,
            skills = skills,
            basicAttack = basicAttack,
            items = createDefaultItems()
        )
    }
    
    private fun calculatePlayerStats(player: Player): BattleStats {
        val classModifiers = getClassStatModifiers(player.characterClass)
        
        return BattleStats(
            attack = (player.strength * classModifiers.attackMod).toInt() + (player.equippedWeapon?.strengthBonus ?: 0),
            defense = (player.defense * classModifiers.defenseMod).toInt() + (player.equippedArmor?.defenseBonus ?: 0),
            magicAttack = (player.intelligence * classModifiers.magicMod).toInt(),
            magicDefense = (player.intelligence * 0.8).toInt() + (player.equippedArmor?.defenseBonus ?: 0) / 2,
            speed = (player.agility * classModifiers.speedMod).toInt(),
            luck = 5 + player.level
        )
    }
    
    private fun calculateMaxMp(player: Player): Int {
        val baseMp = when (player.characterClass) {
            CharacterClass.MAGE -> 100 + player.level * 8
            CharacterClass.PALADIN -> 80 + player.level * 6
            CharacterClass.NINJA -> 60 + player.level * 5
            CharacterClass.RANGER -> 60 + player.level * 5
            CharacterClass.MONK -> 70 + player.level * 5
            CharacterClass.WARRIOR -> 50 + player.level * 4
        }
        return baseMp
    }
    
    private fun getClassElement(characterClass: CharacterClass): Element {
        return when (characterClass) {
            CharacterClass.WARRIOR -> Element.NEUTRAL
            CharacterClass.MAGE -> Element.FIRE
            CharacterClass.NINJA -> Element.DARK
            CharacterClass.PALADIN -> Element.LIGHT
            CharacterClass.RANGER -> Element.EARTH
            CharacterClass.MONK -> Element.NEUTRAL
        }
    }
    
    private fun getClassStatModifiers(characterClass: CharacterClass): ClassStatMods {
        return when (characterClass) {
            CharacterClass.WARRIOR -> ClassStatMods(1.2, 1.2, 0.7, 0.9)
            CharacterClass.MAGE -> ClassStatMods(0.7, 0.8, 1.5, 0.9)
            CharacterClass.NINJA -> ClassStatMods(1.0, 0.8, 0.9, 1.4)
            CharacterClass.PALADIN -> ClassStatMods(1.0, 1.3, 1.1, 0.8)
            CharacterClass.RANGER -> ClassStatMods(1.1, 0.9, 0.8, 1.2)
            CharacterClass.MONK -> ClassStatMods(1.3, 1.0, 0.8, 1.1)
        }
    }
    
    private fun createDefaultItems(): MutableList<BattleItem> {
        return mutableListOf(
            BattleItem("potion", "Potion", BattleItemType.HEALING, 50, 3),
            BattleItem("hi_potion", "Hi-Potion", BattleItemType.HEALING, 150, 1),
            BattleItem("ether", "Ether", BattleItemType.MP_RESTORE, 30, 2),
            BattleItem("antidote", "Antidote", BattleItemType.STATUS_CURE, 0, 2)
        )
    }
    
    // enemy creation
    
    fun createEnemy(enemyId: String, levelScaling: Int = 0): BattleParticipant {
        val enemyData = EnemyDatabase.getEnemy(enemyId)
        val scaledLevel = enemyData.baseLevel + levelScaling
        
        return BattleParticipant(
            name = enemyData.name,
            level = scaledLevel,
            currentHp = enemyData.baseHp + (scaledLevel * 10),
            maxHp = enemyData.baseHp + (scaledLevel * 10),
            currentMp = enemyData.baseMp,
            maxMp = enemyData.baseMp,
            stats = BattleStats(
                attack = enemyData.baseAttack + (scaledLevel * 2),
                defense = enemyData.baseDefense + scaledLevel,
                magicAttack = enemyData.baseMagicAttack + (scaledLevel * 2),
                magicDefense = enemyData.baseMagicDefense + scaledLevel,
                speed = enemyData.baseSpeed + scaledLevel,
                luck = 3 + scaledLevel / 2
            ),
            element = enemyData.element,
            skills = enemyData.skills,
            basicAttack = enemyData.basicAttack,
            isBoss = enemyData.isBoss,
            xpReward = enemyData.xpReward + (scaledLevel * 5),
            goldReward = enemyData.goldReward + (scaledLevel * 3)
        )
    }
}

data class ClassStatMods(
    val attackMod: Double,
    val defenseMod: Double,
    val magicMod: Double,
    val speedMod: Double
)

// enemy database
object EnemyDatabase {
    
    fun getEnemy(id: String): EnemyData {
        return enemies[id] ?: enemies["slime"]!!
    }
    
    fun getEnemiesForWorld(worldId: Int): List<String> {
        return when (worldId) {
            1 -> listOf("slime", "goblin", "skeleton", "skeleton_boss")
            2 -> listOf("wolf", "orc", "troll", "troll_boss")
            3 -> listOf("bandit", "golem", "dark_knight", "dark_knight_boss")
            4 -> listOf("dragon_young", "demon", "sloth_king")
            else -> listOf("ancient_dragon")
        }
    }
    
    private val enemies = mapOf(

        // world 1 enemies
       // sprites placeholder
        "slime" to EnemyData(
            id = "slime",
            name = "Green Slime",
            baseLevel = 1,
            baseHp = 40,
            baseMp = 10,
            baseAttack = 8,
            baseDefense = 3,
            baseMagicAttack = 5,
            baseMagicDefense = 2,
            baseSpeed = 5,
            element = Element.WATER,
            xpReward = 15,
            goldReward = 8,
            basicAttack = Skill("slime_tackle", "Tackle", "Slimy charge", SkillType.DAMAGE, Element.WATER, 30, 0, 90, true),
            skills = listOf(
                Skill("acid_spit", "Acid Spit", "Corrosive attack", SkillType.DAMAGE, Element.WATER, 35, 5, 85, false,
                    statusEffect = StatusEffect(StatusType.POISON, 3), statusChance = 20)
            )
        ),
        
        "goblin" to EnemyData(
            id = "goblin",
            name = "Goblin Scout",
            baseLevel = 2,
            baseHp = 55,
            baseMp = 15,
            baseAttack = 12,
            baseDefense = 5,
            baseMagicAttack = 4,
            baseMagicDefense = 3,
            baseSpeed = 10,
            element = Element.NEUTRAL,
            xpReward = 25,
            goldReward = 15,
            basicAttack = Skill("goblin_stab", "Stab", "Quick dagger stab", SkillType.DAMAGE, Element.NEUTRAL, 35, 0, 95, true),
            skills = listOf(
                Skill("goblin_rage", "Goblin Rage", "Frenzied attack", SkillType.DAMAGE, Element.NEUTRAL, 50, 8, 80, true)
            )
        ),
        
        "skeleton" to EnemyData(
            id = "skeleton",
            name = "Skeleton Soldier",
            baseLevel = 3,
            baseHp = 70,
            baseMp = 20,
            baseAttack = 15,
            baseDefense = 10,
            baseMagicAttack = 8,
            baseMagicDefense = 5,
            baseSpeed = 7,
            element = Element.DARK,
            xpReward = 40,
            goldReward = 25,
            basicAttack = Skill("bone_slash", "Bone Slash", "Sword slash", SkillType.DAMAGE, Element.DARK, 40, 0, 90, true),
            skills = listOf(
                Skill("bone_throw", "Bone Throw", "Throw bone projectile", SkillType.DAMAGE, Element.DARK, 45, 8, 85, true)
            )
        ),
        
        "skeleton_boss" to EnemyData(
            id = "skeleton_boss",
            name = "Skeleton King",
            baseLevel = 5,
            baseHp = 200,
            baseMp = 50,
            baseAttack = 22,
            baseDefense = 15,
            baseMagicAttack = 18,
            baseMagicDefense = 12,
            baseSpeed = 10,
            element = Element.DARK,
            isBoss = true,
            xpReward = 150,
            goldReward = 100,
            basicAttack = Skill("royal_slash", "Royal Slash", "Powerful sword strike", SkillType.DAMAGE, Element.DARK, 55, 0, 90, true),
            skills = listOf(
                Skill("dark_wave", "Dark Wave", "Wave of dark energy", SkillType.DAMAGE, Element.DARK, 70, 15, 90, false),
                Skill("summon_bones", "Summon Bones", "Rain of bones", SkillType.DAMAGE, Element.DARK, 50, 12, 80, true),
                Skill("curse", "Curse", "Inflict curse", SkillType.STATUS, Element.DARK, 0, 10, 70,
                    statusEffect = StatusEffect(StatusType.POISON, 4), statusChance = 100)
            )
        ),
        

        // world 2 enemies
// enemy sprites placeholder
        "wolf" to EnemyData(
            id = "wolf",
            name = "Dire Wolf",
            baseLevel = 6,
            baseHp = 90,
            baseMp = 20,
            baseAttack = 22,
            baseDefense = 10,
            baseMagicAttack = 8,
            baseMagicDefense = 8,
            baseSpeed = 18,
            element = Element.DARK,
            xpReward = 50,
            goldReward = 30,
            basicAttack = Skill("wolf_bite", "Bite", "Vicious bite attack", SkillType.DAMAGE, Element.DARK, 45, 0, 95, true,
                statusEffect = StatusEffect(StatusType.BLEED, 2), statusChance = 15),
            skills = listOf(
                Skill("howl", "Howl", "Intimidating howl", SkillType.BUFF, Element.DARK, 0, 10,
                    buff = StatModifier("ATK Up", ModifiableStat.ATTACK, 1.3, 3))
            )
        ),
        
        "orc" to EnemyData(
            id = "orc",
            name = "Orc Brute",
            baseLevel = 7,
            baseHp = 130,
            baseMp = 25,
            baseAttack = 28,
            baseDefense = 15,
            baseMagicAttack = 5,
            baseMagicDefense = 8,
            baseSpeed = 8,
            element = Element.EARTH,
            xpReward = 65,
            goldReward = 40,
            basicAttack = Skill("orc_smash", "Smash", "Brutal club smash", SkillType.DAMAGE, Element.EARTH, 55, 0, 85, true),
            skills = listOf(
                Skill("war_stomp", "War Stomp", "Ground-shaking stomp", SkillType.DAMAGE, Element.EARTH, 60, 12, 80, true,
                    statusEffect = StatusEffect(StatusType.PARALYSIS, 1), statusChance = 25)
            )
        ),
        
        "troll" to EnemyData(
            id = "troll",
            name = "Forest Troll",
            baseLevel = 8,
            baseHp = 160,
            baseMp = 30,
            baseAttack = 25,
            baseDefense = 18,
            baseMagicAttack = 10,
            baseMagicDefense = 12,
            baseSpeed = 6,
            element = Element.EARTH,
            xpReward = 80,
            goldReward = 55,
            basicAttack = Skill("troll_claw", "Claw", "Massive claw swipe", SkillType.DAMAGE, Element.EARTH, 50, 0, 85, true),
            skills = listOf(
                Skill("regenerate", "Regenerate", "Trolls heal naturally", SkillType.HEAL, Element.EARTH, 40, 10)
            )
        ),
        
        "troll_boss" to EnemyData(
            id = "troll_boss",
            name = "Troll Warlord",
            baseLevel = 10,
            baseHp = 350,
            baseMp = 60,
            baseAttack = 35,
            baseDefense = 25,
            baseMagicAttack = 15,
            baseMagicDefense = 18,
            baseSpeed = 8,
            element = Element.EARTH,
            isBoss = true,
            xpReward = 250,
            goldReward = 180,
            basicAttack = Skill("warlord_slam", "Warlord Slam", "Devastating slam", SkillType.DAMAGE, Element.EARTH, 70, 0, 85, true),
            skills = listOf(
                Skill("earthquake", "Earthquake", "Massive ground attack", SkillType.DAMAGE, Element.EARTH, 85, 20, 80, true),
                Skill("battle_roar", "Battle Roar", "Boost all stats", SkillType.BUFF, Element.NEUTRAL, 0, 15,
                    buff = StatModifier("All Up", ModifiableStat.ATTACK, 1.4, 3)),
                Skill("mega_regen", "Mega Regenerate", "Powerful healing", SkillType.HEAL, Element.EARTH, 100, 25)
            )
        ),
        
        // world 3 enemies
        "bandit" to EnemyData(
            id = "bandit",
            name = "Bridge Bandit",
            baseLevel = 10,
            baseHp = 140,
            baseMp = 35,
            baseAttack = 32,
            baseDefense = 18,
            baseMagicAttack = 12,
            baseMagicDefense = 15,
            baseSpeed = 16,
            element = Element.NEUTRAL,
            xpReward = 90,
            goldReward = 70,
            basicAttack = Skill("bandit_slash", "Slash", "Quick sword slash", SkillType.DAMAGE, Element.NEUTRAL, 55, 0, 95, true, highCritRate = true),
            skills = listOf(
                Skill("steal_gold", "Steal", "Attempt to steal gold", SkillType.DAMAGE, Element.NEUTRAL, 35, 8, 90, true),
                Skill("dirty_trick", "Dirty Trick", "Blind enemy", SkillType.DEBUFF, Element.DARK, 0, 12,
                    debuff = StatModifier("Blinded", ModifiableStat.SPEED, 0.6, 3))
            )
        ),
        
        "golem" to EnemyData(
            id = "golem",
            name = "Stone Golem",
            baseLevel = 11,
            baseHp = 220,
            baseMp = 20,
            baseAttack = 30,
            baseDefense = 35,
            baseMagicAttack = 15,
            baseMagicDefense = 25,
            baseSpeed = 4,
            element = Element.EARTH,
            xpReward = 110,
            goldReward = 80,
            basicAttack = Skill("golem_punch", "Stone Punch", "Heavy stone fist", SkillType.DAMAGE, Element.EARTH, 65, 0, 80, true),
            skills = listOf(
                Skill("rock_throw", "Rock Throw", "Throw boulder", SkillType.DAMAGE, Element.EARTH, 70, 10, 75, true)
            )
        ),
        
        "dark_knight" to EnemyData(
            id = "dark_knight",
            name = "Dark Knight",
            baseLevel = 12,
            baseHp = 180,
            baseMp = 45,
            baseAttack = 38,
            baseDefense = 28,
            baseMagicAttack = 25,
            baseMagicDefense = 22,
            baseSpeed = 12,
            element = Element.DARK,
            xpReward = 130,
            goldReward = 100,
            basicAttack = Skill("dark_slash", "Dark Slash", "Darkness-infused slash", SkillType.DAMAGE, Element.DARK, 60, 0, 90, true),
            skills = listOf(
                Skill("shadow_blade", "Shadow Blade", "Dark magic sword", SkillType.DAMAGE, Element.DARK, 75, 15, 90, false)
            )
        ),
        
        "dark_knight_boss" to EnemyData(
            id = "dark_knight_boss",
            name = "Knight Commander",
            baseLevel = 15,
            baseHp = 500,
            baseMp = 80,
            baseAttack = 48,
            baseDefense = 35,
            baseMagicAttack = 35,
            baseMagicDefense = 30,
            baseSpeed = 15,
            element = Element.DARK,
            isBoss = true,
            xpReward = 400,
            goldReward = 300,
            basicAttack = Skill("commander_strike", "Commander Strike", "Masterful sword strike", SkillType.DAMAGE, Element.DARK, 75, 0, 95, true),
            skills = listOf(
                Skill("darkness_wave", "Darkness Wave", "Wave of pure darkness", SkillType.DAMAGE, Element.DARK, 95, 20, 90, false),
                Skill("soul_drain", "Soul Drain", "Drain life force", SkillType.DAMAGE, Element.DARK, 70, 18, 85, false),
                Skill("dark_armor", "Dark Armor", "Boost defense with dark magic", SkillType.BUFF, Element.DARK, 0, 20,
                    buff = StatModifier("Dark Shield", ModifiableStat.DEFENSE, 1.6, 3)),
                Skill("execute", "Execute", "Devastating finisher", SkillType.DAMAGE, Element.DARK, 120, 30, 75, true, highCritRate = true)
            )
        ),
        
        // world 4 enemies
        "dragon_young" to EnemyData(
            id = "dragon_young",
            name = "Young Dragon",
            baseLevel = 15,
            baseHp = 280,
            baseMp = 60,
            baseAttack = 42,
            baseDefense = 30,
            baseMagicAttack = 45,
            baseMagicDefense = 35,
            baseSpeed = 14,
            element = Element.FIRE,
            xpReward = 200,
            goldReward = 150,
            basicAttack = Skill("dragon_claw", "Dragon Claw", "Sharp claw attack", SkillType.DAMAGE, Element.FIRE, 70, 0, 90, true),
            skills = listOf(
                Skill("fire_breath", "Fire Breath", "Cone of fire", SkillType.DAMAGE, Element.FIRE, 85, 18, 90, false,
                    statusEffect = StatusEffect(StatusType.BURN, 3), statusChance = 35)
            )
        ),
        
        "demon" to EnemyData(
            id = "demon",
            name = "Lesser Demon",
            baseLevel = 16,
            baseHp = 250,
            baseMp = 80,
            baseAttack = 40,
            baseDefense = 25,
            baseMagicAttack = 55,
            baseMagicDefense = 40,
            baseSpeed = 16,
            element = Element.DARK,
            xpReward = 220,
            goldReward = 170,
            basicAttack = Skill("demon_slash", "Demon Slash", "Unholy claw attack", SkillType.DAMAGE, Element.DARK, 65, 0, 90, true),
            skills = listOf(
                Skill("hellfire", "Hellfire", "Demonic flames", SkillType.DAMAGE, Element.FIRE, 90, 20, 90, false,
                    statusEffect = StatusEffect(StatusType.BURN, 3), statusChance = 40),
                Skill("curse_spell", "Curse", "Inflict multiple ailments", SkillType.STATUS, Element.DARK, 0, 15, 70,
                    statusEffect = StatusEffect(StatusType.POISON, 4), statusChance = 100)
            )
        ),
        
        "sloth_king" to EnemyData(
            id = "sloth_king",
            name = "SLOTH KING",
            baseLevel = 20,
            baseHp = 1500,
            baseMp = 150,
            baseAttack = 55,
            baseDefense = 40,
            baseMagicAttack = 60,
            baseMagicDefense = 45,
            baseSpeed = 5,
            element = Element.EARTH,
            isBoss = true,
            xpReward = 800,
            goldReward = 600,
            basicAttack = Skill("king_slam", "Royal Slam", "Crushing slam attack", SkillType.DAMAGE, Element.EARTH, 85, 0, 85, true),
            skills = listOf(
                Skill("lazy_yawn", "Lazy Yawn", "Put enemies to sleep", SkillType.STATUS, Element.DARK, 0, 20, 60,
                    statusEffect = StatusEffect(StatusType.SLEEP, 3), statusChance = 100),
                Skill("nature_wrath", "Nature's Wrath", "Powerful earth magic", SkillType.DAMAGE, Element.EARTH, 110, 25, 90, false),
                Skill("mega_regen", "Sloth Regeneration", "Massive HP recovery", SkillType.HEAL, Element.EARTH, 200, 30),
                Skill("earthquake", "Cataclysm", "Devastating quake", SkillType.DAMAGE, Element.EARTH, 130, 40, 85, true,
                    statusEffect = StatusEffect(StatusType.PARALYSIS, 2), statusChance = 30),
                Skill("kings_decree", "King's Decree", "Buff all stats", SkillType.BUFF, Element.NEUTRAL, 0, 25,
                    buff = StatModifier("Royal Power", ModifiableStat.ATTACK, 1.5, 4))
            )
        ),
        
        "ancient_dragon" to EnemyData(
            id = "ancient_dragon",
            name = "Ancient Dragon",
            baseLevel = 25,
            baseHp = 2500,
            baseMp = 200,
            baseAttack = 70,
            baseDefense = 50,
            baseMagicAttack = 80,
            baseMagicDefense = 55,
            baseSpeed = 18,
            element = Element.FIRE,
            isBoss = true,
            xpReward = 1500,
            goldReward = 1000,
            basicAttack = Skill("ancient_claw", "Ancient Claw", "Devastating claw strike", SkillType.DAMAGE, Element.FIRE, 100, 0, 90, true),
            skills = listOf(
                Skill("inferno", "Inferno", "All-consuming flames", SkillType.DAMAGE, Element.FIRE, 140, 35, 90, false,
                    statusEffect = StatusEffect(StatusType.BURN, 4), statusChance = 50),
                Skill("dragon_roar", "Dragon Roar", "Terrifying roar", SkillType.DEBUFF, Element.DARK, 0, 20,
                    debuff = StatModifier("Fear", ModifiableStat.ATTACK, 0.6, 3)),
                Skill("wing_attack", "Wing Buffet", "Powerful wind attack", SkillType.DAMAGE, Element.WIND, 100, 25, 85, true),
                Skill("meteor_breath", "Meteor Breath", "Ultimate dragon attack", SkillType.DAMAGE, Element.FIRE, 180, 50, 80, false)
            )
        )
    )
}

data class EnemyData(
    val id: String,
    val name: String,
    val baseLevel: Int,
    val baseHp: Int,
    val baseMp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseMagicAttack: Int,
    val baseMagicDefense: Int,
    val baseSpeed: Int,
    val element: Element,
    val xpReward: Int,
    val goldReward: Int,
    val basicAttack: Skill,
    val skills: List<Skill>,
    val isBoss: Boolean = false
)
