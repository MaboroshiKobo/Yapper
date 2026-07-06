package org.maboroshi.yapper.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.ChannelTemplate.ChannelFormat;
import org.maboroshi.yapper.hook.TownyHook;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final Yapper plugin;
    private final ConfigManager config;
    private final Map<UUID, String> playerActiveChannel = new ConcurrentHashMap<>();

    public ChatListener(Yapper plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String channelId = playerActiveChannel.getOrDefault(sender.getUniqueId(), "global");
        String rawText = PlainTextComponentSerializer.plainText().serialize(event.message());

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> broadcastToChannel(sender, channelId, rawText));
    }

    public void broadcastToChannel(Player sender, String channelId, String rawMessageText) {
        ChannelTemplate channel = config.getChannel(channelId);

        if (channel == null) {
            channel = config.getChannel("global");
            Log.error(
                    "Channel '" + channelId + "' not found for player " + sender.getName() + ", using global channel.");
        }

        if (channel == null) {
            return;
        }

        if (!channelId.equalsIgnoreCase("global") && !sender.hasPermission("yapper.channel." + channelId + ".send")) {
            setPlayerChannel(sender, "global");
            channel = config.getChannel("global");

            if (channel == null) {
                return;
            }

            sender.sendRichMessage(
                    "<red>You do not have permission to speak in that channel. Switched to global.</red>");
        }

        final ChannelTemplate finalChannel = channel;

        ChannelFormat matchedFormat = null;
        for (ChannelFormat f : channel.formats.values()) {
            if (f.permission == null || f.permission.isEmpty() || sender.hasPermission(f.permission)) {
                matchedFormat = f;
                break;
            }
        }

        if (matchedFormat == null) {
            return;
        }

        String rawFormatString = matchedFormat.format;
        String parsedFormatString = plugin.getYapperUtils().process(sender, rawFormatString);

        List<TagResolver> layoutTags =
                new ArrayList<>(config.getMainConfig().components.size());
        for (Map.Entry<String, String> entry : config.getMainConfig().components.entrySet()) {
            String papiLine = plugin.getYapperUtils().process(sender, entry.getValue());
            layoutTags.add(Placeholder.parsed(entry.getKey(), papiLine));
        }

        List<TagResolver> chatMacros =
                new ArrayList<>(config.getMainConfig().macros.size());
        for (Map.Entry<String, String> entry : config.getMainConfig().macros.entrySet()) {
            String macroName = entry.getKey();
            String macroValue = entry.getValue();

            if (sender.hasPermission("yapper.macro." + macroName)) {
                chatMacros.add(TagResolver.resolver(macroName, (queue, context) -> {
                    String papiMacro = plugin.getYapperUtils().process(sender, macroValue);
                    Component macroComponent = MM.deserialize(papiMacro);
                    return Tag.inserting(macroComponent);
                }));
            }
        }

        TagResolver macroSystem = TagResolver.resolver(chatMacros);
        Component formattedContent =
                plugin.getYapperUtils().getEffectiveParser(sender).deserialize(rawMessageText, macroSystem);

        TagResolver customTags = TagResolver.resolver(layoutTags);
        TagResolver defaultTags = TagResolver.resolver(
                Placeholder.parsed("name", sender.getName()),
                Placeholder.component("displayname", sender.displayName()),
                Placeholder.parsed("channel", finalChannel.name),
                Placeholder.parsed("world", sender.getWorld().getName()),
                Placeholder.component("message", formattedContent));

        TagResolver allTags = TagResolver.resolver(customTags, defaultTags);
        Component finalChatComponent = MM.deserialize(parsedFormatString, allTags);

        Collection<Player> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());

        recipients.removeIf(viewer -> !viewer.hasPermission("yapper.channel." + channelId + ".view"));

        if (channelId.startsWith("towny-")) {
            recipients.removeIf(viewer -> !TownyHook.isVisibleTo(sender, viewer, channelId));
        }

        if (finalChannel.radius > 0) {
            double radiusSquared = finalChannel.radius * finalChannel.radius;
            var senderLocation = sender.getLocation();
            recipients.removeIf(viewer -> !viewer.getWorld().equals(senderLocation.getWorld())
                    || senderLocation.distanceSquared(viewer.getLocation()) > radiusSquared);
        }

        for (Player recipient : recipients) {
            recipient.sendMessage(finalChatComponent);
        }

        Bukkit.getConsoleSender().sendMessage(finalChatComponent);
    }

    public void setPlayerChannel(Player player, String channelId) {
        playerActiveChannel.put(player.getUniqueId(), channelId.toLowerCase());
    }
}
