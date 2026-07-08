package org.maboroshi.yapper.config;

import de.exlll.configlib.ConfigLib;
import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.MainConfig;
import org.maboroshi.yapper.config.settings.MessageConfig;
import org.maboroshi.yapper.util.Log;

public class ConfigManager {
    private final File dataFolder;
    private final Map<String, ChannelTemplate> channels = new HashMap<>();
    private MainConfig mainConfig;
    private MessageConfig messageConfig;

    private static final YamlConfigurationProperties PROPERTIES = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .build();

    public ConfigManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void loadConfig() {
        this.mainConfig = MainConfig.load(dataFolder, PROPERTIES);
        this.loadChannels();
    }

    public void loadMessages() {
        this.messageConfig = MessageConfig.load(dataFolder, PROPERTIES);
    }

    private void loadChannels() {
        this.channels.clear();

        File channelsDirectory = new File(dataFolder, "channels");
        if (!channelsDirectory.exists()) {
            channelsDirectory.mkdirs();
        }

        File defaultChannelFile = new File(channelsDirectory, "global.yml");
        if (!defaultChannelFile.exists()) {
            ChannelTemplate defaultChannel = new ChannelTemplate();
            YamlConfigurations.save(defaultChannelFile.toPath(), ChannelTemplate.class, defaultChannel, PROPERTIES);
        }

        File[] channelFiles = channelsDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (channelFiles != null) {
            for (File channelFile : channelFiles) {
                String fileName = channelFile.getName();

                if (fileName.contains(" ")) {
                    Log.warn("Channel file '" + fileName + "' contains spaces and was skipped.");
                    continue;
                }

                String channelId =
                        fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
                ChannelTemplate channelTemplate = ChannelTemplate.load(channelFile, PROPERTIES);
                this.channels.put(channelId, channelTemplate);
            }
        }
    }

    public void saveConfig() {
        Path configPath = dataFolder.toPath().resolve("config.yml");
        YamlConfigurations.save(configPath, MainConfig.class, mainConfig, PROPERTIES);
    }

    public void saveMessages() {
        Path messagesPath = dataFolder.toPath().resolve("messages.yml");
        YamlConfigurations.save(messagesPath, MessageConfig.class, messageConfig, PROPERTIES);
    }

    public MainConfig getMainConfig() {
        return this.mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return this.messageConfig;
    }

    public ChannelTemplate getChannel(String id) {
        return this.channels.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> getChannelIds() {
        return this.channels.keySet();
    }
}
