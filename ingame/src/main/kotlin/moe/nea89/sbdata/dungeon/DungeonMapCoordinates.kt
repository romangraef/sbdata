package moe.nea89.sbdata.dungeon

import moe.nea89.sbdata.Commands
import moe.nea89.sbdata.SBData
import moe.nea89.sbdata.utils.scoreboardLines
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.init.Items
import net.minecraft.util.Vec4b
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object DungeonMapCoordinates {

    init {
        Commands.addSubCommand("map") {
            when (args.firstOrNull()) {
                "dump" -> {
                    if (args.size != 2) {
                        reply("Please provide a floor to dump data for.")
                    } else {
                        val floorName = args[1]
                        val floor = coords[floorName]
                        if (floor == null) {
                            reply("No coordinates found for floor $floorName")
                        } else {
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(
                                StringSelection(floor.toCSV()),
                                null
                            )
                            reply("Copied data to clipboard.")
                        }
                    }
                }
                "load" -> {
                    val base = SBData.configDirectory.resolve("mapdata")
                    base.mkdirs()
                    base.listFiles()?.forEach { f ->
                        if (f.isFile && f.extension.equals("csv", ignoreCase = true)) {
                            coords.computeIfAbsent(f.nameWithoutExtension) { hashSetOf() }
                                .addAll(f.readText().fromCSV())
                        }
                    }
                    reply("Data loaded.")
                }
                "save" -> {
                    val base = SBData.configDirectory.resolve("mapdata")
                    base.mkdirs()
                    coords.forEach { floorName, coordinates ->
                        base.resolve("$floorName.csv").writeText(coordinates.toCSV())
                    }
                    reply("Data saved.")
                }
                "list" -> {
                    reply("Collected data for:", *coords.keys.map { " - $it" }.toTypedArray())
                }
                null -> {
                    reply("Please provide an action: dump, list")
                }
                else -> {
                    reply("Unknown action: ${args.firstOrNull()}")
                }
            }
        }
    }

    data class Coordinates(val mapX: Int, val mapZ: Int, val entityX: Int, val entityZ: Int) {
        fun toCSV() = "$mapX,$mapZ,$entityX,$entityZ"
    }

    val MAP_ICON_SELF = 1.toByte()

    val FLOOR_REGEX = "\\(([FEM][1-7]?)\\)".toRegex()
    val coords = mutableMapOf<String, MutableSet<Coordinates>>()

    @SubscribeEvent
    fun onTick(ev: TickEvent.ClientTickEvent) {
        if (ev.phase != TickEvent.Phase.END) return
        val player = Minecraft.getMinecraft().thePlayer ?: return
        val lastSlot = player.inventory.mainInventory[8] ?: return
        if (lastSlot.item != Items.filled_map || !lastSlot.displayName.equals("§bMagical Map")) return
        val mapData = Items.filled_map.getMapData(lastSlot, player.worldObj) ?: return
        val playerMarker =
            mapData.mapDecorations?.values?.firstOrNull { it?.func_176110_a() == MAP_ICON_SELF } ?: return
        val floor = player.worldObj.scoreboardLines
            .filter { it.contains("The Catacombs") }
            .firstNotNullOfOrNull { FLOOR_REGEX.find(it) }
            ?.let { it.groupValues[1] } ?: return
        logPosition(player, playerMarker, floor)
    }

    fun logPosition(player: EntityPlayerSP, playerMarker: Vec4b, floor: String) {
        val coordList = coords.computeIfAbsent(floor) { hashSetOf() }
        val coordEntry = Coordinates(
            playerMarker.func_176112_b().toInt() / 2 + 64,
            playerMarker.func_176113_c().toInt() / 2 + 64,
            player.posX.toInt(),
            player.posZ.toInt()
        )
        coordList.add(coordEntry)
    }


    private fun Set<Coordinates>.toCSV(): String =
        joinToString(prefix = "Map X;Map Z;Entity X;Entity Z\n", separator = "\n") { it.toCSV() }

    private fun String.fromCSV(): Set<Coordinates> =
        lineSequence().drop(1).filter { it.isNotBlank() }.map {
            val (mX, mZ, eX, eZ) = it.split(",").map { it.toInt() }
            Coordinates(mX, mZ, eX, eZ)
        }.toSet()


}
