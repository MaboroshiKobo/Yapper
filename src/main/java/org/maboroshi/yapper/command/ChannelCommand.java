package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ChannelTemplate;
import org.maboroshi.yapper.config.MessageConfig;

public class ChannelCommand {
    private final Yapper plugin;

    public ChannelCommand(Yapper plugin) {
        this.plugin = plugin;
    }

    @Suggestions("channels")
    public List<String> channelSuggestions(CommandContext<CommandSourceStack> context, String input) {
        CommandSender sender = context.sender().getSender();
        List<String> suggestions = new ArrayList<>();

        for (Map.Entry<String, ChannelTemplate> entry :
                plugin.getConfigManager().getChannels().entrySet()) {
            String channelId = entry.getKey();
            if (sender.hasPermission("yapper.channel." + channelId + ".view")) {
                suggestions.add(channelId);
            }
        }

        return suggestions;
    }

    @Command("yapper channels")
    @Command("yapper channel list")
    @Permission("yapper.command.channel.list")
    public void onChannelList(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        sender.sendRichMessage(msgConfig.channels.listHeader, Placeholder.parsed("prefix", msgConfig.prefix));

        Map<String, ChannelTemplate> channels = plugin.getConfigManager().getChannels();
        for (Map.Entry<String, ChannelTemplate> entry : channels.entrySet()) {
            String channelId = entry.getKey();
            ChannelTemplate template = entry.getValue();

            if (sender.hasPermission("yapper.channel." + channelId + ".view")) {
                sender.sendRichMessage(
                        msgConfig.channels.listItem,
                        Placeholder.parsed("prefix", msgConfig.prefix),
                        Placeholder.parsed("channel", template.name),
                        Placeholder.parsed("channel_id", channelId));
            }
        }
    }

    @Command("yapper channel hide <channelId>")
    @Permission("yapper.command.channel.hide")
    public void onChannelHide(
            CommandSourceStack source, @Argument(value = "channelId", suggestions = "channels") String channelId) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!(sender instanceof Player player)) {
            sender.sendRichMessage(msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix));
            return;
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            sender.sendRichMessage(
                    msgConfig.channels.notFound,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".view")) {
            player.sendRichMessage(
                    msgConfig.channels.noPermissionView,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channel.name),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (!plugin.getSessionManager().isChannelHidden(player.getUniqueId(), channelId)) {
            plugin.getSessionManager().toggleChannelHide(player, channelId);
        }

        player.sendRichMessage(
                msgConfig.channels.hideSuccess,
                Placeholder.parsed("prefix", msgConfig.prefix),
                Placeholder.parsed("channel", channel.name),
                Placeholder.parsed("channel_id", channelId));
    }

    @Command("yapper channel show <channelId>")
    @Permission("yapper.command.channel.show")
    public void onChannelShow(
            CommandSourceStack source, @Argument(value = "channelId", suggestions = "channels") String channelId) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!(sender instanceof Player player)) {
            sender.sendRichMessage(msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix));
            return;
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendRichMessage(
                    msgConfig.channels.notFound,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".view")) {
            player.sendRichMessage(
                    msgConfig.channels.noPermissionView,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channel.name),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (plugin.getSessionManager().isChannelHidden(player.getUniqueId(), channelId)) {
            plugin.getSessionManager().toggleChannelHide(player, channelId);
        }

        player.sendRichMessage(
                msgConfig.channels.showSuccess,
                Placeholder.parsed("prefix", msgConfig.prefix),
                Placeholder.parsed("channel", channel.name),
                Placeholder.parsed("channel_id", channelId));
    }

    @Command("yapper channel info [channelId]")
    @Permission("yapper.command.channel.info")
    public void onChannelInfo(
            CommandSourceStack source, @Argument(value = "channelId", suggestions = "channels") String channelId) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        String targetId = channelId;
        if (targetId == null || targetId.isEmpty()) {
            if (sender instanceof Player player) {
                targetId = plugin.getSessionManager().getCurrentMessageChannel(player);
            } else {
                targetId = "global";
            }
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(targetId);
        if (channel == null) {
            sender.sendRichMessage(
                    msgConfig.channels.notFound,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel_id", targetId));
            return;
        }

        String radiusText = channel.radius > 0
                ? msgConfig.channels.radiusBlocks.replace("<radius>", String.valueOf(channel.radius))
                : msgConfig.channels.radiusInfinite;

        String visibilityStatus = msgConfig.channels.statusVisible;
        if (sender instanceof Player player
                && plugin.getSessionManager().isChannelHidden(player.getUniqueId(), targetId)) {
            visibilityStatus = msgConfig.channels.statusHidden;
        }

        TagResolver basePlaceholders = TagResolver.resolver(
                Placeholder.parsed("prefix", msgConfig.prefix),
                Placeholder.parsed("channel", channel.name),
                Placeholder.parsed("channel_id", targetId),
                Placeholder.parsed("radius", radiusText),
                Placeholder.parsed("status", visibilityStatus));

        sender.sendRichMessage(msgConfig.channels.infoHeader, basePlaceholders);
        sender.sendRichMessage(msgConfig.channels.infoRadius, basePlaceholders);
        sender.sendRichMessage(msgConfig.channels.infoStatus, basePlaceholders);
    }

    @Command("yapper channel <channelId> [message]")
    @Permission("yapper.command.channel.use")
    public void onChannelAction(
            CommandSourceStack source,
            @Argument(value = "channelId", suggestions = "channels") String channelId,
            @Argument("message") String[] messageArgs) {

        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!(sender instanceof Player player)) {
            sender.sendRichMessage(msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix));
            return;
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendRichMessage(
                    msgConfig.channels.notFound,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".send")) {
            player.sendRichMessage(
                    msgConfig.channels.noPermissionSend,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channel.name),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        if (messageArgs == null || messageArgs.length == 0) {
            plugin.getSessionManager().setPlayerChannel(player, channelId);
            player.sendRichMessage(
                    msgConfig.channels.switchChannel,
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channel.name),
                    Placeholder.parsed("channel_id", channelId));
            return;
        }

        String rawMessage = String.join(" ", messageArgs);
        plugin.getSessionManager().setTempChannelOverride(player, channelId);
        player.chat(rawMessage);
    }
}
