package org.maboroshi.yapper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.hook.PlaceholderAPIHook;
import org.maboroshi.yapper.hook.PlaceholderHook;

public class YapperUtils {
    private final PlaceholderHook hook;
    private final Map<Integer, MiniMessage> parserCache = new ConcurrentHashMap<>();

    public YapperUtils() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.hook = new PlaceholderAPIHook();
        } else {
            this.hook = (player, text) -> text;
        }
    }

    public String process(OfflinePlayer player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return hook.parse(player, text);
    }

    public MiniMessage getEffectiveParser(Player player) {
        int mask = 0;
        if (player.hasPermission("yapper.chat.color")) mask |= 1;
        if (player.hasPermission("yapper.chat.decorations")) mask |= 2;
        if (player.hasPermission("yapper.chat.gradient")) mask |= 4;
        if (player.hasPermission("yapper.chat.rainbow")) mask |= 8;

        return parserCache.computeIfAbsent(mask, k -> {
            List<TagResolver> tags = new ArrayList<>();
            if ((k & 1) != 0) tags.add(StandardTags.color());
            if ((k & 2) != 0) tags.add(StandardTags.decorations());
            if ((k & 4) != 0) tags.add(StandardTags.gradient());
            if ((k & 8) != 0) tags.add(StandardTags.rainbow());
            return MiniMessage.builder().tags(TagResolver.resolver(tags)).build();
        });
    }
}
