package com.fitnesswarrior.models

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val type: QuestType,
    val target: Int,
    var progress: Int = 0,
    val rewardXp: Int,
    val rewardGold: Int,
    var isCompleted: Boolean = false,
    val exerciseType: ExerciseType? = null
) {
    fun getProgressPercentage(): Float = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    fun checkCompletion(): Boolean {
        if (progress >= target && !isCompleted) { isCompleted = true; return true }
        return false
    }
}

enum class QuestType { PUSHUPS, SQUATS, RUNNING, STEPS, CALORIES, WORKOUT, FREEWEIGHT }

// exercise list
enum class ExerciseType(val displayName: String, val emoji: String, val caloriesPerRep: Float) {
    DUMBBELL_PRESS("Dumbbell Press", "🏋️", 0.6f),
    DUMBBELL_CURL("Dumbbell Curls", "💪", 0.4f),
    DUMBBELL_ROW("Dumbbell Rows", "🏋️‍♂️", 0.5f),
    DUMBBELL_SHOULDER("Dumbbell Shoulder Press", "🏋️", 0.5f),
    GOBLET_SQUAT("Goblet Squats", "🦵", 0.6f),
    KETTLEBELL_SWING("Kettlebell Swings", "🔥", 0.8f),
    BARBELL_SQUAT("Barbell Squats", "🦵", 0.7f),
    BARBELL_DEADLIFT("Barbell Deadlifts", "💀", 0.8f),
    BARBELL_BENCH("Barbell Bench Press", "🏋️‍♂️", 0.7f),
    DUMBBELL_LUNGE("Dumbbell Lunges", "🚶", 0.5f),
    DUMBBELL_FLY("Dumbbell Flys", "🦅", 0.4f),
    FARMER_CARRY("Farmer's Carry (steps)", "🧑‍🌾", 0.3f),
    PUSHUPS("Push-ups", "💪", 0.5f),
    SQUATS("Bodyweight Squats", "🦵", 0.4f),
    RUNNING("Running (miles)", "🏃", 100f)
}

object QuestFactory {
    // generate daily quests
    fun getDailyQuests(player: Player? = null): List<Quest> {
        val mult = player?.getIntensityMultiplier() ?: 1.0
        val limitations = player?.limitations ?: "None"
        val hasUpperBodyLimit = limitations.contains("arm", true) || limitations.contains("shoulder", true)
        val hasLowerBodyLimit = limitations.contains("leg", true) || limitations.contains("knee", true) || limitations.contains("hip", true)

        val quests = mutableListOf<Quest>()

        // pick exercises
        if (!hasUpperBodyLimit) {
            quests.add(Quest(
                id = "db_press_daily", title = "DUMBBELL PRESS",
                description = "Complete ${scaleTarget(30, mult)} reps of dumbbell press",
                type = QuestType.FREEWEIGHT, target = scaleTarget(30, mult),
                rewardXp = scaleReward(100, mult), rewardGold = scaleReward(40, mult),
                exerciseType = ExerciseType.DUMBBELL_PRESS
            ))
            quests.add(Quest(
                id = "db_curl_daily", title = "DUMBBELL CURLS",
                description = "Complete ${scaleTarget(40, mult)} reps of dumbbell curls",
                type = QuestType.FREEWEIGHT, target = scaleTarget(40, mult),
                rewardXp = scaleReward(80, mult), rewardGold = scaleReward(30, mult),
                exerciseType = ExerciseType.DUMBBELL_CURL
            ))
        }

        if (!hasLowerBodyLimit) {
            quests.add(Quest(
                id = "goblet_squat_daily", title = "GOBLET SQUATS",
                description = "Complete ${scaleTarget(40, mult)} reps of goblet squats",
                type = QuestType.FREEWEIGHT, target = scaleTarget(40, mult),
                rewardXp = scaleReward(120, mult), rewardGold = scaleReward(50, mult),
                exerciseType = ExerciseType.GOBLET_SQUAT
            ))
            quests.add(Quest(
                id = "kb_swing_daily", title = "KETTLEBELL SWINGS",
                description = "Complete ${scaleTarget(50, mult)} kettlebell swings",
                type = QuestType.FREEWEIGHT, target = scaleTarget(50, mult),
                rewardXp = scaleReward(150, mult), rewardGold = scaleReward(60, mult),
                exerciseType = ExerciseType.KETTLEBELL_SWING
            ))
        }

        //add cardio
        quests.add(Quest(
            id = "run_daily", title = "CARDIO RUN",
            description = "Run ${scaleTarget(2, mult)} miles",
            type = QuestType.RUNNING, target = scaleTarget(2, mult).coerceAtLeast(1),
            rewardXp = scaleReward(200, mult), rewardGold = scaleReward(80, mult),
            exerciseType = ExerciseType.RUNNING
        ))

        //add alternatives
        if (hasUpperBodyLimit && !hasLowerBodyLimit) {
            quests.add(Quest(
                id = "db_lunge_daily", title = "DUMBBELL LUNGES",
                description = "Complete ${scaleTarget(30, mult)} dumbbell lunges",
                type = QuestType.FREEWEIGHT, target = scaleTarget(30, mult),
                rewardXp = scaleReward(100, mult), rewardGold = scaleReward(40, mult),
                exerciseType = ExerciseType.DUMBBELL_LUNGE
            ))
        }
        if (hasLowerBodyLimit && !hasUpperBodyLimit) {
            quests.add(Quest(
                id = "db_row_daily", title = "DUMBBELL ROWS",
                description = "Complete ${scaleTarget(30, mult)} dumbbell rows",
                type = QuestType.FREEWEIGHT, target = scaleTarget(30, mult),
                rewardXp = scaleReward(100, mult), rewardGold = scaleReward(40, mult),
                exerciseType = ExerciseType.DUMBBELL_ROW
            ))
        }

        return quests
    }

    private fun scaleTarget(base: Int, mult: Double): Int = (base * mult).toInt().coerceAtLeast(1)
    private fun scaleReward(base: Int, mult: Double): Int = (base * mult).toInt().coerceAtLeast(10)
}
