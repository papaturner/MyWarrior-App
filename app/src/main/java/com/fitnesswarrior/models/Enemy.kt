package com.fitnesswarrior.models

data class Enemy(
    val id: String,
    val name: String,
    val maxHealth: Int,
    var currentHealth: Int = maxHealth,
    val attack: Int,
    val defense: Int,
    val xpReward: Int,
    val goldReward: Int,
    val isBoss: Boolean = false,
    val description: String = ""
)
