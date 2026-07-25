package org.maboroshi.yapper.manager;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.maboroshi.yapper.util.Keys;

public class SessionManager {
    private final Map<UUID, String> activeChannels = new ConcurrentHashMap<>();
    private final Map<UUID, String> temporaryChannelOverrides = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastUsedMessageChannels = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> hiddenChannels = new ConcurrentHashMap<>();

    public void loadSession(Player player) {
        UUID playerUuid = player.getUniqueId();
        clearSession(playerUuid);
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        String active = pdc.get(Keys.ACTIVE_CHANNEL, PersistentDataType.STRING);
        if (active != null && !active.isEmpty()) {
            activeChannels.put(playerUuid, active);
        }

        String hiddenRaw = pdc.get(Keys.HIDDEN_CHANNELS, PersistentDataType.STRING);
        if (hiddenRaw != null && !hiddenRaw.isEmpty()) {
            Set<String> set = ConcurrentHashMap.newKeySet();
            set.addAll(Arrays.asList(hiddenRaw.split(",")));
            hiddenChannels.put(playerUuid, set);
        }
    }

    public void clearSession(UUID playerUuid) {
        activeChannels.remove(playerUuid);
        temporaryChannelOverrides.remove(playerUuid);
        lastUsedMessageChannels.remove(playerUuid);
        hiddenChannels.remove(playerUuid);
    }

    public String resolveTargetChannel(UUID playerUuid) {
        String temp = temporaryChannelOverrides.remove(playerUuid);
        return temp != null ? temp : activeChannels.getOrDefault(playerUuid, "global");
    }

    public void setPlayerChannel(Player player, String channelId) {
        String normalized = channelId.toLowerCase(Locale.ROOT);
        activeChannels.put(player.getUniqueId(), normalized);
        player.getPersistentDataContainer().set(Keys.ACTIVE_CHANNEL, PersistentDataType.STRING, normalized);
    }

    public void setTempChannelOverride(Player player, String channelId) {
        temporaryChannelOverrides.put(player.getUniqueId(), channelId.toLowerCase(Locale.ROOT));
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

    public boolean toggleChannelHide(Player player, String channelId) {
        Set<String> hidden = hiddenChannels.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        String normalized = channelId.toLowerCase(Locale.ROOT);
        boolean isNowHidden;
        if (hidden.contains(normalized)) {
            hidden.remove(normalized);
            isNowHidden = false;
        } else {
            hidden.add(normalized);
            isNowHidden = true;
        }

        if (hidden.isEmpty()) {
            player.getPersistentDataContainer().remove(Keys.HIDDEN_CHANNELS);
        } else {
            player.getPersistentDataContainer()
                    .set(Keys.HIDDEN_CHANNELS, PersistentDataType.STRING, String.join(",", hidden));
        }

        return isNowHidden;
    }

    public boolean isChannelHidden(UUID playerUuid, String channelId) {
        Set<String> hidden = hiddenChannels.get(playerUuid);
        return hidden != null && hidden.contains(channelId.toLowerCase(Locale.ROOT));
    }
}
