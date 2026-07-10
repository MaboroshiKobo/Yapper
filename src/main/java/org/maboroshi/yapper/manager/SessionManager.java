package org.maboroshi.yapper.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public class SessionManager {
    private final Map<UUID, String> activeChannels = new ConcurrentHashMap<>();
    private final Map<UUID, String> temporaryChannelOverrides = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastUsedMessageChannels = new ConcurrentHashMap<>();

    public void clearSession(UUID playerUuid) {
        activeChannels.remove(playerUuid);
        temporaryChannelOverrides.remove(playerUuid);
        lastUsedMessageChannels.remove(playerUuid);
    }

    public String resolveTargetChannel(UUID playerUuid) {
        String temp = temporaryChannelOverrides.remove(playerUuid);
        return temp != null ? temp : activeChannels.getOrDefault(playerUuid, "global");
    }

    public void setPlayerChannel(Player player, String channelId) {
        activeChannels.put(player.getUniqueId(), channelId.toLowerCase());
    }

    public void setTempChannelOverride(Player player, String channelId) {
        temporaryChannelOverrides.put(player.getUniqueId(), channelId.toLowerCase());
    }

    public void updateLastUsedChannel(UUID playerUuid, String channelId) {
        lastUsedMessageChannels.put(playerUuid, channelId);
    }

    public String getCurrentMessageChannel(Player player) {
        return lastUsedMessageChannels.getOrDefault(
                player.getUniqueId(), activeChannels.getOrDefault(player.getUniqueId(), "global"));
    }

    public void clearCurrentMessageChannel(Player player) {
        lastUsedMessageChannels.remove(player.getUniqueId());
    }
}
