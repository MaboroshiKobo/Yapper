package org.maboroshi.yapper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.maboroshi.yapper.command.YapperCommand;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.listener.ChatListener;
import org.maboroshi.yapper.util.Log;

public final class Yapper extends JavaPlugin {
    private static Yapper plugin;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;
        this.configManager = new ConfigManager(this, getDataFolder());
        Log.init(getComponentLogger(), () -> configManager != null && configManager.getMainConfig().debug);

        try {
            configManager.loadConfig();
            configManager.loadMessages();
        } catch (Exception e) {
            Log.error("Failed to load configuration: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        PaperCommandManager<CommandSourceStack> commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);

        AnnotationParser<CommandSourceStack> annotationParser =
                new AnnotationParser<>(commandManager, CommandSourceStack.class);

        annotationParser.parse(new YapperCommand(this));

        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this, 32126);

        Log.info("Yapper has been enabled!");
    }

    public boolean reload() {
        try {
            configManager.loadConfig();
            configManager.loadMessages();

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }

            return true;
        } catch (Exception e) {
            Log.error("Failed to reload configuration: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onDisable() {
        Log.info("Yapper has been disabled!");
    }

    public static Yapper getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
