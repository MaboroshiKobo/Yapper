package org.maboroshi.yapper.manager;

import io.papermc.paper.chat.ChatRenderer;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
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

        TagResolver customTagsResolver = new TagResolver() {
            @Override
            public Tag resolve(String tagName, ArgumentQueue args, Context ctx) {
                String rawTagValue =
                        plugin.getConfigManager().getMainConfig().customTags.get(tagName);
                if (rawTagValue == null) {
                    return null;
                }

                if (rawTagValue.isBlank()) {
                    return Tag.selfClosingInserting(Component.empty());
                }

                String processedValue =
                        plugin.getFormatUtils().resolveEmbeddedPlaceholders(source, rawTagValue, placeholderApiEnabled);

                if (processedValue.isBlank()) {
                    return Tag.selfClosingInserting(Component.empty());
                }

                Component tagComponent = MINI_MESSAGE.deserialize(processedValue, baseResolverBundle);

                if (PlainTextComponentSerializer.plainText()
                        .serialize(tagComponent)
                        .trim()
                        .isEmpty()) {
                    return Tag.selfClosingInserting(Component.empty());
                }

                return Tag.selfClosingInserting(tagComponent);
            }

            @Override
            public boolean has(String tagName) {
                return plugin.getConfigManager().getMainConfig().customTags.containsKey(tagName);
            }
        };

        TagResolver messageResolver = Placeholder.component("message", message);
        TagResolver layoutResolver = TagResolver.resolver(baseResolverBundle, customTagsResolver, messageResolver);

        String layoutTemplate = plugin.getFormatUtils()
                .resolveEmbeddedPlaceholders(source, matchedFormat.format, placeholderApiEnabled);

        Component rendered = MINI_MESSAGE.deserialize(layoutTemplate, layoutResolver);

        plugin.getChatLogger().log(rendered);

        return rendered;
    }
}
