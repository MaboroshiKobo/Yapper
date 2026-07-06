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
import org.maboroshi.yapper.config.settings.MessageConfig;

public class YapperCommand {
    private final Yapper plugin;
    private final MiniMessage MM = MiniMessage.miniMessage();

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
            MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
            TagResolver tags = TagResolver.resolver(Placeholder.parsed("prefix", msgConfig.prefix));
            Component message = MM.deserialize(msgConfig.commands.reloadSuccess, tags);
            sender.sendMessage(message);
        } else {
            MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
            TagResolver tags = TagResolver.resolver(
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("error", "Check console for details."));
            Component message = MM.deserialize(msgConfig.commands.reloadFail, tags);
            sender.sendMessage(message);
        }
    }
}
