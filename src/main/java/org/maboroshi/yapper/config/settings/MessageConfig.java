package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.nio.file.Path;

public class MessageConfig {

    public static MessageConfiguration load(File dataFolder) {
        YamlConfigurationProperties properties = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
                .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
                .build();
        Path messagesFile = new File(dataFolder, "messages.yml").toPath();
        return YamlConfigurations.update(messagesFile, MessageConfiguration.class, properties);
    }

    @Configuration
    public static class MessageConfiguration {
        @Comment("The global prefix used in messages. Use <prefix> in other messages to include it.")
        public String prefix = "<color:#ea76cb><bold>Yapper</bold> ➟</color>";

        @Comment("Command-related messages and responses.")
        public CommandMessages commands = new CommandMessages();

        @Comment("Help command messages and entry format.")
        public HelpMessages help = new HelpMessages();

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
    }
}
