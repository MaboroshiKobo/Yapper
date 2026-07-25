package org.maboroshi.yapper.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {
    public static NamespacedKey ACTIVE_CHANNEL;
    public static NamespacedKey HIDDEN_CHANNELS;

    private Keys() {}

    public static void init(Plugin plugin) {
        ACTIVE_CHANNEL = new NamespacedKey(plugin, "active_channel");
        HIDDEN_CHANNELS = new NamespacedKey(plugin, "hidden_channels");
    }
}
