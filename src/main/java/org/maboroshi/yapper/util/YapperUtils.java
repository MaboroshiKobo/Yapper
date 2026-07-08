package org.maboroshi.yapper.util;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.entity.Player;

public class YapperUtils {

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
}
