package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.MessageConfig;

public class YapperCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;

    public YapperCommand(Yapper plugin) {
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

    @Command("yapper")
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

    @Command("yapper channel [list]")
    @Permission("yapper.command.channel.list")
    public void onChannelList(CommandSourceStack source) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        sender.sendMessage(MINI_MESSAGE.deserialize(
                msgConfig.channels.listHeader, Placeholder.parsed("prefix", msgConfig.prefix)));

        Map<String, ChannelTemplate> channels = plugin.getConfigManager().getChannels();
        for (Map.Entry<String, ChannelTemplate> entry : channels.entrySet()) {
            String channelId = entry.getKey();
            ChannelTemplate template = entry.getValue();

            if (sender.hasPermission("yapper.channel." + channelId + ".view")) {
                TagResolver placeholders = TagResolver.resolver(
                        Placeholder.parsed("prefix", msgConfig.prefix),
                        Placeholder.parsed("channel", template.name),
                        Placeholder.parsed("channel_id", channelId));
                sender.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.listItem, placeholders));
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
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.channels.notFound,
                    TagResolver.resolver(
                            Placeholder.parsed("prefix", msgConfig.prefix),
                            Placeholder.parsed("channel_id", channelId))));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".view")) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.channels.noPermissionView,
                    TagResolver.resolver(
                            Placeholder.parsed("prefix", msgConfig.prefix),
                            Placeholder.parsed("channel", channel.name),
                            Placeholder.parsed("channel_id", channelId))));
            return;
        }

        boolean hidden = plugin.getSessionManager().toggleChannelHide(player, channelId);
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("prefix", msgConfig.prefix),
                Placeholder.parsed("channel", channel.name),
                Placeholder.parsed("channel_id", channelId));

        if (hidden) {
            player.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.hideSuccess, placeholders));
        } else {
            player.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.showSuccess, placeholders));
        }
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
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.channels.notFound,
                    TagResolver.resolver(
                            Placeholder.parsed("prefix", msgConfig.prefix),
                            Placeholder.parsed("channel_id", targetId))));
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

        sender.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.infoHeader, basePlaceholders));
        sender.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.infoRadius, basePlaceholders));
        sender.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.infoStatus, basePlaceholders));
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
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.commands.playerOnly, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.channels.notFound,
                    TagResolver.resolver(
                            Placeholder.parsed("prefix", msgConfig.prefix),
                            Placeholder.parsed("channel_id", channelId))));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".send")) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.channels.noPermissionSend,
                    TagResolver.resolver(
                            Placeholder.parsed("prefix", msgConfig.prefix),
                            Placeholder.parsed("channel", channel.name),
                            Placeholder.parsed("channel_id", channelId))));
            return;
        }

        if (messageArgs == null || messageArgs.length == 0) {
            plugin.getSessionManager().setPlayerChannel(player, channelId);
            TagResolver placeholders = TagResolver.resolver(
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channel.name),
                    Placeholder.parsed("channel_id", channelId));
            player.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.switchChannel, placeholders));
            return;
        }

        String rawMessage = String.join(" ", messageArgs);
        plugin.getSessionManager().setTempChannelOverride(player, channelId);
        player.chat(rawMessage);
    }
}
