package org.maboroshi.yapper.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.MainConfig;
import org.maboroshi.yapper.config.MessageConfig;

public class PrivateMessageManager {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;
    private final MacroProcessor macroProcessor;
    private final Map<UUID, UUID> replyTargets = new ConcurrentHashMap<>();

    public PrivateMessageManager(Yapper plugin) {
        this.plugin = plugin;
        this.macroProcessor = new MacroProcessor(plugin);
    }

    public void clearSession(UUID uuid) {
        replyTargets.remove(uuid);
        replyTargets.values().removeIf(targetUuid -> targetUuid.equals(uuid));
    }

    public void sendPrivateMessage(Player sender, Player recipient, String rawMessage) {
        MainConfig mainConfig = plugin.getConfigManager().getMainConfig();
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();

        if (!mainConfig.privateMessages.enabled) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.privateMessages.disabled, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        if (sender.getUniqueId().equals(recipient.getUniqueId())) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.privateMessages.cannotMessageSelf, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        boolean papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        TagResolver senderPapiResolver = plugin.getFormatUtils().createPapiResolver(sender, papiEnabled);

        List<TagResolver> macroResolvers = macroProcessor.buildMacroResolvers(sender, senderPapiResolver, papiEnabled);

        MiniMessage chatParser = plugin.getFormatUtils().getChatParser(sender);
        Component parsedMessage = chatParser.deserialize(rawMessage, TagResolver.resolver(macroResolvers));

        Component senderFormatted =
                formatMessage(sender, recipient, parsedMessage, mainConfig.privateMessages.senderFormat, papiEnabled);

        Component recipientFormatted = formatMessage(
                sender, recipient, parsedMessage, mainConfig.privateMessages.recipientFormat, papiEnabled);

        sender.sendMessage(senderFormatted);
        recipient.sendMessage(recipientFormatted);

        Bukkit.getConsoleSender().sendMessage(senderFormatted);
        plugin.getChatLogger().log(senderFormatted);

        replyTargets.put(sender.getUniqueId(), recipient.getUniqueId());
        replyTargets.put(recipient.getUniqueId(), sender.getUniqueId());
    }

    public void reply(Player sender, String rawMessage) {
        MessageConfig msgConfig = plugin.getConfigManager().getMessageConfig();
        UUID targetUuid = replyTargets.get(sender.getUniqueId());

        if (targetUuid == null) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.privateMessages.noReplyTarget, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        Player recipient = Bukkit.getPlayer(targetUuid);
        if (recipient == null || !recipient.isOnline() || !sender.canSee(recipient)) {
            sender.sendMessage(MINI_MESSAGE.deserialize(
                    msgConfig.privateMessages.replyTargetOffline, Placeholder.parsed("prefix", msgConfig.prefix)));
            return;
        }

        sendPrivateMessage(sender, recipient, rawMessage);
    }

    private Component formatMessage(
            Player sender, Player recipient, Component messageComponent, String layoutTemplate, boolean papiEnabled) {

        TagResolver senderPapiResolver = plugin.getFormatUtils().createPapiResolver(sender, papiEnabled);
        TagResolver recipientPapiResolver = plugin.getFormatUtils().createRecipientPapiResolver(recipient, papiEnabled);

        List<TagResolver> baseResolvers = new ArrayList<>();
        baseResolvers.add(senderPapiResolver);
        baseResolvers.add(recipientPapiResolver);
        baseResolvers.add(Placeholder.parsed("username", sender.getName()));
        baseResolvers.add(Placeholder.component("displayname", sender.displayName()));
        baseResolvers.add(Placeholder.parsed("sender_username", sender.getName()));
        baseResolvers.add(Placeholder.component("sender_displayname", sender.displayName()));
        baseResolvers.add(Placeholder.parsed("recipient_username", recipient.getName()));
        baseResolvers.add(Placeholder.component("recipient_displayname", recipient.displayName()));
        baseResolvers.add(Placeholder.parsed("sender", sender.getName()));
        baseResolvers.add(Placeholder.parsed("recipient", recipient.getName()));

        TagResolver baseResolverBundle = TagResolver.resolver(baseResolvers);

        List<TagResolver> layoutResolvers = new ArrayList<>(baseResolvers);
        layoutResolvers.add(Placeholder.component("message", messageComponent));

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String processedTagValue = plugin.getFormatUtils()
                    .resolveEmbeddedPlaceholders(sender, recipient, tagEntry.getValue(), papiEnabled);
            Component tagComponent = MINI_MESSAGE.deserialize(processedTagValue, baseResolverBundle);

            if (PlainTextComponentSerializer.plainText()
                    .serialize(tagComponent)
                    .trim()
                    .isEmpty()) {
                tagComponent = Component.empty();
            }

            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        String resolvedTemplate =
                plugin.getFormatUtils().resolveEmbeddedPlaceholders(sender, recipient, layoutTemplate, papiEnabled);
        return MINI_MESSAGE.deserialize(resolvedTemplate, TagResolver.resolver(layoutResolvers));
    }
}
