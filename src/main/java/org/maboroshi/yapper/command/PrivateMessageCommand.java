package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.MessageConfig;

public class PrivateMessageCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;

    public PrivateMessageCommand(Yapper plugin) {
        this.plugin = plugin;
    }

    @Suggestions("players")
    public List<String> playerSuggestions(CommandContext<CommandSourceStack> context, String input) {
        CommandSender sender = context.sender().getSender();
        List<String> suggestions = new ArrayList<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player player && !player.canSee(onlinePlayer)) {
                continue;
            }
            suggestions.add(onlinePlayer.getName());
        }
        return suggestions;
    }

    @Command("yapper whisper <recipient> <message>")
    @Permission("yapper.command.whisper")
    public void onWhisper(
            CommandSourceStack source,
            @Argument(value = "recipient", suggestions = "players") String recipientName,
            @Argument("message") String[] messageArgs) {

        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        Player recipient = Bukkit.getPlayer(recipientName);
        if (recipient == null || !recipient.isOnline() || !player.canSee(recipient)) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.privateMessages.playerNotFound, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        String rawMessage = String.join(" ", messageArgs);
        plugin.getPrivateMessageManager().sendPrivateMessage(player, recipient, rawMessage);
    }

    @Command("yapper reply <message>")
    @Permission("yapper.command.reply")
    public void onReply(CommandSourceStack source, @Argument("message") String[] messageArgs) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        String rawMessage = String.join(" ", messageArgs);
        plugin.getPrivateMessageManager().reply(player, rawMessage);
    }
}
