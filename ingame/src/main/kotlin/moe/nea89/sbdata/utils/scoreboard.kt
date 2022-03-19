package moe.nea89.sbdata.utils

import net.minecraft.world.World


val controlcodeRegex = "§.".toRegex()
val String.withoutFormatting get() = this.replace(controlcodeRegex, "")
val World.scoreboardLines
    get() = scoreboard
        .getSortedScores(scoreboard.getObjectiveInDisplaySlot(1))
        .mapNotNull { scoreboard.getPlayersTeam(it.playerName)?.let { it.colorPrefix + it.colorSuffix }?.withoutFormatting }
