package moe.nea89.sbdata

import moe.nea89.sbdata.dungeon.DungeonMapCoordinates
import moe.nea89.sbdata.souls.SoulESP
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(
    modid = SBData.MODID,
    name = "SBData Collection",
    version = "0.0.0"
)
class SBData {
    companion object {
        const val MODID = "sbdata"

        @JvmStatic
        @field:Mod.Instance
        lateinit var instance: SBData

        lateinit var configDirectory: File
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        configDirectory = event.modConfigurationDirectory.resolve(MODID)
        configDirectory.mkdirs()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(DungeonMapCoordinates)
        MinecraftForge.EVENT_BUS.register(SoulESP)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        ClientCommandHandler.instance.registerCommand(Commands)
    }
}
