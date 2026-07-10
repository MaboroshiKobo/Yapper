package org.maboroshi.yapper;

import github.scarsz.discordsrv.DiscordSRV;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.setting.ManagerSetting;
import org.maboroshi.yapper.command.YapperCommand;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.MessageConfig;
import org.maboroshi.yapper.hook.DiscordSRVHook;
import org.maboroshi.yapper.listener.ChatListener;
import org.maboroshi.yapper.listener.InventoryListener;
import org.maboroshi.yapper.manager.SessionManager;
import org.maboroshi.yapper.util.FormatUtils;
import org.maboroshi.yapper.util.Log;

public final class Yapper extends JavaPlugin {
    private static Yapper plugin;

    private ConfigManager configManager;
    private FormatUtils formatUtils;
    private SessionManager sessionManager;
    private PaperCommandManager<CommandSourceStack> commandManager;
    private final Set<String> registeredChannelCommands = new HashSet<>();
    private ChatListener chatListener;
    private final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        plugin = this;
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

        commandManager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);

        AnnotationParser<CommandSourceStack> annotationParser =
                new AnnotationParser<>(commandManager, CommandSourceStack.class);

        annotationParser.parse(new YapperCommand(this));

        this.registerChannelCommands();

        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.subscribe(new DiscordSRVHook(this));
        }

        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this, 32126);

        Log.info("Yapper has been enabled!");
    }

    private void registerChannelCommands() {
        for (String channelId : configManager.getChannelIds()) {
            ChannelTemplate channel = configManager.getChannel(channelId);
            if (channel == null || channel.commands == null) continue;

            for (String commandAlias : channel.commands) {
                String normalizedAlias = commandAlias.toLowerCase();
                if (registeredChannelCommands.contains(normalizedAlias)) continue;

                var channelCommand = commandManager
                        .commandBuilder(commandAlias)
                        .permission("yapper.channel." + channelId + ".send")
                        .optional("message", StringParser.greedyStringParser())
                        .handler(context -> {
                            CommandSender commandSender = context.sender().getSender();
                            if (!(commandSender instanceof Player player)) {
                                commandSender.sendMessage("Only players can use this command.");
                                return;
                            }

                            ChannelTemplate currentChannel = configManager.getChannel(channelId);
                            if (currentChannel == null) {
                                player.sendMessage("This channel is no longer available.");
                                return;
                            }

                            Optional<String> messageOptional = context.optional("message");
                            MessageConfig messageConfig = configManager.getMessageConfig();

                            if (messageOptional.isEmpty()) {
                                sessionManager.setPlayerChannel(player, channelId);

                                var templatePlaceholders = TagResolver.resolver(
                                        Placeholder.parsed("prefix", messageConfig.prefix),
                                        Placeholder.parsed("channel", currentChannel.name));

                                player.sendMessage(MINI_MESSAGE.deserialize(
                                        messageConfig.channels.switchChannel, templatePlaceholders));
                            } else {
                                sessionManager.setTempChannelOverride(player, channelId);
                                player.chat(messageOptional.get());
                            }
                        });

                commandManager.command(channelCommand);
                registeredChannelCommands.add(normalizedAlias);
            }
        }
    }

    public boolean reload() {
        configManager.loadConfig();
        configManager.loadMessages();
        registerChannelCommands();
        for (Player player : Bukkit.getOnlinePlayers()) {
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
