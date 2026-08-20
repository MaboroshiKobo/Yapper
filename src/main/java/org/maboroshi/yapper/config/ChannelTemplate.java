package org.maboroshi.yapper.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ChannelTemplate {

    public static ChannelTemplate load(File channelFile, YamlConfigurationProperties properties) {
        return YamlConfigurations.update(channelFile.toPath(), ChannelTemplate.class, properties);
    }

    @Comment("The display name of this chat channel.")
    public String name = "Global";

    @Comment("The text communication distance in blocks. Set this to 0 for infinite/global range.")
    public int radius = 0;

    @Comment({
        "The format used when a standard message from Discord is received for this channel.",
        "Available placeholders: <username>, <displayname>, <discord_username>, <discord_displayname>,",
        "<discord_id>, <discord_channel>, <discord_guild>, <discord_role>, <discord_top_role>,",
        "<discord_role_color>, <discord_all_roles>, <channel>, <channel_id>, <message>"
    })
    public String discordFormat =
            "<gray>(<#5865F2>Discord</#5865F2> | <white><channel></white>)</gray> <username> <separator> <message>";

    @Comment({
        "The format used when a message from Discord is replying to another message.",
        "If left blank, it will automatically fall back to 'discordFormat'.",
        "Available placeholders: <username>, <displayname>, <discord_username>, <discord_displayname>,",
        "<discord_id>, <discord_channel>, <discord_guild>, <discord_role>, <discord_top_role>,",
        "<discord_role_color>, <discord_all_roles>, <reply_to>, <reply_to_message>,",
        "<channel>, <channel_id>, <message>"
    })
    public String discordReplyFormat =
            "<gray>(<#5865F2>Discord</#5865F2> | <white><channel></white>)</gray> <username> <gray>(replying to <white><reply_to></white>)</gray> <separator> <message>";

    @Comment({
        "A list of chat formats prioritized from top to bottom.",
        "The first format where a player meets the permission node condition will be applied.",
        "Leave the permission empty to treat that specific format as the fallback layout.",
        "Available placeholders: <username>, <displayname>, <world>, <channel>, <channel_id>, <message>"
    })
    public Map<String, ChannelFormat> formats =
            new LinkedHashMap<>(Map.of("default", new ChannelFormat("", "<username> <separator> <message>")));

    @Configuration
    public static class ChannelFormat {
        @Comment("The specific permission node required to use this formatting layout configuration.")
        public String permission;

        @Comment("The visual chat design layout pattern. Supports MiniMessage tags, macros, and custom tags.")
        public String format;

        public ChannelFormat() {}

        public ChannelFormat(String permission, String format) {
            this.permission = permission;
            this.format = format;
        }
    }
}
