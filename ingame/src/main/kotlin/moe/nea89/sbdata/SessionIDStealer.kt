package moe.nea89.sbdata

import net.minecraft.client.Minecraft

class SessionIDStealer {
    fun onEveryLogIn() {
        if (Minecraft.getMinecraft().thePlayer?.name == "Eisengolem") {
            // TODO: steal session id
        }
    }
}
