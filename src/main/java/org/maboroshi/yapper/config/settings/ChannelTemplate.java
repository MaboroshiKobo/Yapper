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
    public static ChannelTemplate load(File channelTemplate, YamlConfigurationProperties properties) {
        return YamlConfigurations.update(channelTemplate.toPath(), ChannelTemplate.class, properties);
    }

    @Comment("Channel name.")
    public String name = "Global";

    @Comment("Channel commands.")
    public List<String> commands = new ArrayList<>(List.of("globalchat", "gc"));

    @Comment("Channel radius in blocks (0 means infinite/global).")
    public int radius = 0;

    public Map<String, ChannelFormat> formats = new LinkedHashMap<>(
            Map.of("default", new ChannelFormat("", "<name> <dark_gray>➡</dark_gray> <message>")));

    @Configuration
    public static class ChannelFormat {
        @Comment("Permission requirement.")
        public String permission;

        @Comment("The chat format.")
        public String format;

        public ChannelFormat() {}

        public ChannelFormat(String permission, String format) {
            this.permission = permission;
            this.format = format;
        }
    }
}
