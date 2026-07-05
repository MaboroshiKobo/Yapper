package org.maboroshi.yapper.hook;

import org.bukkit.OfflinePlayer;

public interface PlaceholderHook {
    String parse(OfflinePlayer player, String text);
}
