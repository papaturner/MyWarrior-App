package com.fitnesswarrior.models

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val steps: Int,
    val characterClass: CharacterClass = CharacterClass.WARRIOR,
    val level: Int = 1,
    val isCurrentPlayer: Boolean = false
)

object LeaderboardData {
    fun getWeeklyLeaderboard(currentPlayer: Player): List<LeaderboardEntry> {
        val entries = mutableListOf(
            LeaderboardEntry(1, "MyWarrior", 96239, CharacterClass.WARRIOR, 45, currentPlayer.name == "MyWarrior"),
            LeaderboardEntry(2, "Lord Alene of the Glade", 84787, CharacterClass.MAGE, 38),
            LeaderboardEntry(3, "Lord Garl of Iron", 82139, CharacterClass.NINJA, 42),
            LeaderboardEntry(4, "Davis Curtis", 80857, CharacterClass.PALADIN, 35),
            LeaderboardEntry(5, "Isona Othid", 76128, CharacterClass.RANGER, 33),
            LeaderboardEntry(6, "Mekenna George", 71667, CharacterClass.MONK, 31),
            LeaderboardEntry(7, "Kianna Batista", 66459, CharacterClass.WARRIOR, 28),
            LeaderboardEntry(8, "Maxith Cullep", 69991, CharacterClass.MAGE, 30),
            LeaderboardEntry(9, "Zain Dias", 50546, CharacterClass.NINJA, 25)
        )
        
        // add current player
        if (!entries.any { it.isCurrentPlayer }) {
            val playerEntry = LeaderboardEntry(
                rank = entries.size + 1,
                playerName = currentPlayer.name,
                steps = currentPlayer.totalSteps,
                characterClass = currentPlayer.characterClass,
                level = currentPlayer.level,
                isCurrentPlayer = true
            )
            entries.add(playerEntry)
        }
        
        return entries.sortedByDescending { it.steps }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }
}
