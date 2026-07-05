package org.maboroshi.yapper.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

public class PlaceholderAPIHook implements PlaceholderHook {
    @Override
    public String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
