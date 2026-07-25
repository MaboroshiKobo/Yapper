package org.maboroshi.yapper;

import github.scarsz.discordsrv.DiscordSRV;
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
import org.maboroshi.yapper.hook.DiscordSRVHook;
import org.maboroshi.yapper.listener.ChatListener;
import org.maboroshi.yapper.listener.InventoryListener;
import org.maboroshi.yapper.manager.SessionManager;
import org.maboroshi.yapper.util.FormatUtils;
import org.maboroshi.yapper.util.Keys;
import org.maboroshi.yapper.util.Log;

public final class Yapper extends JavaPlugin {
    private static Yapper plugin;

    private ConfigManager configManager;
    private FormatUtils formatUtils;
    private SessionManager sessionManager;
    private PaperCommandManager<CommandSourceStack> commandManager;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        plugin = this;
        Keys.init(this);
        this.configManager = new ConfigManager(getDataFolder());
        this.formatUtils = new FormatUtils();
        this.sessionManager = new SessionManager();

        Log.init(
                getComponentLogger(),
                () -> configManager != null
                        && configManager.getMainConfig() != null
                        && configManager.getMainConfig().debug);

        configManager.loadConfig();
        configManager.loadMessages();

        this.chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);

        AnnotationParser<CommandSourceStack> annotationParser =
                new AnnotationParser<>(commandManager, CommandSourceStack.class);

        annotationParser.parse(new YapperCommand(this));

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.subscribe(new DiscordSRVHook(this));
        }

        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this, 32126);

        Log.info("Yapper has been enabled!");
    }

    public boolean reload() {
        configManager.loadConfig();
        configManager.loadMessages();
        for (Player player : Bukkit.getOnlinePlayers()) {
            sessionManager.loadSession(player);
            player.updateCommands();
        }
        return true;
    }

    @Override
    public void onDisable() {
        Log.info("Yapper has been disabled!");
        plugin = null;
    }

    public static Yapper getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public FormatUtils getFormatUtils() {
        return formatUtils;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
}
