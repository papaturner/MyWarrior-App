package com.fitnesswarrior.game

import com.fitnesswarrior.models.CharacterClass

//class skill definitions
object ClassSkills {
    
    fun getSkillsForClass(characterClass: CharacterClass, level: Int): List<Skill> {
        val baseSkills = getBaseSkills(characterClass)
        val unlockedSkills = baseSkills.filter { it.levelRequired <= level }
        return unlockedSkills.map { it.skill }
    }
    
    fun getBasicAttack(characterClass: CharacterClass): Skill {
        return when (characterClass) {
            CharacterClass.WARRIOR -> Skill(
                id = "warrior_slash",
                name = "Slash",
                description = "A powerful sword strike",
                type = SkillType.DAMAGE,
                element = Element.NEUTRAL,
                power = 40,
                mpCost = 0,
                accuracy = 95,
                isPhysical = true
            )
            CharacterClass.MAGE -> Skill(
                id = "mage_staff",
                name = "Staff Strike",
                description = "A weak physical attack with staff",
                type = SkillType.DAMAGE,
                element = Element.NEUTRAL,
                power = 25,
                mpCost = 0,
                accuracy = 90,
                isPhysical = true
            )
            CharacterClass.NINJA -> Skill(
                id = "ninja_strike",
                name = "Swift Strike",
                description = "A quick dagger slash",
                type = SkillType.DAMAGE,
                element = Element.NEUTRAL,
                power = 35,
                mpCost = 0,
                accuracy = 100,
                isPhysical = true,
                highCritRate = true
            )
            CharacterClass.PALADIN -> Skill(
                id = "paladin_smite",
                name = "Holy Smite",
                description = "A righteous sword attack",
                type = SkillType.DAMAGE,
                element = Element.LIGHT,
                power = 38,
                mpCost = 0,
                accuracy = 95,
                isPhysical = true
            )
            CharacterClass.RANGER -> Skill(
                id = "ranger_shot",
                name = "Arrow Shot",
                description = "A precise arrow attack",
                type = SkillType.DAMAGE,
                element = Element.NEUTRAL,
                power = 35,
                mpCost = 0,
                accuracy = 98,
                isPhysical = true
            )
            CharacterClass.MONK -> Skill(
                id = "monk_punch",
                name = "Chi Punch",
                description = "A powerful martial arts strike",
                type = SkillType.DAMAGE,
                element = Element.NEUTRAL,
                power = 42,
                mpCost = 0,
                accuracy = 92,
                isPhysical = true
            )
        }
    }
    
    private fun getBaseSkills(characterClass: CharacterClass): List<LevelSkill> {
        return when (characterClass) {
            CharacterClass.WARRIOR -> getWarriorSkills()
            CharacterClass.MAGE -> getMageSkills()
            CharacterClass.NINJA -> getNinjaSkills()
            CharacterClass.PALADIN -> getPaladinSkills()
            CharacterClass.RANGER -> getRangerSkills()
            CharacterClass.MONK -> getMonkSkills()
        }
    }
    
    // the warrior skills
    private fun getWarriorSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "power_strike",
            name = "Power Strike",
            description = "A devastating overhead swing",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 60,
            mpCost = 5,
            accuracy = 90,
            isPhysical = true
        )),
        LevelSkill(3, Skill(
            id = "war_cry",
            name = "War Cry",
            description = "Boost attack power for 3 turns",
            type = SkillType.BUFF,
            element = Element.NEUTRAL,
            power = 0,
            mpCost = 8,
            buff = StatModifier("ATK Up", ModifiableStat.ATTACK, 1.5, 3)
        )),
        LevelSkill(5, Skill(
            id = "shield_bash",
            name = "Shield Bash",
            description = "Stun enemy with shield",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 45,
            mpCost = 10,
            accuracy = 85,
            isPhysical = true,
            statusEffect = StatusEffect(StatusType.PARALYSIS, 2),
            statusChance = 30
        )),
        LevelSkill(8, Skill(
            id = "berserk",
            name = "Berserk",
            description = "Massively boost attack but lower defense",
            type = SkillType.BUFF,
            element = Element.NEUTRAL,
            power = 0,
            mpCost = 15,
            buff = StatModifier("Berserk ATK", ModifiableStat.ATTACK, 2.0, 3),
            debuff = StatModifier("Berserk DEF", ModifiableStat.DEFENSE, 0.5, 3)
        )),
        LevelSkill(10, Skill(
            id = "whirlwind",
            name = "Whirlwind Slash",
            description = "Spin attack hitting all enemies",
            type = SkillType.DAMAGE,
            element = Element.WIND,
            power = 55,
            mpCost = 15,
            accuracy = 85,
            isPhysical = true
        )),
        LevelSkill(15, Skill(
            id = "executioner",
            name = "Executioner",
            description = "Massive damage, higher crit on low HP enemies",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 90,
            mpCost = 25,
            accuracy = 80,
            isPhysical = true,
            highCritRate = true
        )),
        LevelSkill(20, Skill(
            id = "blade_storm",
            name = "Blade Storm",
            description = "Ultimate warrior technique",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 120,
            mpCost = 40,
            accuracy = 75,
            isPhysical = true
        ))
    )
    
    //mage skills
    private fun getMageSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "fire",
            name = "Fire",
            description = "Basic fire magic",
            type = SkillType.DAMAGE,
            element = Element.FIRE,
            power = 50,
            mpCost = 6,
            accuracy = 100,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.BURN, 3),
            statusChance = 10
        )),
        LevelSkill(1, Skill(
            id = "ice",
            name = "Blizzard",
            description = "Basic ice magic",
            type = SkillType.DAMAGE,
            element = Element.ICE,
            power = 50,
            mpCost = 6,
            accuracy = 100,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.FREEZE, 2),
            statusChance = 10
        )),
        LevelSkill(3, Skill(
            id = "thunder",
            name = "Thunder",
            description = "Lightning strike from above",
            type = SkillType.DAMAGE,
            element = Element.LIGHTNING,
            power = 55,
            mpCost = 8,
            accuracy = 95,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.PARALYSIS, 2),
            statusChance = 15
        )),
        LevelSkill(5, Skill(
            id = "drain",
            name = "Drain",
            description = "Absorb HP from enemy",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 40,
            mpCost = 12,
            accuracy = 100,
            isPhysical = false
        )),
        LevelSkill(8, Skill(
            id = "fira",
            name = "Fira",
            description = "Stronger fire magic",
            type = SkillType.DAMAGE,
            element = Element.FIRE,
            power = 80,
            mpCost = 15,
            accuracy = 100,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.BURN, 3),
            statusChance = 20
        )),
        LevelSkill(10, Skill(
            id = "sleep",
            name = "Sleep",
            description = "Put enemy to sleep",
            type = SkillType.STATUS,
            element = Element.DARK,
            power = 0,
            mpCost = 10,
            accuracy = 70,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.SLEEP, 3),
            statusChance = 100
        )),
        LevelSkill(12, Skill(
            id = "magic_barrier",
            name = "Magic Barrier",
            description = "Boost magic defense",
            type = SkillType.BUFF,
            element = Element.LIGHT,
            power = 0,
            mpCost = 12,
            buff = StatModifier("M.DEF Up", ModifiableStat.MAGIC_DEFENSE, 1.75, 4)
        )),
        LevelSkill(15, Skill(
            id = "firaga",
            name = "Firaga",
            description = "Powerful fire explosion",
            type = SkillType.DAMAGE,
            element = Element.FIRE,
            power = 110,
            mpCost = 28,
            accuracy = 100,
            isPhysical = false,
            statusEffect = StatusEffect(StatusType.BURN, 3),
            statusChance = 30
        )),
        LevelSkill(20, Skill(
            id = "meteor",
            name = "Meteor",
            description = "Ultimate destruction magic",
            type = SkillType.DAMAGE,
            element = Element.FIRE,
            power = 150,
            mpCost = 50,
            accuracy = 90,
            isPhysical = false
        ))
    )
    
    //ninja skills
    private fun getNinjaSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "shadow_strike",
            name = "Shadow Strike",
            description = "Attack from the shadows",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 45,
            mpCost = 5,
            accuracy = 100,
            isPhysical = true,
            highCritRate = true
        )),
        LevelSkill(3, Skill(
            id = "smoke_bomb",
            name = "Smoke Bomb",
            description = "Lower enemy accuracy",
            type = SkillType.DEBUFF,
            element = Element.DARK,
            power = 0,
            mpCost = 8,
            accuracy = 100,
            debuff = StatModifier("Blind", ModifiableStat.SPEED, 0.5, 3)
        )),
        LevelSkill(5, Skill(
            id = "poison_blade",
            name = "Poison Blade",
            description = "Poisoned dagger attack",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 40,
            mpCost = 10,
            accuracy = 95,
            isPhysical = true,
            statusEffect = StatusEffect(StatusType.POISON, 4),
            statusChance = 50
        )),
        LevelSkill(8, Skill(
            id = "shuriken",
            name = "Shuriken",
            description = "Throw multiple shurikens",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 30,
            mpCost = 12,
            accuracy = 90,
            isPhysical = true
        )), // multi hit
        LevelSkill(10, Skill(
            id = "shadow_clone",
            name = "Shadow Clone",
            description = "Create illusion to boost evasion",
            type = SkillType.BUFF,
            element = Element.DARK,
            power = 0,
            mpCost = 15,
            buff = StatModifier("Evasion Up", ModifiableStat.SPEED, 2.0, 3)
        )),
        LevelSkill(13, Skill(
            id = "assassinate",
            name = "Assassinate",
            description = "Instant kill attempt on low HP",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 100,
            mpCost = 25,
            accuracy = 70,
            isPhysical = true,
            highCritRate = true
        )),
        LevelSkill(18, Skill(
            id = "death_blossom",
            name = "Death Blossom",
            description = "Ultimate ninja technique",
            type = SkillType.DAMAGE,
            element = Element.DARK,
            power = 85,
            mpCost = 35,
            accuracy = 95,
            isPhysical = true,
            highCritRate = true,
            statusEffect = StatusEffect(StatusType.BLEED, 3),
            statusChance = 40
        ))
    )
    
    //paladin skills
    private fun getPaladinSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "cure",
            name = "Cure",
            description = "Restore HP",
            type = SkillType.HEAL,
            element = Element.LIGHT,
            power = 50,
            mpCost = 8,
            accuracy = 100
        )),
        LevelSkill(3, Skill(
            id = "holy_strike",
            name = "Holy Strike",
            description = "Light-infused attack",
            type = SkillType.DAMAGE,
            element = Element.LIGHT,
            power = 55,
            mpCost = 8,
            accuracy = 95,
            isPhysical = true
        )),
        LevelSkill(5, Skill(
            id = "protect",
            name = "Protect",
            description = "Increase physical defense",
            type = SkillType.BUFF,
            element = Element.LIGHT,
            power = 0,
            mpCost = 10,
            buff = StatModifier("DEF Up", ModifiableStat.DEFENSE, 1.5, 4)
        )),
        LevelSkill(7, Skill(
            id = "cura",
            name = "Cura",
            description = "Stronger healing",
            type = SkillType.HEAL,
            element = Element.LIGHT,
            power = 100,
            mpCost = 18,
            accuracy = 100
        )),
        LevelSkill(10, Skill(
            id = "banish",
            name = "Banish",
            description = "Holy damage, extra vs undead/dark",
            type = SkillType.DAMAGE,
            element = Element.LIGHT,
            power = 75,
            mpCost = 15,
            accuracy = 100,
            isPhysical = false
        )),
        LevelSkill(12, Skill(
            id = "regen",
            name = "Regen",
            description = "Heal over time",
            type = SkillType.STATUS,
            element = Element.LIGHT,
            power = 0,
            mpCost = 15,
            statusEffect = StatusEffect(StatusType.REGEN, 5),
            statusChance = 100
        )),
        LevelSkill(15, Skill(
            id = "divine_shield",
            name = "Divine Shield",
            description = "Massive defense boost",
            type = SkillType.BUFF,
            element = Element.LIGHT,
            power = 0,
            mpCost = 25,
            buff = StatModifier("Divine Guard", ModifiableStat.DEFENSE, 2.0, 3)
        )),
        LevelSkill(18, Skill(
            id = "curaga",
            name = "Curaga",
            description = "Powerful healing magic",
            type = SkillType.HEAL,
            element = Element.LIGHT,
            power = 180,
            mpCost = 35,
            accuracy = 100
        )),
        LevelSkill(22, Skill(
            id = "holy",
            name = "Holy",
            description = "Ultimate light magic",
            type = SkillType.DAMAGE,
            element = Element.LIGHT,
            power = 130,
            mpCost = 45,
            accuracy = 100,
            isPhysical = false
        ))
    )
    
    //ranger skills
    private fun getRangerSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "aimed_shot",
            name = "Aimed Shot",
            description = "Careful aimed attack",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 50,
            mpCost = 5,
            accuracy = 100,
            isPhysical = true
        )),
        LevelSkill(3, Skill(
            id = "poison_arrow",
            name = "Poison Arrow",
            description = "Arrow tipped with venom",
            type = SkillType.DAMAGE,
            element = Element.EARTH,
            power = 40,
            mpCost = 8,
            accuracy = 95,
            isPhysical = true,
            statusEffect = StatusEffect(StatusType.POISON, 4),
            statusChance = 40
        )),
        LevelSkill(5, Skill(
            id = "hunters_mark",
            name = "Hunter's Mark",
            description = "Lower enemy defense",
            type = SkillType.DEBUFF,
            element = Element.NEUTRAL,
            power = 0,
            mpCost = 10,
            debuff = StatModifier("Marked", ModifiableStat.DEFENSE, 0.7, 4)
        )),
        LevelSkill(8, Skill(
            id = "multishot",
            name = "Multishot",
            description = "Fire multiple arrows",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 35,
            mpCost = 15,
            accuracy = 85,
            isPhysical = true
        )), // multi hit
        LevelSkill(10, Skill(
            id = "nature_heal",
            name = "Nature's Gift",
            description = "Heal using nature energy",
            type = SkillType.HEAL,
            element = Element.EARTH,
            power = 70,
            mpCost = 15
        )),
        LevelSkill(13, Skill(
            id = "ice_arrow",
            name = "Ice Arrow",
            description = "Arrow infused with ice",
            type = SkillType.DAMAGE,
            element = Element.ICE,
            power = 65,
            mpCost = 18,
            accuracy = 90,
            isPhysical = true,
            statusEffect = StatusEffect(StatusType.FREEZE, 2),
            statusChance = 25
        )),
        LevelSkill(16, Skill(
            id = "snipe",
            name = "Snipe",
            description = "Perfect accuracy headshot",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 90,
            mpCost = 22,
            accuracy = 100,
            isPhysical = true,
            highCritRate = true
        )),
        LevelSkill(20, Skill(
            id = "arrow_rain",
            name = "Arrow Rain",
            description = "Ultimate ranger technique",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 100,
            mpCost = 40,
            accuracy = 80,
            isPhysical = true
        ))
    )
    
    //monk skills
    private fun getMonkSkills(): List<LevelSkill> = listOf(
        LevelSkill(1, Skill(
            id = "focus_punch",
            name = "Focus Punch",
            description = "Concentrated strike",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 55,
            mpCost = 5,
            accuracy = 90,
            isPhysical = true
        )),
        LevelSkill(3, Skill(
            id = "inner_peace",
            name = "Inner Peace",
            description = "Restore HP through meditation",
            type = SkillType.HEAL,
            element = Element.LIGHT,
            power = 40,
            mpCost = 8
        )),
        LevelSkill(5, Skill(
            id = "ki_charge",
            name = "Ki Charge",
            description = "Boost attack power",
            type = SkillType.BUFF,
            element = Element.NEUTRAL,
            power = 0,
            mpCost = 10,
            buff = StatModifier("Ki Charged", ModifiableStat.ATTACK, 1.6, 3)
        )),
        LevelSkill(8, Skill(
            id = "roundhouse",
            name = "Roundhouse Kick",
            description = "Spinning kick attack",
            type = SkillType.DAMAGE,
            element = Element.NEUTRAL,
            power = 65,
            mpCost = 12,
            accuracy = 85,
            isPhysical = true,
            statusEffect = StatusEffect(StatusType.PARALYSIS, 1),
            statusChance = 20
        )),
        LevelSkill(10, Skill(
            id = "chakra",
            name = "Chakra",
            description = "Restore HP and cure status",
            type = SkillType.HEAL,
            element = Element.LIGHT,
            power = 60,
            mpCost = 18
        )),
        LevelSkill(13, Skill(
            id = "iron_fist",
            name = "Iron Fist",
            description = "Fist hardened with ki",
            type = SkillType.DAMAGE,
            element = Element.EARTH,
            power = 80,
            mpCost = 18,
            accuracy = 90,
            isPhysical = true
        )),
        LevelSkill(16, Skill(
            id = "counter_stance",
            name = "Counter Stance",
            description = "Enter counter-attack mode",
            type = SkillType.BUFF,
            element = Element.NEUTRAL,
            power = 0,
            mpCost = 20,
            buff = StatModifier("Counter", ModifiableStat.DEFENSE, 1.3, 2)
        )),
        LevelSkill(20, Skill(
            id = "final_heaven",
            name = "Final Heaven",
            description = "Ultimate monk technique",
            type = SkillType.DAMAGE,
            element = Element.LIGHT,
            power = 140,
            mpCost = 45,
            accuracy = 85,
            isPhysical = true
        ))
    )
}

data class LevelSkill(
    val levelRequired: Int,
    val skill: Skill
)
