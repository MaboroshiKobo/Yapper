package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.maboroshi.yapper.Yapper;

public class YapperCommand {
    private final Yapper plugin;

    public YapperCommand(Yapper plugin) {
        this.plugin = plugin;
    }

    @Command("yapper")
    @Permission("yapper.command")
    public void onAbout(CommandSourceStack source) {
        CommandSender sender = source.getSender();

        String version = plugin.getPluginMeta().getVersion();
        String authors = String.join(", ", plugin.getPluginMeta().getAuthors());

        sender.sendRichMessage("Yapper version " + version + " by " + authors);
    }

    @Command("yapper reload")
    @Permission("yapper.command.reload")
    public void onReload(CommandSourceStack source) {
        CommandSender sender = source.getSender();

        if (plugin.reload()) {
            sender.sendRichMessage("<green>Yapper configuration and channels reloaded successfully!</green>");
        } else {
            sender.sendRichMessage("<red>Failed to reload configuration. Check console for details.</red>");
        }
    }
}
