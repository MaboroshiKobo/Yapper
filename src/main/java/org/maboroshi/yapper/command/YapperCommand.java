package org.maboroshi.yapper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.MessageConfig;
import org.maboroshi.yapper.hook.TownyHook;
import org.maboroshi.yapper.manager.MacroProcessor;
import org.maboroshi.yapper.renderer.ChannelRenderer;

public class YapperCommand {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;

    public YapperCommand(Yapper plugin) {
        this.plugin = plugin;
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

        commandSender.sendMessage(MINI_MESSAGE.deserialize(
                "<prefix> Running version <yellow><version></yellow> developed by <gold><authors></gold>.",
                aboutPlaceholders));
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
                "<prefix> <gray>Available Chat Channels:</gray>", Placeholder.parsed("prefix", msgConfig.prefix)));

        Map<String, ChannelTemplate> channels = plugin.getConfigManager().getChannels();
        for (Map.Entry<String, ChannelTemplate> entry : channels.entrySet()) {
            String channelId = entry.getKey();
            ChannelTemplate template = entry.getValue();

            if (sender.hasPermission("yapper.channel." + channelId + ".view")) {
                String line = "<gray>- </gray><yellow>" + template.name + "</yellow> <dark_gray>(" + channelId
                        + ")</dark_gray> "
                        + "<click:run_command:'/yapper channel " + channelId
                        + "'><hover:show_text:'<gray>Click to switch to </gray><yellow>" + template.name
                        + "</yellow>'><green>[Switch]</green></hover></click> "
                        + "<click:run_command:'/yapper channel hide " + channelId
                        + "'><hover:show_text:'<gray>Click to toggle visibility for </gray><yellow>" + template.name
                        + "</yellow>'><red>[Hide/Show]</red></hover></click>";
                sender.sendMessage(MINI_MESSAGE.deserialize(line));
            }
        }
    }

    @Command("yapper channel hide <channelId>")
    @Permission("yapper.command.channel.hide")
    public void onChannelHide(CommandSourceStack source, @Argument("channelId") String channelId) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<red>Only players can hide chat channels.</red>"));
            return;
        }

        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>Channel <yellow>" + channelId + "</yellow> does not exist.</red>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        boolean hidden = plugin.getSessionManager().toggleChannelHide(player, channelId);
        if (hidden) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <gray>Hidden channel <yellow>" + channel.name
                            + "</yellow>. You will no longer see messages from it.</gray>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
        } else {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <gray>Unhidden channel <yellow>" + channel.name
                            + "</yellow>. You will now see messages from it again.</gray>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
        }
    }

    @Command("yapper channel info [channelId]")
    @Permission("yapper.command.channel.info")
    public void onChannelInfo(CommandSourceStack source, @Argument("channelId") String channelId) {
        CommandSender sender = source.getSender();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        String targetId = channelId;
        if (targetId == null || targetId.isEmpty()) {
            if (sender instanceof Player player) {
                targetId = plugin.getSessionManager().resolveTargetChannel(player.getUniqueId());
            } else {
                targetId = "global";
            }
        }

        ChannelTemplate channel = plugin.getConfigManager().getChannel(targetId);
        if (channel == null) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>Channel <yellow>" + targetId + "</yellow> does not exist.</red>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        String radiusText = channel.radius > 0 ? channel.radius + " blocks" : "Global (Infinite)";
        String visibilityStatus = "Visible";
        if (sender instanceof Player player
                && plugin.getSessionManager().isChannelHidden(player.getUniqueId(), targetId)) {
            visibilityStatus = "Hidden";
        }

        sender.sendMessage(MINI_MESSAGE.deserialize(
                "<prefix> <gray>Channel Info for <yellow>" + channel.name + "</yellow> <dark_gray>(" + targetId
                        + ")</dark_gray>:</gray>",
                Placeholder.parsed("prefix", msgConfig.prefix)));
        sender.sendMessage(MINI_MESSAGE.deserialize("<gray> - Radius: <yellow>" + radiusText + "</yellow></gray>"));
        sender.sendMessage(
                MINI_MESSAGE.deserialize("<gray> - Status: <yellow>" + visibilityStatus + "</yellow></gray>"));
    }

    @Command("yapper channel <channelId> [message]")
    @Permission("yapper.command.channel.use")
    public void onChannelAction(
            CommandSourceStack source,
            @Argument("channelId") String channelId,
            @Argument("message") String[] messageArgs) {

        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MINI_MESSAGE.deserialize("<red>Only players can use chat channels.</red>"));
            return;
        }

        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
        ChannelTemplate channel = plugin.getConfigManager().getChannel(channelId);

        if (channel == null) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>Channel <yellow>" + channelId + "</yellow> does not exist.</red>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        if (!player.hasPermission("yapper.channel." + channelId + ".send")) {
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <red>You do not have permission to speak in <yellow>" + channel.name + "</yellow>.</red>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        if (messageArgs == null || messageArgs.length == 0) {
            plugin.getSessionManager().setPlayerChannel(player, channelId);
            player.sendMessage(MINI_MESSAGE.deserialize(
                    "<prefix> <gray>Switched active channel to <yellow>" + channel.name + "</yellow>.</gray>",
                    Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        String rawMessage = String.join(" ", messageArgs);
        dispatchQuickMessage(player, channelId, channel, rawMessage);
    }

    private void dispatchQuickMessage(Player sender, String channelId, ChannelTemplate template, String rawMessage) {
        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        final TagResolver papiResolver;

        if (placeholderApiEnabled) {
            papiResolver = TagResolver.resolver("papi", (args, context) -> {
                if (!args.hasNext()) return Tag.selfClosingInserting(Component.empty());

                List<String> argList = new ArrayList<>();
                while (args.hasNext()) argList.add(args.pop().value());

                String papiQuery = String.join(":", argList);
                String papiText = PlaceholderAPI.setPlaceholders(sender, "%" + papiQuery + "%");

                Component tempComponent;
                if (papiText.contains("§")) {
                    tempComponent = LegacyComponentSerializer.legacySection().deserialize(papiText);
                } else {
                    try {
                        tempComponent = MINI_MESSAGE.deserialize(papiText);
                    } catch (Exception e) {
                        tempComponent = Component.text(papiText);
                    }
                }

                final Component finalComponent = tempComponent;
                String plainText = PlainTextComponentSerializer.plainText().serialize(finalComponent);
                if (plainText.isEmpty()) {
                    return Tag.styling(builder -> builder.merge(finalComponent.style()));
                }

                return Tag.selfClosingInserting(finalComponent);
            });
        } else {
            papiResolver = TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty()));
        }

        MacroProcessor processor = new MacroProcessor(plugin);
        List<TagResolver> playerMsgResolvers =
                processor.buildMacroResolvers(sender, papiResolver, placeholderApiEnabled);

        MiniMessage playerChatParser = plugin.getFormatUtils().getChatParser(sender);
        Component formattedPlayerMessage =
                playerChatParser.deserialize(rawMessage, TagResolver.resolver(playerMsgResolvers));

        ChannelRenderer renderer = new ChannelRenderer(plugin, template);
        Component finalOutput = renderer.render(sender, sender.displayName(), formattedPlayerMessage);

        double radiusSquared = template.radius * template.radius;
        Location senderLocation = sender.getLocation();
        World senderWorld = senderLocation.getWorld();

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.hasPermission("yapper.channel." + channelId + ".view")) continue;
            if (plugin.getSessionManager().isChannelHidden(viewer.getUniqueId(), channelId)) continue;

            if (channelId.startsWith("towny-") && !TownyHook.isVisibleTo(sender, viewer, channelId)) {
                continue;
            }

            if (template.radius > 0) {
                if (!viewer.getWorld().equals(senderWorld)
                        || senderLocation.distanceSquared(viewer.getLocation()) > radiusSquared) {
                    continue;
                }
            }

            viewer.sendMessage(finalOutput);
        }
    }
}
