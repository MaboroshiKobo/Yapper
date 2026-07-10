package org.maboroshi.yapper.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ConfigManager;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.hook.TownyHook;
import org.maboroshi.yapper.manager.MacroProcessor;
import org.maboroshi.yapper.renderer.ChannelRenderer;
import org.maboroshi.yapper.util.Log;

public class ChatListener implements Listener {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Yapper plugin;
    private final ConfigManager config;

    public ChatListener(Yapper plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        plugin.getSessionManager().clearSession(playerUuid);
        Log.debug("Cleared active session maps for UUID: " + playerUuid);
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
            sender.sendRichMessage("<red>You do not have permission to speak in the <yellow>" + channelTemplate.name
                    + "</yellow><red> channel.</red>");
            return;
        }

        plugin.getSessionManager().updateLastUsedChannel(senderUuid, channelId);

        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        final TagResolver papiResolver;

        if (placeholderApiEnabled) {
            papiResolver = TagResolver.resolver("papi", (args, context) -> {
                if (!args.hasNext()) return Tag.selfClosingInserting(Component.empty());

                List<String> argList = new ArrayList<>();
                while (args.hasNext()) argList.add(args.pop().value());

                String papiQuery = String.join(":", argList);
                String papiText = PlaceholderAPI.setPlaceholders(sender, "%" + papiQuery + "%");

                Component tempComponent;
                if (papiText.contains("§")) {
                    tempComponent = LegacyComponentSerializer.legacySection().deserialize(papiText);
                } else {
                    try {
                        tempComponent = MINI_MESSAGE.deserialize(papiText);
                    } catch (Exception e) {
                        tempComponent = Component.text(papiText);
                    }
                }

                final Component finalComponent = tempComponent;
                String plainText = PlainTextComponentSerializer.plainText().serialize(finalComponent);
                if (plainText.isEmpty()) {
                    return Tag.styling(builder -> builder.merge(finalComponent.style()));
                }

                return Tag.selfClosingInserting(finalComponent);
            });
        } else {
            papiResolver = TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty()));
        }

        List<TagResolver> playerMsgResolvers =
                new MacroProcessor(plugin).buildMacroResolvers(sender, papiResolver, placeholderApiEnabled);

        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        MiniMessage playerChatParser = plugin.getFormatUtils().getChatParser(sender);
        Component formattedPlayerMessage =
                playerChatParser.deserialize(plainTextMessage, TagResolver.resolver(playerMsgResolvers));
        event.message(formattedPlayerMessage);

        String targetChannelId = channelId;
        ChannelTemplate targetTemplate = channelTemplate;

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player viewer)) return false;
            return !viewer.hasPermission("yapper.channel." + targetChannelId + ".view");
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
