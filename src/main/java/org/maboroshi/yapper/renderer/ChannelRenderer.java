package org.maboroshi.yapper.renderer;

import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

public class ChannelRenderer implements ChatRenderer.ViewerUnaware {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern PAPI_EMBED_PATTERN = Pattern.compile("<papi:([^>]+)>");

    private final Yapper plugin;
    private final ChannelTemplate channel;

    public ChannelRenderer(Yapper plugin, ChannelTemplate channel) {
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

        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        String layoutTemplate = resolveEmbeddedPlaceholders(source, matchedFormat.format, placeholderApiEnabled);
        List<TagResolver> layoutResolvers = new ArrayList<>();

        final TagResolver papiResolver;
        if (placeholderApiEnabled) {
            papiResolver = TagResolver.resolver("papi", (args, context) -> {
                if (!args.hasNext()) {
                    return Tag.selfClosingInserting(Component.empty());
                }

                List<String> argList = new ArrayList<>();
                while (args.hasNext()) {
                    argList.add(args.pop().value());
                }
                String papiQuery = String.join(":", argList);
                String papiText = PlaceholderAPI.setPlaceholders(source, "%" + papiQuery + "%");

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

                final Component placeholderComponent = tempComponent;
                String plainText = PlainTextComponentSerializer.plainText().serialize(placeholderComponent);
                if (plainText.isEmpty()) {
                    return Tag.styling(builder -> builder.merge(placeholderComponent.style()));
                }

                return Tag.selfClosingInserting(placeholderComponent);
            });
        } else {
            papiResolver = TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty()));
        }

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String processedTagValue = resolveEmbeddedPlaceholders(source, tagEntry.getValue(), placeholderApiEnabled);
            Component tagComponent = MINI_MESSAGE.deserialize(processedTagValue, papiResolver);
            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        layoutResolvers.add(papiResolver);
        layoutResolvers.add(Placeholder.parsed("name", source.getName()));
        layoutResolvers.add(Placeholder.component("displayname", sourceDisplayName));
        layoutResolvers.add(Placeholder.parsed("channel", channel.name));
        layoutResolvers.add(Placeholder.parsed("world", source.getWorld().getName()));
        layoutResolvers.add(Placeholder.component("message", message));

        TagResolver finalRegistry =
                TagResolver.builder().resolvers(layoutResolvers).build();
        return MINI_MESSAGE.deserialize(layoutTemplate, finalRegistry);
    }

    private String resolveEmbeddedPlaceholders(Player player, String text, boolean papiEnabled) {
        if (text == null || !papiEnabled || !text.contains("<papi:")) {
            return text;
        }

        Matcher matcher = PAPI_EMBED_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholderQuery = matcher.group(1);
            String papiText = PlaceholderAPI.setPlaceholders(player, "%" + placeholderQuery + "%");

            String mmCompatibleText;
            if (papiText.contains("§")) {
                Component legacyComponent =
                        LegacyComponentSerializer.legacySection().deserialize(papiText);
                mmCompatibleText = MINI_MESSAGE.serialize(legacyComponent);
            } else {
                mmCompatibleText = papiText;
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(mmCompatibleText));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
