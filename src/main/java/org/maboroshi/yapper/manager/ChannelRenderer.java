package org.maboroshi.yapper.manager;

import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.ChannelTemplate;
import org.maboroshi.yapper.config.ChannelTemplate.ChannelFormat;

public class ChannelRenderer implements ChatRenderer.ViewerUnaware {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Yapper plugin;
    private final ChannelTemplate channel;

    public ChannelRenderer(Yapper plugin, ChannelTemplate channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message) {
        if (channel == null) {
            return message;
        }

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
        TagResolver papiResolver = plugin.getFormatUtils().createPapiResolver(source, placeholderApiEnabled);

        List<TagResolver> baseResolvers = new ArrayList<>();
        baseResolvers.add(papiResolver);
        baseResolvers.add(Placeholder.parsed("username", source.getName()));
        baseResolvers.add(Placeholder.component(
                "displayname", sourceDisplayName != null ? sourceDisplayName : source.displayName()));
        baseResolvers.add(Placeholder.parsed("channel", channel.name));
        baseResolvers.add(Placeholder.parsed("world", source.getWorld().getName()));

        TagResolver baseResolverBundle = TagResolver.resolver(baseResolvers);

        List<TagResolver> layoutResolvers = new ArrayList<>(baseResolvers);
        layoutResolvers.add(Placeholder.component("message", message));

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String processedTagValue = plugin.getFormatUtils()
                    .resolveEmbeddedPlaceholders(source, tagEntry.getValue(), placeholderApiEnabled);
            Component tagComponent = MINI_MESSAGE.deserialize(processedTagValue, baseResolverBundle);

            if (PlainTextComponentSerializer.plainText()
                    .serialize(tagComponent)
                    .trim()
                    .isEmpty()) {
                tagComponent = Component.empty();
            }

            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        String layoutTemplate = plugin.getFormatUtils()
                .resolveEmbeddedPlaceholders(source, matchedFormat.format, placeholderApiEnabled);
        Component rendered = MINI_MESSAGE.deserialize(layoutTemplate, TagResolver.resolver(layoutResolvers));

        plugin.getChatLogger().log(rendered);

        return rendered;
    }
}
