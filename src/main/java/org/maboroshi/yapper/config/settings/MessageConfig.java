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
        Path messagesPath = dataFolder.toPath().resolve("messages.yml");
        return YamlConfigurations.update(messagesPath, MessageConfig.class, properties);
    }

    @Comment("The system-wide prefix variable. You can use <prefix> inside any other response below.")
    public String prefix = "<color:#ea76cb><bold>Yapper</bold> ➟</color>";

    @Comment("Core plugin command executions and administrative feedback responses.")
    public CommandMessages commands = new CommandMessages();

    @Comment("The informational menu headers and descriptive syntax layouts.")
    public HelpMessages help = new HelpMessages();

    @Comment("Active channel switching notifications and interaction responses.")
    public ChannelMessages channels = new ChannelMessages();

    @Configuration
    public static class CommandMessages {
        @Comment("Message broadcasted to an administrator following a successful configurations refresh.")
        public String reloadSuccess = "<prefix> Plugin configuration reloaded successfully.";

        @Comment("Feedback sent to an operator when an internal parsing exception blocks a files reload.")
        public String reloadFail = "<prefix> Failed to reload configuration: <red><error></red>";
    }

    @Configuration
    public static class HelpMessages {
        @Comment("The structural header text for the central assistance layout directory.")
        public String header = "<prefix> Help Menu";

        @Comment("Description for the standard plugin metrics syntax information command.")
        public String about = "<prefix> /yapper about <gray>- Shows plugin about information</gray>";

        @Comment("Description for displaying the operational commands list directory.")
        public String help = "<prefix> /yapper help <gray>- Shows this help menu</gray>";

        @Comment("Description for the system files verification refresh execution path.")
        public String reload = "<prefix> /yapper reload <gray>- Reloads plugin config</gray>";
    }

    @Configuration
    public static class ChannelMessages {
        @Comment("The notification sent directly to a player when they alter their focus channel room target.")
        public String switchChannel = "<prefix> Switched to <channel> channel.";
    }
}
