package org.maboroshi.yapper.renderer;

import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.ChannelTemplate.ChannelFormat;

public class YapperRenderer implements ChatRenderer.ViewerUnaware {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;
    private final ChannelTemplate channel;

    public YapperRenderer(Yapper plugin, ChannelTemplate channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message) {
        ChannelFormat matchedFormat = null;
        for (ChannelFormat format : channel.formats.values()) {
            if (format.permission == null || format.permission.isEmpty() || source.hasPermission(format.permission)) {
                matchedFormat = format;
                break;
            }
        }

        if (matchedFormat == null) {
            return message;
        }

        String layoutTemplate = matchedFormat.format;
        List<TagResolver> layoutResolvers = new ArrayList<>();
        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if (placeholderApiEnabled) {
            layoutResolvers.add(TagResolver.resolver("papi", (args, context) -> {
                String papiQuery = args.popOr("The papi tag requires an internal identifier argument").value();
                String papiText = PlaceholderAPI.setPlaceholders(source, "%" + papiQuery + "%");
                Component placeholderComponent =
                        LegacyComponentSerializer.legacySection().deserialize(papiText);
                return Tag.selfClosingInserting(placeholderComponent);
            }));
        } else {
            layoutResolvers.add(
                    TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty())));
        }

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String processedTagValue = tagEntry.getValue();
            if (placeholderApiEnabled) {
                processedTagValue = PlaceholderAPI.setPlaceholders(source, processedTagValue);
            }
            Component tagComponent = MINI_MESSAGE.deserialize(processedTagValue);
            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        List<TagResolver> playerMsgResolvers = new ArrayList<>();
        for (Map.Entry<String, String> macroEntry :
                plugin.getConfigManager().getMainConfig().macros.entrySet()) {
            String rawMacroName = macroEntry.getKey();
            String macroValue = macroEntry.getValue();
            String[] macroAliases = rawMacroName.split("\\|");

            for (String alias : macroAliases) {
                String normalizedAlias = alias.trim().toLowerCase();

                if (!source.hasPermission("yapper.macro." + normalizedAlias)) {
                    continue;
                }

                String processedMacroValue = macroValue;
                if (placeholderApiEnabled) {
                    processedMacroValue = PlaceholderAPI.setPlaceholders(source, processedMacroValue);
                }

                final String finalMacroValue = processedMacroValue;
                playerMsgResolvers.add(TagResolver.resolver(normalizedAlias, (args, context) -> {
                    try {
                        return Tag.selfClosingInserting(MINI_MESSAGE.deserialize(finalMacroValue));
                    } catch (Exception e) {
                        return Tag.selfClosingInserting(
                                LegacyComponentSerializer.legacySection().deserialize(finalMacroValue));
                    }
                }));
            }
        }

        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(message);
        MiniMessage playerChatParser = plugin.getYapperUtils().getChatParser(source);
        Component formattedPlayerMessage =
                playerChatParser.deserialize(plainTextMessage, TagResolver.resolver(playerMsgResolvers));

        layoutResolvers.add(Placeholder.parsed("name", source.getName()));
        layoutResolvers.add(Placeholder.component("displayname", sourceDisplayName));
        layoutResolvers.add(Placeholder.parsed("channel", channel.name));
        layoutResolvers.add(Placeholder.parsed("world", source.getWorld().getName()));
        layoutResolvers.add(Placeholder.component("message", formattedPlayerMessage));

        TagResolver finalRegistry =
                TagResolver.builder().resolvers(layoutResolvers).build();
        return MINI_MESSAGE.deserialize(layoutTemplate, finalRegistry);
    }
}
