package org.maboroshi.yapper;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.maboroshi.yapper.command.YapperCommand;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.MessageConfig;
import org.maboroshi.yapper.listener.ChatListener;
import org.maboroshi.yapper.util.Log;
import org.maboroshi.yapper.util.YapperUtils;

public final class Yapper extends JavaPlugin {
    private static Yapper plugin;

    private ConfigManager configManager;
    private YapperUtils yapperUtils;
    private PaperCommandManager<CommandSourceStack> commandManager;
    private final Set<String> registeredChannelCommands = new HashSet<>();
    private ChatListener chatListener;
    private final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        plugin = this;
        this.configManager = new ConfigManager(getDataFolder());
        this.yapperUtils = new YapperUtils();

        Log.init(
                getComponentLogger(),
                () -> configManager != null
                        && configManager.getMainConfig() != null
                        && configManager.getMainConfig().debug);

        try {
            configManager.loadConfig();
            configManager.loadMessages();
        } catch (Exception e) {
            getComponentLogger().error("Failed to load configuration files!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        this.commandManager = PaperCommandManager.builder()
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);

        AnnotationParser<CommandSourceStack> annotationParser =
                new AnnotationParser<>(commandManager, CommandSourceStack.class);

        annotationParser.parse(new YapperCommand(this));

        this.registerChannelCommands();

        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this, 32126);

        Log.info("Yapper has been enabled!");
    }

    private void registerChannelCommands() {
        for (String channelId : configManager.getChannelIds()) {
            ChannelTemplate channel = configManager.getChannel(channelId);
            if (channel == null || channel.commands == null) continue;

            for (String alias : channel.commands) {
                String lowercaseAlias = alias.toLowerCase();
                if (registeredChannelCommands.contains(lowercaseAlias)) continue;

                var channelCommand = commandManager
                        .commandBuilder(alias)
                        .permission("yapper.channel." + channelId + ".send")
                        .optional("message", StringParser.greedyStringParser())
                        .handler(context -> {
                            Player player = (Player) context.sender().getSender();
                            Optional<String> msgOpt = context.optional("message");
                            MessageConfig msgConfig = configManager.getMessageConfig();

                            if (msgOpt.isEmpty()) {
                                chatListener.setPlayerChannel(player, channelId);

                                var tags = TagResolver.resolver(
                                        Placeholder.parsed("prefix", msgConfig.prefix),
                                        Placeholder.parsed("channel", channel.name));
                                player.sendMessage(
                                        MM.deserialize(msgConfig.channels.switchChannel, tags));
                            } else {
                                chatListener.broadcastToChannel(player, channelId, msgOpt.get());
                            }
                        });

                commandManager.command(channelCommand);
                registeredChannelCommands.add(lowercaseAlias);
            }
        }
    }

    public boolean reload() {
        try {
            configManager.loadConfig();
            configManager.loadMessages();

            registeredChannelCommands.clear();
            registerChannelCommands();

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
        plugin = null;
    }

    public static Yapper getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public YapperUtils getYapperUtils() {
        return yapperUtils;
    }

    public PaperCommandManager<CommandSourceStack> getCommandManager() {
        return commandManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
}
