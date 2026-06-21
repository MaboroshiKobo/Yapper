package org.maboroshi.yapper;

import org.bukkit.plugin.java.JavaPlugin;

public final class Yapper extends JavaPlugin {
    private static Yapper plugin;

    @Override
    public void onEnable() {
        plugin = this;
    }

    @Override
    public void onDisable() {}

    public static Yapper getPlugin() {
        return plugin;
    }
}
