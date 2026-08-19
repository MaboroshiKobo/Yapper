package org.maboroshi.yapper.hook;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.util.Log;

public class DiscordSRVHook {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;

    public DiscordSRVHook(Yapper plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onGameChatMessagePreProcess(GameChatMessagePreProcessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        String channelId = plugin.getSessionManager().getCurrentMessageChannel(player);
        if (channelId == null || channelId.isBlank()) {
            channelId = plugin.getSessionManager().resolveTargetChannel(player.getUniqueId());
        }

        Log.debug("DiscordSRV processing message for channel context: " + channelId);

        event.setChannel(channelId);
        plugin.getSessionManager().clearCurrentMessageChannel(player);
    }

    @Subscribe
    public void onDiscordGuildMessagePreProcess(DiscordGuildMessagePreProcessEvent event) {
        String discordChannelId = event.getChannel().getId();
        String matchedChannelId = null;

        Map<String, String> channels = DiscordSRV.getPlugin().getChannels();
        if (channels != null) {
            for (Map.Entry<String, String> entry : channels.entrySet()) {
                if (discordChannelId.equals(entry.getValue())) {
                    matchedChannelId = entry.getKey();
                    break;
                }
            }
        }

        if (matchedChannelId == null) return;

        ChannelTemplate channelTemplate = plugin.getConfigManager().getChannel(matchedChannelId);
        if (channelTemplate == null) return;

        Message referencedMessage = event.getMessage().getReferencedMessage();
        String replyToName = null;

        if (referencedMessage != null) {
            replyToName = referencedMessage.getMember() != null
                    ? referencedMessage.getMember().getEffectiveName()
                    : referencedMessage.getAuthor().getName();
        }

        String rawFormatTemplate;
        if (replyToName != null
                && channelTemplate.discordReplyFormat != null
                && !channelTemplate.discordReplyFormat.isBlank()) {
            rawFormatTemplate = channelTemplate.discordReplyFormat;
        } else {
            rawFormatTemplate = channelTemplate.discordFormat;
        }

        if (rawFormatTemplate == null || rawFormatTemplate.isBlank()) {
            return;
        }

        event.setCancelled(true);

        String discordUsername = event.getMember() != null
                ? event.getMember().getEffectiveName()
                : event.getAuthor().getName();

        String rawContent = event.getMessage().getContentDisplay();

        UUID linkedUuid = DiscordSRV.getPlugin().getAccountLinkManager() != null
                ? DiscordSRV.getPlugin()
                        .getAccountLinkManager()
                        .getUuid(event.getAuthor().getId())
                : null;
        OfflinePlayer linkedPlayer = linkedUuid != null ? Bukkit.getOfflinePlayer(linkedUuid) : null;

        String minecraftUsername =
                (linkedPlayer != null && linkedPlayer.getName() != null) ? linkedPlayer.getName() : discordUsername;

        Component displayComponent;
        if (linkedPlayer != null && linkedPlayer.isOnline()) {
            Player onlinePlayer = linkedPlayer.getPlayer();
            displayComponent = onlinePlayer != null ? onlinePlayer.displayName() : Component.text(minecraftUsername);
        } else if (linkedPlayer != null && linkedPlayer.getName() != null) {
            displayComponent = Component.text(linkedPlayer.getName());
        } else {
            displayComponent = Component.text(discordUsername);
        }

        boolean papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        TagResolver papiResolver = plugin.getFormatUtils().createPapiResolver(linkedPlayer, papiEnabled);

        List<TagResolver> baseResolvers = new ArrayList<>();
        baseResolvers.add(papiResolver);
        baseResolvers.add(Placeholder.parsed("username", minecraftUsername));
        baseResolvers.add(Placeholder.parsed("discord_username", discordUsername));
        baseResolvers.add(Placeholder.component("displayname", displayComponent));
        baseResolvers.add(Placeholder.parsed("reply_to", replyToName != null ? replyToName : ""));
        baseResolvers.add(Placeholder.parsed("channel", channelTemplate.name));

        TagResolver baseResolverBundle = TagResolver.resolver(baseResolvers);

        List<TagResolver> layoutResolvers = new ArrayList<>(baseResolvers);
        layoutResolvers.add(Placeholder.unparsed("message", rawContent));

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String resolvedTagValue =
                    plugin.getFormatUtils().resolveEmbeddedPlaceholders(linkedPlayer, tagEntry.getValue(), papiEnabled);
            Component tagComponent = MINI_MESSAGE.deserialize(resolvedTagValue, baseResolverBundle);

            if (PlainTextComponentSerializer.plainText()
                    .serialize(tagComponent)
                    .trim()
                    .isEmpty()) {
                tagComponent = Component.empty();
            }

            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        String resolvedFormat =
                plugin.getFormatUtils().resolveEmbeddedPlaceholders(linkedPlayer, rawFormatTemplate, papiEnabled);
        Component formattedComponent = MINI_MESSAGE.deserialize(resolvedFormat, TagResolver.resolver(layoutResolvers));

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            boolean hasPermission = viewer.hasPermission("yapper.channel." + matchedChannelId + ".view");
            boolean isHidden = plugin.getSessionManager().isChannelHidden(viewer.getUniqueId(), matchedChannelId);

            if (hasPermission && !isHidden) {
                viewer.sendMessage(formattedComponent);
            }
        }

        Bukkit.getConsoleSender().sendMessage(formattedComponent);
    }
}
