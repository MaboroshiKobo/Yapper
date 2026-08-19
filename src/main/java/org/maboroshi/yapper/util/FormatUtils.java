package org.maboroshi.yapper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
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

    public TagResolver createPapiResolver(OfflinePlayer player, boolean papiEnabled) {
        if (!papiEnabled || player == null) {
            return TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty()));
        }

        return TagResolver.resolver("papi", (args, context) -> {
            if (!args.hasNext()) return Tag.selfClosingInserting(Component.empty());
            List<String> argList = new ArrayList<>();
            while (args.hasNext()) {
                argList.add(args.pop().value());
            }
            String papiQuery = String.join(":", argList);
            String papiText = PlaceholderAPI.setPlaceholders(player, "%" + papiQuery + "%");
            Component comp;
            if (papiText.contains("§")) {
                comp = LegacyComponentSerializer.legacySection().deserialize(papiText);
            } else {
                try {
                    comp = MINI_MESSAGE.deserialize(papiText);
                } catch (Exception e) {
                    comp = Component.text(papiText);
                }
            }
            return Tag.selfClosingInserting(comp);
        });
    }

    public String resolveEmbeddedPlaceholders(OfflinePlayer player, String text, boolean papiEnabled) {
        if (text == null || !papiEnabled || !text.contains("<papi:")) {
            return text;
        }

        Matcher matcher = PAPI_EMBED_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String placeholderQuery = matcher.group(1);
            String papiText =
                    player != null ? PlaceholderAPI.setPlaceholders(player, "%" + placeholderQuery + "%") : "";

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
