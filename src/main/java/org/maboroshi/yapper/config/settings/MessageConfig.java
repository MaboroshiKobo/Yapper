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
    public String prefix = "<color:#ad37fd><bold>Yapper</bold> ➟</color>";

    @Comment("Core plugin command executions and administrative feedback responses.")
    public CommandMessages commands = new CommandMessages();

    @Comment("The informational menu headers and descriptive syntax layouts.")
    public HelpMessages help = new HelpMessages();

    @Comment("Active channel switching notifications and interaction responses.")
    public ChannelMessages channels = new ChannelMessages();

    @Configuration
    public static class CommandMessages {
        @Comment("Message broadcasted when a non-player attempts to run a player-only command.")
        public String playerOnly = "<prefix> <red>Only players can execute this command.</red>";

        @Comment("Message display for the about plugin command.")
        public String about =
                "<prefix> Running version <light_purple><version></light_purple> developed by <light_purple><authors></light_purple>.";

        @Comment("Message broadcasted to an administrator following a successful configuration refresh.")
        public String reloadSuccess = "<prefix> <green>Plugin configuration reloaded successfully.</green>";

        @Comment("Feedback sent to an operator when an internal parsing exception blocks a configuration reload.")
        public String reloadFail = "<prefix> <red>Failed to reload configuration:</red> <gray><error></gray>";
    }

    @Configuration
    public static class HelpMessages {
        @Comment("The structural header text for the central assistance layout directory.")
        public String header = "<prefix> <gray>Command Help</gray>";

        @Comment("Description for the standard plugin version information command.")
        public String about = "<prefix> /yapper about <gray>- Shows plugin info and version</gray>";

        @Comment("Description for displaying the operational commands list directory.")
        public String help = "<prefix> /yapper help <gray>- Shows this help menu</gray>";

        @Comment("Description for the system configuration refresh command.")
        public String reload = "<prefix> /yapper reload <gray>- Reloads plugin configuration</gray>";
    }

    @Configuration
    public static class ChannelMessages {
        @Comment("The notification sent directly to a player showing their current focused channel.")
        public String currentChannel =
                "<prefix> You are currently in <light_purple><channel></light_purple> <dark_gray>(<channel_id>)</dark_gray>.";

        @Comment("The notification sent directly to a player when they alter their focus channel room target.")
        public String switchChannel = "<prefix> Switched active channel to <light_purple><channel></light_purple>.";

        @Comment("Message sent when a specified channel cannot be found.")
        public String notFound = "<prefix> Channel <red><channel_id></red> does not exist.</red>";

        @Comment("Message sent when a player lacks permission to view a channel.")
        public String noPermissionView =
                "<prefix> <red>You do not have permission to view <light_purple><channel></light_purple>.</red>";

        @Comment("Message sent when a player lacks permission to speak in a channel.")
        public String noPermissionSend =
                "<prefix> You do not have permission to speak in <light_purple><channel></light_purple>.";

        @Comment("Notification sent when a player hides a channel.")
        public String hideSuccess =
                "<prefix> Channel <light_purple><channel></light_purple> hidden. You will no longer see messages from it.";

        @Comment("Notification sent when a player unhides a channel.")
        public String showSuccess =
                "<prefix> Channel <light_purple><channel></light_purple> unhidden. You will now see messages from it again.";

        @Comment("Header for the channel list menu.")
        public String listHeader = "<prefix> Available chat channels:";

        @Comment("Format for each channel item entry in the channel list.")
        public String listItem = "<gray>- </gray><light_purple><channel></light_purple> <gray>(<channel_id>)</gray> "
                + "<click:run_command:'/yapper channel <channel_id>'><hover:show_text:'Click to switch to <light_purple><channel></light_purple>'><yellow>[Switch]</yellow></hover></click> "
                + "<click:run_command:'/yapper channel hide <channel_id>'><hover:show_text:'Click to hide <light_purple><channel></light_purple>'><red>[Hide]</red></hover></click> "
                + "<click:run_command:'/yapper channel show <channel_id>'><hover:show_text:'Click to show <light_purple><channel></light_purple>'><green>[Show]</green></hover></click>";

        @Comment("Header for the channel information command.")
        public String infoHeader =
                "<prefix> Channel information for <light_purple><channel></light_purple> <gray>(<channel_id>)</gray>:";

        @Comment("Radius label in channel info.")
        public String infoRadius = " - Radius: <light_purple><radius></light_purple>";

        @Comment("Status label in channel info.")
        public String infoStatus = " - Status: <light_purple><status></light_purple>";

        @Comment("Text representation for infinite/global radius.")
        public String radiusInfinite = "Global (Infinite)";

        @Comment("Text representation for block radius.")
        public String radiusBlocks = "<radius> blocks";

        @Comment("Status text when a channel is visible.")
        public String statusVisible = "Visible";

        @Comment("Status text when a channel is hidden.")
        public String statusHidden = "Hidden";
    }
}
