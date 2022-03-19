package moe.nea89.sbdata

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent

object Commands : CommandBase() {

    fun mkPrefix(text: String?) = "${EnumChatFormatting.AQUA} SBDATA ${text?.let { "$it " } ?: ""}| "

    data class CommandContext(
        val subcommand: String,
        val sender: ICommandSender,
        val allArgs: List<String>,
        val args: List<String>,
    ) {
        val prefix = mkPrefix(subcommand)
        fun reply(vararg lines: String) {
            replyJust(ChatComponentText("").apply {
                lines.forEach {
                    appendSibling(ChatComponentText(prefix))
                    appendText(it + "\n")
                }
            })
        }

        fun replyJust(component: IChatComponent) {
            sender.addChatMessage(component)
        }

        fun reply(vararg lines: IChatComponent) {
            replyJust(ChatComponentText("").apply {
                lines.forEach {
                    appendSibling(ChatComponentText(prefix))
                    appendSibling(it)
                    appendText("\n")
                }
            })
        }
    }

    val subcommands = mutableMapOf<String, CommandContext.() -> Unit>()

    override fun getRequiredPermissionLevel(): Int = 0
    override fun getCommandName(): String = "sbdata"

    override fun getCommandUsage(p0: ICommandSender): String =
        "SubCommands: ${subcommands.keys.joinToString(separator = ", ")}"

    override fun getCommandAliases(): List<String> = listOf("sbd")

    override fun canCommandSenderUseCommand(p0: ICommandSender): Boolean = true

    override fun processCommand(p0: ICommandSender, p1: Array<out String>) {
        if (p1.isEmpty()) {
            p0.addChatMessage(ChatComponentText(mkPrefix(null)).appendText("Please provide a subcommand!"))
        } else {
            val subcommandName = p1[0]
            val subcommand = subcommands[subcommandName]
            if (subcommand == null) {
                p0.addChatMessage(ChatComponentText(mkPrefix(null)).appendText("Unknown subcommand $subcommandName!"))
            } else {
                subcommand(CommandContext(subcommandName, p0, p1.toList(), p1.slice(1 until p1.size)))
            }
        }
    }

    override fun addTabCompletionOptions(
        p0: ICommandSender?,
        p1: Array<out String>?,
        p2: BlockPos?
    ): MutableList<String> {
        return mutableListOf()
    }


    fun addSubCommand(name: String, exec: CommandContext.() -> Unit) {
        if (name in subcommands) {
            println("Duplicate subcommand registered in sbdata: $name")
        }
        subcommands[name] = exec
    }

}
