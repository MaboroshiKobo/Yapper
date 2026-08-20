package org.maboroshi.yapper.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ChannelTemplate;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.MessageConfig;
import org.maboroshi.yapper.hook.TownyHook;
import org.maboroshi.yapper.manager.ChannelRenderer;
import org.maboroshi.yapper.manager.MacroProcessor;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Yapper plugin;
    private final ConfigManager config;
    private final MacroProcessor macroProcessor;

    public ChatListener(Yapper plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.macroProcessor = new MacroProcessor(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getSessionManager().loadSession(event.getPlayer());
        Log.debug("Loaded active session for UUID: " + event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        plugin.getSessionManager().clearSession(playerUuid);
        plugin.getPrivateMessageManager().clearSession(playerUuid);
        Log.debug("Cleared active session for UUID: " + playerUuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        UUID senderUuid = sender.getUniqueId();

        String channelId = plugin.getSessionManager().resolveTargetChannel(senderUuid);

        ChannelTemplate channelTemplate = config.getChannel(channelId);
        if (channelTemplate == null) {
            channelId = "global";
            channelTemplate = config.getChannel("global");
        }

        if (channelTemplate == null) return;

        if (!sender.hasPermission("yapper.channel." + channelId + ".send")) {
            plugin.getSessionManager().clearCurrentMessageChannel(sender);
            event.setCancelled(true);

            MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
            TagResolver placeholders = TagResolver.resolver(
                    Placeholder.parsed("prefix", msgConfig.prefix),
                    Placeholder.parsed("channel", channelTemplate.name),
                    Placeholder.parsed("channel_id", channelId));

            sender.sendMessage(MINI_MESSAGE.deserialize(msgConfig.channels.noPermissionSend, placeholders));
            return;
        }

        plugin.getSessionManager().updateLastUsedChannel(senderUuid, channelId);

        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        TagResolver papiResolver = plugin.getFormatUtils().createPapiResolver(sender, placeholderApiEnabled);

        List<TagResolver> playerMsgResolvers =
                macroProcessor.buildMacroResolvers(sender, papiResolver, placeholderApiEnabled);

        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        MiniMessage playerChatParser = plugin.getFormatUtils().getChatParser(sender);
        Component formattedPlayerMessage =
                playerChatParser.deserialize(plainTextMessage, TagResolver.resolver(playerMsgResolvers));
        event.message(formattedPlayerMessage);

        String targetChannelId = channelId;
        ChannelTemplate targetTemplate = channelTemplate;

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player viewer)) return false;
            return !viewer.hasPermission("yapper.channel." + targetChannelId + ".view")
                    || plugin.getSessionManager().isChannelHidden(viewer.getUniqueId(), targetChannelId);
        });

        if (targetChannelId.startsWith("towny-")) {
            event.viewers().removeIf(audience -> {
                if (!(audience instanceof Player viewer)) return false;
                return !TownyHook.isVisibleTo(sender, viewer, targetChannelId);
            });
        }

        if (targetTemplate.radius > 0) {
            double radiusSquared = targetTemplate.radius * targetTemplate.radius;
            Location senderLocation = sender.getLocation();
            World senderWorld = senderLocation.getWorld();

            event.viewers().removeIf(audience -> {
                if (!(audience instanceof Player viewer)) return false;
                return !viewer.getWorld().equals(senderWorld)
                        || senderLocation.distanceSquared(viewer.getLocation()) > radiusSquared;
            });
        }

        event.renderer(ChatRenderer.viewerUnaware(new ChannelRenderer(plugin, targetTemplate)));
    }
}
