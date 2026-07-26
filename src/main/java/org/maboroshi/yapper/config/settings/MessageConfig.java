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
                "<prefix> Running version <yellow><version></yellow> developed by <gold><authors></gold>.";

        @Comment("Message broadcasted to an administrator following a successful configuration refresh.")
        public String reloadSuccess = "<prefix> <green>Plugin configuration reloaded successfully.</green>";

        @Comment("Feedback sent to an operator when an internal parsing exception blocks a configuration reload.")
        public String reloadFail = "<prefix> <red>Failed to reload configuration:</red> <gray><error></gray>";
    }

    @Configuration
    public static class HelpMessages {
        @Comment("The structural header text for the central assistance layout directory.")
        public String header = "<prefix> <gray>Command Help:</gray>";

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
                "<prefix> You are currently speaking in <yellow><channel></yellow> <dark_gray>(<channel_id>)</dark_gray>.";

        @Comment("The notification sent directly to a player when they alter their focus channel room target.")
        public String switchChannel = "<prefix> Switched active channel to <yellow><channel></yellow>.";

        @Comment("Message sent when a specified channel cannot be found.")
        public String notFound = "<prefix> <red>Channel <yellow><channel_id></yellow> does not exist.</red>";

        @Comment("Message sent when a player lacks permission to view a channel.")
        public String noPermissionView =
                "<prefix> <red>You do not have permission to view <yellow><channel></yellow>.</red>";

        @Comment("Message sent when a player lacks permission to speak in a channel.")
        public String noPermissionSend =
                "<prefix> <red>You do not have permission to speak in <yellow><channel></yellow>.</red>";

        @Comment("Notification sent when a player hides a channel.")
        public String hideSuccess =
                "<prefix> <gray>Channel <yellow><channel></yellow> hidden. You will no longer see messages from it.</gray>";

        @Comment("Notification sent when a player unhides a channel.")
        public String showSuccess =
                "<prefix> <gray>Channel <yellow><channel></yellow> unhidden. You will now see messages from it again.</gray>";

        @Comment("Header for the channel list menu.")
        public String listHeader = "<prefix> <gray>Available Chat Channels:</gray>";

        @Comment("Format for each channel item entry in the channel list.")
        public String listItem = "<gray>- </gray><yellow><channel></yellow> <dark_gray>(<channel_id>)</dark_gray> "
                + "<click:run_command:'/yapper channel <channel_id>'><hover:show_text:'<gray>Click to switch to </gray><yellow><channel></yellow>'><green>[Switch]</green></hover></click> "
                + "<click:run_command:'/yapper channel hide <channel_id>'><hover:show_text:'<gray>Click to hide </gray><yellow><channel></yellow>'><red>[Hide]</red></hover></click> "
                + "<click:run_command:'/yapper channel show <channel_id>'><hover:show_text:'<gray>Click to show </gray><yellow><channel></yellow>'><yellow>[Show]</yellow></hover></click>";

        @Comment("Header for the channel information command.")
        public String infoHeader =
                "<prefix> <gray>Channel Info for <yellow><channel></yellow> <dark_gray>(<channel_id>)</dark_gray>:</gray>";

        @Comment("Radius label in channel info.")
        public String infoRadius = "<gray> - Radius: <yellow><radius></yellow></gray>";

        @Comment("Status label in channel info.")
        public String infoStatus = "<gray> - Status: <yellow><status></yellow></gray>";

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
