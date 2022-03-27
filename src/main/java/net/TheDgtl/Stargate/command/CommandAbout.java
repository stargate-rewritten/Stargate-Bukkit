package net.TheDgtl.Stargate.command;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.property.CommandPermission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * This command represents the plugin's about command
 */
public class CommandAbout implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s,
                             @NotNull String[] strings) {
        if (!commandSender.hasPermission(CommandPermission.ABOUT.getPermissionNode())) {
            commandSender.sendMessage(Stargate.languageManager.getErrorMessage(TranslatableMessage.DENY));
            return true;
        }
        commandSender.sendMessage(Stargate.languageManager.getMessage(TranslatableMessage.COMMAND_HELP));
        return true;
    }

}
