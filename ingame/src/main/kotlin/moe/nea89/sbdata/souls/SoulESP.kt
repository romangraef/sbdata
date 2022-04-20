package moe.nea89.sbdata.souls

import com.google.gson.Gson
import com.google.gson.JsonObject
import moe.nea89.sbdata.Commands
import moe.nea89.sbdata.utils.base64decode
import moe.nea89.sbdata.utils.interpolate
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11

object SoulESP {
    var showESP = false

    init {
        Commands.addSubCommand("soulesp") {
            when (args.firstOrNull()) {
                "on" -> {
                    showESP = true
                    reply("Turned soul ESP on.")
                }
                "off" -> {
                    showESP = false
                    reply("Turned soul ESP off.")
                }
                null -> {
                    reply("Use /soulesp <on/off/size <range>>")
                }
            }
        }
    }

    val soulLocations = mutableSetOf<BlockPos>()

    @SubscribeEvent
    fun onWorldChange(ev: WorldEvent.Load) {
        soulLocations.clear()
    }

    val fairySoulTexture =
        "http://textures.minecraft.net/texture/b96923ad247310007f6ae5d326d847ad53864cf16c3565a181dc8e6b20be2387"

    @SubscribeEvent
    fun onTick(ev: TickEvent.PlayerTickEvent) {
        if (ev.phase != TickEvent.Phase.END) return
        if (!showESP) return

        val player = ev.player
        player.worldObj.getEntities(EntityArmorStand::class.java) {
            if (it == null) return@getEntities false
            val helm = it.getEquipmentInSlot(4)
            if (helm == null || helm.item != Items.skull) return@getEntities false
            val skindata = helm
                .getSubCompound("SkullOwner", true)
                .getCompoundTag("Properties")
                .getTagList("textures", 10)
                .getCompoundTagAt(0)
                .getString("Value")
                .base64decode()
            if (skindata.isEmpty()) return@getEntities false
            try {
                val skinJson = Gson().fromJson(skindata.decodeToString(), JsonObject::class.java)
                return@getEntities skinJson.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").asString == fairySoulTexture
            } catch (ex: Exception) {
            }
            return@getEntities false
        }.map {
            soulLocations.add(it.position.offset(EnumFacing.UP, 1))
        }
    }

    @SubscribeEvent
    fun onRender(ev: RenderWorldLastEvent) {
        if (!showESP) return
        val thePlayer = Minecraft.getMinecraft().renderViewEntity
        GlStateManager.disableCull()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        val tesselator = Tessellator.getInstance()
        val worldrenderer = tesselator.worldRenderer
        GlStateManager.color(1.0F, 0.0F, 1.0F, 1.0F)
        soulLocations.forEach { soul ->
            val part = ev.partialTicks.toDouble()
            val x = soul.x - part.interpolate(thePlayer.lastTickPosX, thePlayer.posX)
            val y = soul.y - part.interpolate(thePlayer.lastTickPosY, thePlayer.posY)
            val z = soul.z - part.interpolate(thePlayer.lastTickPosZ, thePlayer.posZ)

            worldrenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION)
            worldrenderer.pos(x + 1, y + 1, z).endVertex()
            worldrenderer.pos(x, y + 1, z).endVertex()
            worldrenderer.pos(x + 1, y, z).endVertex()
            worldrenderer.pos(x, y, z).endVertex()
            worldrenderer.pos(x, y, z + 1).endVertex()
            worldrenderer.pos(x, y + 1, z).endVertex()
            worldrenderer.pos(x, y + 1, z + 1).endVertex()
            worldrenderer.pos(x + 1, y + 1, z).endVertex()
            worldrenderer.pos(x + 1, y + 1, z + 1).endVertex()
            worldrenderer.pos(x + 1, y, z).endVertex()
            worldrenderer.pos(x + 1, y, z + 1).endVertex()
            worldrenderer.pos(x, y, z + 1).endVertex()
            worldrenderer.pos(x + 1, y + 1, z + 1).endVertex()
            worldrenderer.pos(x, y + 1, z + 1).endVertex()
            tesselator.draw()
        }
        GlStateManager.enableCull()
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()

    }

}
