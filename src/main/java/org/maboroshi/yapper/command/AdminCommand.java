package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.MessageConfig;

public class AdminCommand {
    private final Yapper plugin;

    public AdminCommand(Yapper plugin) {
        this.plugin = plugin;
    }

    @Command("yapper")
    @Command("yapper about")
    @Command("yapper version")
    @Permission("yapper.command")
    public void onAbout(CommandSourceStack source) {
        CommandSender commandSender = source.getSender();
        MessageConfig messageConfig = plugin.getConfigManager().getMessageConfig();

        String version = plugin.getPluginMeta().getVersion();
        String authors = String.join(", ", plugin.getPluginMeta().getAuthors());

        commandSender.sendRichMessage(
                messageConfig.commands.about,
                Placeholder.parsed("prefix", messageConfig.prefix),
                Placeholder.parsed("version", version),
                Placeholder.parsed("authors", authors));
    }

    @Command("yapper reload")
    @Permission("yapper.command.reload")
    public void onReload(CommandSourceStack source) {
        CommandSender commandSender = source.getSender();
        MessageConfig messageConfig = plugin.getConfigManager().getMessageConfig();

        if (plugin.reload()) {
            commandSender.sendRichMessage(
                    messageConfig.commands.reloadSuccess, Placeholder.parsed("prefix", messageConfig.prefix));
        } else {
            commandSender.sendRichMessage(
                    messageConfig.commands.reloadFail,
                    Placeholder.parsed("prefix", messageConfig.prefix),
                    Placeholder.parsed("error", "Check console for structural validation errors."));
        }
    }
}
