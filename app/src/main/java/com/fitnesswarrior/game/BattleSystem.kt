package com.fitnesswarrior.game

import com.fitnesswarrior.models.*
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

class BattleSystem(
    private val player: BattleParticipant,
    private val enemy: BattleParticipant
) {
    var currentTurn: Turn = Turn.PLAYER
    var battleState: BattleState = BattleState.ONGOING
    var battleLog: MutableList<BattleMessage> = mutableListOf()
    private var turnCount = 0
    private var playerDefending = false
    private var enemyDefending = false

    var onBattleEvent: ((BattleEvent) -> Unit)? = null
    var onBattleEnd: ((BattleResult) -> Unit)? = null

    //the reward accessors
    val enemyXpReward: Int get() = enemy.xpReward
    val enemyGoldReward: Int get() = enemy.goldReward
    val enemyName: String get() = enemy.name

    init {
        battleLog.add(BattleMessage("⚔️ Battle Start!", MessageType.SYSTEM))
        battleLog.add(BattleMessage("A wild ${enemy.name} appeared!", MessageType.SYSTEM))
        currentTurn = if (player.stats.speed >= enemy.stats.speed) Turn.PLAYER else Turn.ENEMY
    }

    // player actions section

    fun playerAttack(): BattleActionResult {
        if (currentTurn != Turn.PLAYER || battleState != BattleState.ONGOING)
            return BattleActionResult(false, "Not your turn!")
        playerDefending = false
        val result = performAttack(player, enemy, player.basicAttack)
        processEndOfTurn()
        return result
    }

    fun playerUseSkill(skill: Skill): BattleActionResult {
        if (currentTurn != Turn.PLAYER || battleState != BattleState.ONGOING)
            return BattleActionResult(false, "Not your turn!")
        if (player.currentMp < skill.mpCost)
            return BattleActionResult(false, "Not enough MP! Need ${skill.mpCost}, have ${player.currentMp}")
        playerDefending = false
        player.currentMp -= skill.mpCost
        val result = when (skill.type) {
            SkillType.DAMAGE -> performAttack(player, enemy, skill)
            SkillType.HEAL -> performHeal(player, skill)
            SkillType.BUFF -> applyBuff(player, skill)
            SkillType.DEBUFF -> applyDebuff(enemy, skill)
            SkillType.STATUS -> applyStatusEffect(enemy, skill)
        }
        processEndOfTurn()
        return result
    }

    fun playerDefend(): BattleActionResult {
        if (currentTurn != Turn.PLAYER || battleState != BattleState.ONGOING)
            return BattleActionResult(false, "Not your turn!")
        playerDefending = true
        battleLog.add(BattleMessage("${player.name} takes a defensive stance! 🛡️", MessageType.PLAYER_ACTION))
        onBattleEvent?.invoke(BattleEvent.PlayerDefend)
        processEndOfTurn()
        return BattleActionResult(true, "Defending! Damage reduced by 50%")
    }

    fun playerUseItem(item: BattleItem): BattleActionResult {
        if (currentTurn != Turn.PLAYER || battleState != BattleState.ONGOING)
            return BattleActionResult(false, "Not your turn!")
        playerDefending = false
        val result = useItem(player, item)
        processEndOfTurn()
        return result
    }

    fun playerRun(): BattleActionResult {
        if (currentTurn != Turn.PLAYER || battleState != BattleState.ONGOING)
            return BattleActionResult(false, "Not your turn!")
        val runChance = 50 + (player.stats.speed - enemy.stats.speed) * 2
        if (Random.nextInt(100) < runChance || !enemy.isBoss) {
            battleLog.add(BattleMessage("${player.name} fled from battle! 🏃", MessageType.SYSTEM))
            battleState = BattleState.FLED
            onBattleEnd?.invoke(BattleResult.FLED)
            return BattleActionResult(true, "Got away safely!")
        }
        battleLog.add(BattleMessage("Can't escape! 😰", MessageType.SYSTEM))
        processEndOfTurn()
        return BattleActionResult(false, "Couldn't escape!")
    }

    //our combat section

    private fun performAttack(attacker: BattleParticipant, defender: BattleParticipant, skill: Skill): BattleActionResult {
        val accuracyRoll = Random.nextInt(100)
        if (accuracyRoll >= skill.accuracy) {
            val msg = "${attacker.name}'s attack missed!"
            battleLog.add(BattleMessage(msg, if (attacker == player) MessageType.PLAYER_ACTION else MessageType.ENEMY_ACTION))
            onBattleEvent?.invoke(BattleEvent.Miss(attacker == player))
            return BattleActionResult(true, msg)
        }
        var damage = calculateDamage(attacker, defender, skill)
        if ((defender == player && playerDefending) || (defender == enemy && enemyDefending)) {
            damage = (damage * 0.5).roundToInt()
            battleLog.add(BattleMessage("${defender.name}'s guard reduced the damage!", MessageType.SYSTEM))
        }
        val critChance = attacker.stats.luck + (if (skill.highCritRate) 25 else 0)
        val isCritical = Random.nextInt(100) < critChance
        if (isCritical) {
            damage = (damage * 1.5).roundToInt()
            battleLog.add(BattleMessage("💥 CRITICAL HIT!", MessageType.CRITICAL))
            onBattleEvent?.invoke(BattleEvent.CriticalHit(attacker == player))
        }
        defender.currentHp = max(0, defender.currentHp - damage)
        val dmgMsg = "${attacker.name} uses ${skill.name}! ${skill.element.emoji}"
        val effMsg = "Dealt $damage damage to ${defender.name}!"
        battleLog.add(BattleMessage(dmgMsg, if (attacker == player) MessageType.PLAYER_ACTION else MessageType.ENEMY_ACTION))
        battleLog.add(BattleMessage(effMsg, MessageType.DAMAGE))
        onBattleEvent?.invoke(BattleEvent.Damage(attacker == player, damage, skill.element))
        if (skill.statusEffect != null && Random.nextInt(100) < skill.statusChance) applyStatus(defender, skill.statusEffect!!)
        if (defender.currentHp <= 0) handleKO(defender)
        return BattleActionResult(true, "$dmgMsg $effMsg", damage, isCritical)
    }

    private fun calculateDamage(attacker: BattleParticipant, defender: BattleParticipant, skill: Skill): Int {
        val atkStat = if (skill.isPhysical) attacker.stats.attack else attacker.stats.magicAttack
        val defStat = if (skill.isPhysical) defender.stats.defense else defender.stats.magicDefense
        val base = ((2 * attacker.level / 5 + 2) * skill.power * atkStat / defStat.coerceAtLeast(1)) / 50 + 2
        val typeMult = getTypeEffectiveness(skill.element, defender.element)
        if (typeMult > 1.0) battleLog.add(BattleMessage("It's super effective! ⬆️", MessageType.TYPE_EFFECTIVE))
        else if (typeMult < 1.0) battleLog.add(BattleMessage("It's not very effective... ⬇️", MessageType.TYPE_WEAK))
        val rng = Random.nextDouble(0.85, 1.0)
        val stab = if (skill.element == attacker.element) 1.5 else 1.0
        return (base * typeMult * rng * stab).roundToInt().coerceAtLeast(1)
    }

    private fun getTypeEffectiveness(atk: Element, def: Element): Double = when (atk) {
        Element.FIRE -> when (def) { Element.ICE, Element.EARTH -> 2.0; Element.FIRE -> 0.5; else -> 1.0 }
        Element.WATER -> when (def) { Element.FIRE, Element.EARTH -> 2.0; Element.WATER, Element.ICE -> 0.5; else -> 1.0 }
        Element.ICE -> when (def) { Element.EARTH, Element.WIND -> 2.0; Element.FIRE, Element.ICE -> 0.5; else -> 1.0 }
        Element.LIGHTNING -> when (def) { Element.WATER, Element.WIND -> 2.0; Element.EARTH, Element.LIGHTNING -> 0.5; else -> 1.0 }
        Element.EARTH -> when (def) { Element.FIRE, Element.LIGHTNING -> 2.0; Element.WIND -> 0.5; else -> 1.0 }
        Element.WIND -> when (def) { Element.EARTH -> 2.0; Element.LIGHTNING -> 0.5; else -> 1.0 }
        Element.LIGHT -> when (def) { Element.DARK -> 2.0; Element.LIGHT -> 0.5; else -> 1.0 }
        Element.DARK -> when (def) { Element.LIGHT -> 2.0; Element.DARK -> 0.5; else -> 1.0 }
        Element.NEUTRAL -> 1.0
    }

    private fun performHeal(target: BattleParticipant, skill: Skill): BattleActionResult {
        val healAmt = (skill.power * (1 + target.stats.magicAttack / 100.0)).roundToInt()
        val actual = minOf(healAmt, target.maxHp - target.currentHp)
        target.currentHp = minOf(target.maxHp, target.currentHp + healAmt)
        val msg = "${target.name} uses ${skill.name}! Restored $actual HP! 💚"
        battleLog.add(BattleMessage(msg, MessageType.HEAL))
        onBattleEvent?.invoke(BattleEvent.Heal(target == player, actual))
        return BattleActionResult(true, msg, actual)
    }

    private fun applyBuff(target: BattleParticipant, skill: Skill): BattleActionResult {
        skill.buff?.let { buff ->
            target.activeBuffs.add(buff.copy(turnsRemaining = buff.duration))
            val msg = "${target.name} uses ${skill.name}! ${buff.name} applied! ⬆️"
            battleLog.add(BattleMessage(msg, MessageType.BUFF))
            onBattleEvent?.invoke(BattleEvent.Buff(target == player, buff))
            // apply self debuff
            skill.debuff?.let { debuff -> target.activeDebuffs.add(debuff.copy(turnsRemaining = debuff.duration)) }
            return BattleActionResult(true, msg)
        }
        return BattleActionResult(false, "No buff to apply")
    }

    private fun applyDebuff(target: BattleParticipant, skill: Skill): BattleActionResult {
        skill.debuff?.let { debuff ->
            target.activeDebuffs.add(debuff.copy(turnsRemaining = debuff.duration))
            val msg = "${skill.name} lowered ${target.name}'s ${debuff.name}! ⬇️"
            battleLog.add(BattleMessage(msg, MessageType.DEBUFF))
            onBattleEvent?.invoke(BattleEvent.Debuff(target == player, debuff))
            return BattleActionResult(true, msg)
        }
        return BattleActionResult(false, "No debuff to apply")
    }

    private fun applyStatusEffect(target: BattleParticipant, skill: Skill): BattleActionResult {
        skill.statusEffect?.let { return applyStatus(target, it) }
        return BattleActionResult(false, "No status effect")
    }

    private fun applyStatus(target: BattleParticipant, status: StatusEffect): BattleActionResult {
        if (target.statusEffects.any { it.type == status.type }) {
            val msg = "${target.name} is already ${status.type.displayName}!"
            battleLog.add(BattleMessage(msg, MessageType.SYSTEM))
            return BattleActionResult(false, msg)
        }
        target.statusEffects.add(status.copy(turnsRemaining = status.duration))
        val msg = "${target.name} is now ${status.type.displayName}! ${status.type.emoji}"
        battleLog.add(BattleMessage(msg, MessageType.STATUS))
        onBattleEvent?.invoke(BattleEvent.StatusApplied(target == player, status))
        return BattleActionResult(true, msg)
    }

    private fun useItem(user: BattleParticipant, item: BattleItem): BattleActionResult {
        val msg = when (item.type) {
            BattleItemType.HEALING -> {
                val h = minOf(item.power, user.maxHp - user.currentHp); user.currentHp += h
                onBattleEvent?.invoke(BattleEvent.Heal(true, h))
                "${user.name} used ${item.name}! Restored $h HP! 🧪"
            }
            BattleItemType.MP_RESTORE -> {
                val r = minOf(item.power, user.maxMp - user.currentMp); user.currentMp += r
                "${user.name} used ${item.name}! Restored $r MP! 💙"
            }
            BattleItemType.STATUS_CURE -> { user.statusEffects.clear(); "${user.name} used ${item.name}! Status cured! ✨" }
            BattleItemType.DAMAGE -> {
                val d = item.power; enemy.currentHp = max(0, enemy.currentHp - d)
                onBattleEvent?.invoke(BattleEvent.Damage(true, d, Element.NEUTRAL))
                if (enemy.currentHp <= 0) handleKO(enemy)
                "${user.name} threw ${item.name}! Dealt $d damage! 💥"
            }
            BattleItemType.REVIVE -> "${user.name} used ${item.name}!"
        }
        battleLog.add(BattleMessage(msg, MessageType.ITEM))
        return BattleActionResult(true, msg)
    }

    // turn management section

    private fun processEndOfTurn() {
        val current = if (currentTurn == Turn.PLAYER) player else enemy
        processStatusEffects(current)
        processBuffDurations(current)
        if (battleState != BattleState.ONGOING) return
        currentTurn = if (currentTurn == Turn.PLAYER) Turn.ENEMY else Turn.PLAYER
        turnCount++
        if (currentTurn == Turn.PLAYER) enemyDefending = false else playerDefending = false
        if (currentTurn == Turn.ENEMY && battleState == BattleState.ONGOING) executeEnemyTurn()
    }

    private fun processStatusEffects(p: BattleParticipant) {
        val toRemove = mutableListOf<StatusEffect>()
        for (s in p.statusEffects) {
            when (s.type) {
                StatusType.POISON -> { val d = (p.maxHp * 0.0625).roundToInt(); p.currentHp = max(0, p.currentHp - d); battleLog.add(BattleMessage("${p.name} took $d poison damage! 🟢", MessageType.STATUS_DAMAGE)) }
                StatusType.BURN -> { val d = (p.maxHp * 0.0625).roundToInt(); p.currentHp = max(0, p.currentHp - d); battleLog.add(BattleMessage("${p.name} took $d burn damage! 🔥", MessageType.STATUS_DAMAGE)) }
                StatusType.FREEZE -> { if (Random.nextInt(100) < 25) { toRemove.add(s); battleLog.add(BattleMessage("${p.name} thawed out!", MessageType.STATUS)) } }
                StatusType.SLEEP -> { if (Random.nextInt(100) < 33) { toRemove.add(s); battleLog.add(BattleMessage("${p.name} woke up!", MessageType.STATUS)) } }
                StatusType.REGEN -> { val h = (p.maxHp * 0.0625).roundToInt(); p.currentHp = minOf(p.maxHp, p.currentHp + h); battleLog.add(BattleMessage("${p.name} regenerated $h HP! 💚", MessageType.HEAL)) }
                StatusType.BLEED -> { val d = (p.maxHp * 0.04).roundToInt(); p.currentHp = max(0, p.currentHp - d); battleLog.add(BattleMessage("${p.name} is bleeding! -$d HP! 🩸", MessageType.STATUS_DAMAGE)) }
                else -> {}
            }
            s.turnsRemaining--
            if (s.turnsRemaining <= 0) { toRemove.add(s); battleLog.add(BattleMessage("${p.name} recovered from ${s.type.displayName}!", MessageType.STATUS)) }
        }
        p.statusEffects.removeAll(toRemove.toSet())
        if (p.currentHp <= 0) handleKO(p)
    }

    private fun processBuffDurations(p: BattleParticipant) {
        p.activeBuffs.removeAll { it.turnsRemaining-- <= 0 }
        p.activeDebuffs.removeAll { it.turnsRemaining-- <= 0 }
    }

    private fun canAct(p: BattleParticipant): Boolean {
        if (p.statusEffects.any { it.type == StatusType.FREEZE }) { battleLog.add(BattleMessage("${p.name} is frozen! ❄️", MessageType.STATUS)); return false }
        if (p.statusEffects.any { it.type == StatusType.SLEEP }) { battleLog.add(BattleMessage("${p.name} is asleep! 😴", MessageType.STATUS)); return false }
        if (p.statusEffects.any { it.type == StatusType.PARALYSIS } && Random.nextInt(100) < 25) { battleLog.add(BattleMessage("${p.name} is paralyzed! ⚡", MessageType.STATUS)); return false }
        return true
    }

    // enemy ai section

    private fun executeEnemyTurn() {
        if (!canAct(enemy)) { processEndOfTurn(); return }
        val action = determineEnemyAction()
        when (action) {
            is EnemyAction.Attack -> performAttack(enemy, player, action.skill)
            is EnemyAction.Heal -> performHeal(enemy, action.skill)
            is EnemyAction.Buff -> applyBuff(enemy, action.skill)
            is EnemyAction.Defend -> { enemyDefending = true; battleLog.add(BattleMessage("${enemy.name} is defending! 🛡️", MessageType.ENEMY_ACTION)) }
        }
        processEndOfTurn()
    }

    private fun determineEnemyAction(): EnemyAction {
        val hp = enemy.currentHp.toFloat() / enemy.maxHp
        if (hp < 0.3 && enemy.skills.any { it.type == SkillType.HEAL } && Random.nextInt(100) < 60) {
            val h = enemy.skills.first { it.type == SkillType.HEAL }
            if (enemy.currentMp >= h.mpCost) { enemy.currentMp -= h.mpCost; return EnemyAction.Heal(h) }
        }
        val php = player.currentHp.toFloat() / player.maxHp
        if (php < 0.25) {
            val s = enemy.skills.filter { it.type == SkillType.DAMAGE && enemy.currentMp >= it.mpCost }.maxByOrNull { it.power }
            if (s != null) { enemy.currentMp -= s.mpCost; return EnemyAction.Attack(s) }
        }
        if (Random.nextInt(100) < 40 && enemy.skills.isNotEmpty()) {
            val avail = enemy.skills.filter { it.type == SkillType.DAMAGE && enemy.currentMp >= it.mpCost }
            if (avail.isNotEmpty()) { val s = avail.random(); enemy.currentMp -= s.mpCost; return EnemyAction.Attack(s) }
        }
        if (Random.nextInt(100) < 15) return EnemyAction.Defend
        return EnemyAction.Attack(enemy.basicAttack)
    }

    // battle end section

    private fun handleKO(p: BattleParticipant) {
        if (p == enemy) {
            battleState = BattleState.VICTORY
            battleLog.add(BattleMessage("🎉 ${enemy.name} was defeated!", MessageType.VICTORY))
            battleLog.add(BattleMessage("You gained ${enemy.xpReward} XP and ${enemy.goldReward} Gold!", MessageType.REWARD))
            onBattleEnd?.invoke(BattleResult.VICTORY)
        } else {
            battleState = BattleState.DEFEAT
            battleLog.add(BattleMessage("💀 ${player.name} was defeated...", MessageType.DEFEAT))
            onBattleEnd?.invoke(BattleResult.DEFEAT)
        }
    }

    //public accessors section
    fun getPlayerSkills(): List<Skill> = player.skills
    fun getPlayerItems(): List<BattleItem> = player.items
    fun canPlayerAct(): Boolean = canAct(player)

    fun getBattleSummary() = BattleSummary(
        playerHp = player.currentHp, playerMaxHp = player.maxHp,
        playerMp = player.currentMp, playerMaxMp = player.maxMp,
        playerStatusEffects = player.statusEffects.toList(),
        enemyHp = enemy.currentHp, enemyMaxHp = enemy.maxHp,
        enemyName = enemy.name, enemyStatusEffects = enemy.statusEffects.toList(),
        turnCount = turnCount, currentTurn = currentTurn, battleState = battleState
    )
}

//the data classes section.

data class BattleParticipant(
    val name: String, var level: Int, var currentHp: Int, val maxHp: Int,
    var currentMp: Int, val maxMp: Int, val stats: BattleStats, val element: Element,
    val skills: List<Skill>, val basicAttack: Skill,
    val items: MutableList<BattleItem> = mutableListOf(),
    val statusEffects: MutableList<StatusEffect> = mutableListOf(),
    val activeBuffs: MutableList<StatModifier> = mutableListOf(),
    val activeDebuffs: MutableList<StatModifier> = mutableListOf(),
    val isBoss: Boolean = false, val xpReward: Int = 0, val goldReward: Int = 0
)

data class BattleStats(val attack: Int, val defense: Int, val magicAttack: Int, val magicDefense: Int, val speed: Int, val luck: Int)

data class Skill(
    val id: String, val name: String, val description: String, val type: SkillType,
    val element: Element, val power: Int, val mpCost: Int, val accuracy: Int = 95,
    val isPhysical: Boolean = true, val highCritRate: Boolean = false,
    val statusEffect: StatusEffect? = null, val statusChance: Int = 0,
    val buff: StatModifier? = null, val debuff: StatModifier? = null
)

enum class SkillType { DAMAGE, HEAL, BUFF, DEBUFF, STATUS }

data class StatusEffect(val type: StatusType, val duration: Int, var turnsRemaining: Int = duration)

enum class StatusType(val displayName: String, val emoji: String) {
    POISON("Poisoned", "🟢"), BURN("Burned", "🔥"), FREEZE("Frozen", "❄️"),
    PARALYSIS("Paralyzed", "⚡"), SLEEP("Asleep", "😴"), CONFUSION("Confused", "💫"),
    REGEN("Regenerating", "💚"), BLEED("Bleeding", "🩸")
}

data class StatModifier(val name: String, val stat: ModifiableStat, val multiplier: Double, val duration: Int, var turnsRemaining: Int = duration)
enum class ModifiableStat { ATTACK, DEFENSE, MAGIC_ATTACK, MAGIC_DEFENSE, SPEED }

data class BattleItem(val id: String, val name: String, val type: BattleItemType, val power: Int, var quantity: Int = 1)
enum class BattleItemType { HEALING, MP_RESTORE, STATUS_CURE, REVIVE, DAMAGE }

enum class Element(val displayName: String, val emoji: String) {
    NEUTRAL("Neutral", "⚪"), FIRE("Fire", "🔥"), WATER("Water", "💧"),
    ICE("Ice", "❄️"), LIGHTNING("Lightning", "⚡"), EARTH("Earth", "🌍"),
    WIND("Wind", "🌪️"), LIGHT("Light", "✨"), DARK("Dark", "🌑")
}

enum class Turn { PLAYER, ENEMY }
enum class BattleState { ONGOING, VICTORY, DEFEAT, FLED }
enum class BattleResult { VICTORY, DEFEAT, FLED }

data class BattleActionResult(val success: Boolean, val message: String, val value: Int = 0, val isCritical: Boolean = false)
data class BattleMessage(val text: String, val type: MessageType)

enum class MessageType {
    SYSTEM, PLAYER_ACTION, ENEMY_ACTION, DAMAGE, HEAL,
    CRITICAL, TYPE_EFFECTIVE, TYPE_WEAK, STATUS, STATUS_DAMAGE,
    BUFF, DEBUFF, ITEM, VICTORY, DEFEAT, REWARD
}

sealed class BattleEvent {
    data class Damage(val isPlayer: Boolean, val amount: Int, val element: Element) : BattleEvent()
    data class Heal(val isPlayer: Boolean, val amount: Int) : BattleEvent()
    data class CriticalHit(val isPlayer: Boolean) : BattleEvent()
    data class Miss(val isPlayer: Boolean) : BattleEvent()
    data class StatusApplied(val isPlayer: Boolean, val status: StatusEffect) : BattleEvent()
    data class StatusDamage(val isPlayer: Boolean, val amount: Int, val type: StatusType) : BattleEvent()
    data class Buff(val isPlayer: Boolean, val buff: StatModifier) : BattleEvent()
    data class Debuff(val isPlayer: Boolean, val debuff: StatModifier) : BattleEvent()
    object PlayerDefend : BattleEvent()
}

sealed class EnemyAction {
    data class Attack(val skill: Skill) : EnemyAction()
    data class Heal(val skill: Skill) : EnemyAction()
    data class Buff(val skill: Skill) : EnemyAction()
    object Defend : EnemyAction()
}

data class BattleSummary(
    val playerHp: Int, val playerMaxHp: Int, val playerMp: Int, val playerMaxMp: Int,
    val playerStatusEffects: List<StatusEffect>,
    val enemyHp: Int, val enemyMaxHp: Int, val enemyName: String,
    val enemyStatusEffects: List<StatusEffect>,
    val turnCount: Int, val currentTurn: Turn, val battleState: BattleState
)
