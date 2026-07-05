package org.maboroshi.yapper.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.ChannelTemplate.ChannelFormat;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private final Yapper plugin;
    private final ConfigManager config;
    private final Map<UUID, String> playerActiveChannel = new ConcurrentHashMap<>();
    private final Map<Integer, MiniMessage> parserCache = new ConcurrentHashMap<>();

    public ChatListener(Yapper plugin) {
        this.plugin = plugin;
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

        String rawFormatString = matchedFormat.format;
        String processedFormat = plugin.getYapperUtils().process(sender, rawFormatString);
        String rawText = PlainTextComponentSerializer.plainText().serialize(event.message());
        Component formattedContent = getEffectiveParser(sender).deserialize(rawText);

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            TagResolver chatResolvers = TagResolver.resolver(
                    Placeholder.component("player", sourceDisplayName),
                    Placeholder.component("message", formattedContent));

            return MiniMessage.miniMessage().deserialize(processedFormat, chatResolvers);
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

    private MiniMessage getEffectiveParser(Player player) {
        int mask = 0;
        if (player.hasPermission("yapper.chat.color")) mask |= 1;
        if (player.hasPermission("yapper.chat.decorations")) mask |= 2;
        if (player.hasPermission("yapper.chat.gradient")) mask |= 4;
        if (player.hasPermission("yapper.chat.rainbow")) mask |= 8;

        return parserCache.computeIfAbsent(mask, k -> {
            List<TagResolver> tags = new ArrayList<>();
            if ((k & 1) != 0) tags.add(StandardTags.color());
            if ((k & 2) != 0) tags.add(StandardTags.decorations());
            if ((k & 4) != 0) tags.add(StandardTags.gradient());
            if ((k & 8) != 0) tags.add(StandardTags.rainbow());
            return MiniMessage.builder().tags(TagResolver.resolver(tags)).build();
        });
    }

    public void setPlayerChannel(Player player, String channelId) {
        this.playerActiveChannel.put(player.getUniqueId(), channelId.toLowerCase());
    }
}