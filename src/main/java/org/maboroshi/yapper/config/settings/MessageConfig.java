package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.nio.file.Path;

@Configuration
public class MessageConfig {
    public static MessageConfig load(File dataFolder, YamlConfigurationProperties properties) {
        Path messagesFile = new File(dataFolder, "messages.yml").toPath();
        return YamlConfigurations.update(messagesFile, MessageConfig.class, properties);
    }

    @Comment("The global prefix used in messages. Use <prefix> in other messages to include it.")
    public String prefix = "<color:#ea76cb><bold>Yapper</bold> ➟</color>";

    @Comment("Command-related messages and responses.")
    public CommandMessages commands = new CommandMessages();

    @Comment("Help command messages and entry format.")
    public HelpMessages help = new HelpMessages();

    @Comment("Channel-related messages and responses.")
    public ChannelMessages channels = new ChannelMessages();

    @Configuration
    public static class CommandMessages {
        @Comment("Message shown when reload succeeds.")
        public String reloadSuccess = "<prefix> Plugin configuration reloaded successfully.";

        @Comment("Message shown when reload fails.")
        public String reloadFail = "<prefix> Failed to reload configuration: <red><error></red>";
    }

    @Configuration
    public static class HelpMessages {
        @Comment("Help command header.")
        public String header = "<prefix> Help Menu";

        @Comment("Main command usage.")
        public String about = "<prefix> /yapper about <gray>- Shows plugin about information</gray>";

        @Comment("Help command usage.")
        public String help = "<prefix> /yapper help <gray>- Shows this help menu</gray>";

        @Comment("Reload command usage.")
        public String reload = "<prefix> /yapper reload <gray>- Reloads plugin config</gray>";
    }

    @Configuration
    public static class ChannelMessages {
        @Comment("Message shown when switching to a channel.")
        public String switchChannel = "<prefix> Switched to <channel> channel.";
    }
}
