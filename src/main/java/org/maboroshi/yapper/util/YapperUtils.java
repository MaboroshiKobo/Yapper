package org.maboroshi.yapper.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.maboroshi.yapper.hook.PlaceholderAPIHook;
import org.maboroshi.yapper.hook.PlaceholderHook;

public class YapperUtils {
    private final PlaceholderHook hook;

    public YapperUtils() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.hook = new PlaceholderAPIHook();
        } else {
            this.hook = (player, text) -> text;
        }
    }

    public String process(OfflinePlayer player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return hook.parse(player, text);
    }
}
