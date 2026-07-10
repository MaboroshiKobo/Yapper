package org.maboroshi.yapper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class FormatUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern PAPI_EMBED_PATTERN = Pattern.compile("<papi:([^>]+)>");

    public MiniMessage getChatParser(Player player) {
        List<TagResolver> allowedTags = new ArrayList<>();

        if (player.hasPermission("yapper.chat.colors")) {
            allowedTags.add(StandardTags.color());
        }

        if (player.hasPermission("yapper.chat.decorations")) {
            allowedTags.add(StandardTags.decorations());
            allowedTags.add(StandardTags.reset());
        }

        if (player.hasPermission("yapper.chat.gradient")) {
            allowedTags.add(StandardTags.gradient());
        }

        if (player.hasPermission("yapper.chat.rainbow")) {
            allowedTags.add(StandardTags.rainbow());
        }

        if (player.hasPermission("yapper.chat.pride")) {
            allowedTags.add(StandardTags.pride());
        }

        if (player.hasPermission("yapper.chat.shadow")) {
            allowedTags.add(StandardTags.shadowColor());
        }

        return MiniMessage.builder().tags(TagResolver.resolver(allowedTags)).build();
    }

    public String resolveEmbeddedPlaceholders(Player player, String text, boolean papiEnabled) {
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
