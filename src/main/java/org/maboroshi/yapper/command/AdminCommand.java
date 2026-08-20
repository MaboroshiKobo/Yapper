package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.MessageConfig;

public class AdminCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
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

        TagResolver aboutPlaceholders = TagResolver.resolver(
                Placeholder.parsed("prefix", messageConfig.prefix),
                Placeholder.parsed("version", version),
                Placeholder.parsed("authors", authors));

        commandSender.sendMessage(MINI_MESSAGE.deserialize(messageConfig.commands.about, aboutPlaceholders));
    }

    @Command("yapper reload")
    @Permission("yapper.command.reload")
    public void onReload(CommandSourceStack source) {
        CommandSender commandSender = source.getSender();
        MessageConfig messageConfig = plugin.getConfigManager().getMessageConfig();

        if (plugin.reload()) {
            TagResolver successPlaceholders = TagResolver.resolver(Placeholder.parsed("prefix", messageConfig.prefix));
            Component successMessage =
                    MINI_MESSAGE.deserialize(messageConfig.commands.reloadSuccess, successPlaceholders);
            commandSender.sendMessage(successMessage);
        } else {
            TagResolver failurePlaceholders = TagResolver.resolver(
                    Placeholder.parsed("prefix", messageConfig.prefix),
                    Placeholder.parsed("error", "Check console for structural validation errors."));
            Component failureMessage = MINI_MESSAGE.deserialize(messageConfig.commands.reloadFail, failurePlaceholders);
            commandSender.sendMessage(failureMessage);
        }
    }
}
