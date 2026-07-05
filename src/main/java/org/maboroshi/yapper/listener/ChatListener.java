package org.maboroshi.yapper.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.ChannelTemplate.ChannelFormat;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private final ConfigManager config;

    private final Map<UUID, String> playerActiveChannel = new HashMap<>();

    public ChatListener(Yapper plugin) {
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        String channelId = playerActiveChannel.getOrDefault(sender.getUniqueId(), "default");
        ChannelTemplate channel = config.getChannel(channelId);

        if (channel == null) {
            channel = config.getChannel("default");
            Log.error("Channel not found for player " + sender.getName() + ", using default channel.");
        }

        if (channel == null) return;

        ChannelFormat matchedFormat = null;
        for (ChannelFormat f : channel.formats.values()) {
            if (f.permission == null || f.permission.isEmpty() || sender.hasPermission(f.permission)) {
                matchedFormat = f;
                break;
            }
        }

        if (matchedFormat == null) return;

        final String rawFormatString = matchedFormat.format;

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            String plainMessage = MiniMessage.miniMessage().serialize(message);

            Component formattedContent = parsePlayerContent(source, plainMessage);

            TagResolver chatResolvers = TagResolver.resolver(
                    Placeholder.component("player", sourceDisplayName),
                    Placeholder.component("message", formattedContent));

            return MiniMessage.miniMessage().deserialize(rawFormatString, chatResolvers);
        });

        if (channel.radius > 0) {
            double radiusSquared = channel.radius * channel.radius;
            event.viewers().removeIf(viewer -> {
                if (viewer instanceof Player recipient) {
                    if (!recipient.getWorld().equals(sender.getWorld())) return true;
                    return sender.getLocation().distanceSquared(recipient.getLocation()) > radiusSquared;
                }
                return false;
            });
        }
    }

    private Component parsePlayerContent(Player player, String rawText) {
        List<TagResolver> permittedTags = new ArrayList<>();

        if (player.hasPermission("yapper.chat.color")) {
            permittedTags.add(StandardTags.color());
        }

        if (player.hasPermission("yapper.chat.decorations")) {
            permittedTags.add(StandardTags.decorations());
        }

        if (player.hasPermission("yapper.chat.gradient")) {
            permittedTags.add(StandardTags.gradient());
        }

        if (player.hasPermission("yapper.chat.rainbow")) {
            permittedTags.add(StandardTags.rainbow());
        }

        MiniMessage restrictedParser =
                MiniMessage.builder().tags(TagResolver.resolver(permittedTags)).build();

        return restrictedParser.deserialize(rawText);
    }

    public void setPlayerChannel(Player player, String channelId) {
        this.playerActiveChannel.put(player.getUniqueId(), channelId.toLowerCase());
    }
}
