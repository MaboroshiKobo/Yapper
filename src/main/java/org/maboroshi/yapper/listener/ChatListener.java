package org.maboroshi.yapper.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.hook.TownyHook;
import org.maboroshi.yapper.renderer.YapperRenderer;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private final Yapper plugin;
    private final ConfigManager config;

    private final Map<UUID, String> activeChannels = new ConcurrentHashMap<>();
    private final Map<UUID, String> temporaryChannelOverrides = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastUsedMessageChannels = new ConcurrentHashMap<>();

    public ChatListener(Yapper plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();

        activeChannels.remove(playerUuid);
        temporaryChannelOverrides.remove(playerUuid);
        lastUsedMessageChannels.remove(playerUuid);

        Log.debug("Cleared active session maps for UUID: " + playerUuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderUuid = sender.getUniqueId();

        String channelId = temporaryChannelOverrides.remove(senderUuid);
        Log.debug("Override removed = " + channelId);

        if (channelId == null) {
            channelId = activeChannels.getOrDefault(senderUuid, "global");
        }
        Log.debug("Chat event channel = " + channelId);

        ChannelTemplate channelTemplate = config.getChannel(channelId);
        if (channelTemplate == null) {
            Log.error("Channel '" + channelId + "' not found for player " + sender.getName()
                    + ", falling back to global.");
            channelId = "global";
            channelTemplate = config.getChannel("global");
        }

        if (channelTemplate == null) return;

        if (!sender.hasPermission("yapper.channel." + channelId + ".send")) {
            lastUsedMessageChannels.remove(senderUuid);
            event.setCancelled(true);

            sender.sendRichMessage("<red>You do not have permission to speak in the <yellow>"
                    + channelTemplate.name
                    + "</yellow><red> channel.</red>");

            Log.debug("Cancelled chat from " + sender.getName() + " in channel '" + channelId
                    + "' due to missing permission.");
            return;
        }

        lastUsedMessageChannels.put(senderUuid, channelId);
        Log.debug("Stored lastMessageChannel = " + channelId);

        String targetChannelId = channelId;
        ChannelTemplate targetTemplate = channelTemplate;

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player viewer)) {
                return false;
            }
            return !viewer.hasPermission("yapper.channel." + targetChannelId + ".view");
        });

        if (targetChannelId.startsWith("towny-")) {
            event.viewers().removeIf(audience -> {
                if (!(audience instanceof Player viewer)) {
                    return false;
                }
                return !TownyHook.isVisibleTo(sender, viewer, targetChannelId);
            });
        }

        if (targetTemplate.radius > 0) {
            double radiusSquared = targetTemplate.radius * targetTemplate.radius;
            Location senderLocation = sender.getLocation();
            World senderWorld = senderLocation.getWorld();

            event.viewers().removeIf(audience -> {
                if (!(audience instanceof Player viewer)) {
                    return false;
                }
                return !viewer.getWorld().equals(senderWorld)
                        || senderLocation.distanceSquared(viewer.getLocation()) > radiusSquared;
            });
        }

        event.renderer(ChatRenderer.viewerUnaware(new YapperRenderer(plugin, targetTemplate)));
    }

    public void setPlayerChannel(Player player, String channelId) {
        activeChannels.put(player.getUniqueId(), channelId.toLowerCase());
    }

    public String getPlayerChannel(Player player) {
        return activeChannels.getOrDefault(player.getUniqueId(), "global");
    }

    public String getCurrentMessageChannel(Player player) {
        return lastUsedMessageChannels.getOrDefault(player.getUniqueId(), getPlayerChannel(player));
    }

    public void clearCurrentMessageChannel(Player player) {
        lastUsedMessageChannels.remove(player.getUniqueId());
    }

    public void setTempChannelOverride(Player player, String channelId) {
        temporaryChannelOverrides.put(player.getUniqueId(), channelId.toLowerCase());
    }
}
