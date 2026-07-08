package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ChannelTemplate {

    public static ChannelTemplate load(File channelFile, YamlConfigurationProperties properties) {
        return YamlConfigurations.update(channelFile.toPath(), ChannelTemplate.class, properties);
    }

    @Comment("The display name of this chat channel.")
    public String name = "Global";

    @Comment("Command aliases that players can use to switch to this channel or talk in it directly.")
    public List<String> commands = new ArrayList<>(List.of("globalchat", "gc"));

    @Comment("The text communication distance in blocks. Set this to 0 for infinite/global range.")
    public int radius = 0;

    @Comment({
        "A list of chat formats prioritized from top to bottom.",
        "The first format where a player meets the permission node condition will be applied.",
        "Leave the permission empty to treat that specific format as the fallback layout."
    })
    public Map<String, ChannelFormat> formats =
            new LinkedHashMap<>(Map.of("default", new ChannelFormat("", "<name> <dark_gray>➡</dark_gray> <message>")));

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
