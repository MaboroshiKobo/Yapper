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
        try {
            this.mainConfig = MainConfig.load(dataFolder, PROPERTIES);
        } catch (Exception e) {
            Log.error("Failed to parse config.yml: " + e.getMessage());
            if (this.mainConfig == null) {
                Log.warn("Initial load failed. Using default configuration fallback.");
                this.mainConfig = new MainConfig();
            } else {
                Log.warn("Reload aborted. Retaining last known stable config state.");
            }
        }
        this.loadChannels();
    }

    public void loadMessages() {
        try {
            this.messageConfig = MessageConfig.load(dataFolder, PROPERTIES);
        } catch (Exception e) {
            Log.error("Failed to parse messages.yml: " + e.getMessage());
            if (this.messageConfig == null) {
                Log.warn("Initial load failed. Using default message template fallback.");
                this.messageConfig = new MessageConfig();
            } else {
                Log.warn("Reload aborted. Retaining last known stable message state.");
            }
        }
    }

    private void loadChannels() {
        Map<String, ChannelTemplate> stagingChannels = new HashMap<>();
        File channelsDirectory = new File(dataFolder, "channels");

        if (!channelsDirectory.exists()) {
            channelsDirectory.mkdirs();
        }

        File defaultChannelFile = new File(channelsDirectory, "global.yml");
        if (!defaultChannelFile.exists()) {
            try {
                ChannelTemplate defaultChannel = new ChannelTemplate();
                YamlConfigurations.save(defaultChannelFile.toPath(), ChannelTemplate.class, defaultChannel, PROPERTIES);
            } catch (Exception e) {
                Log.error("Could not generate default global.yml template: " + e.getMessage());
            }
        }

        File[] channelFiles = channelsDirectory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (channelFiles != null) {
            for (File channelFile : channelFiles) {
                String fileName = channelFile.getName();

                if (fileName.contains(" ")) {
                    Log.warn("Skipped channel file '" + fileName + "' due to unsupported spaces in the filename.");
                    continue;
                }

                String channelId =
                        fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);

                try {
                    ChannelTemplate channelTemplate = ChannelTemplate.load(channelFile, PROPERTIES);
                    stagingChannels.put(channelId, channelTemplate);
                } catch (Exception e) {
                    Log.error("Failed to parse channel '" + fileName + "': " + e.getMessage());
                    if (this.channels.containsKey(channelId)) {
                        Log.warn("Using active in-memory instance for channel fallback: " + channelId);
                        stagingChannels.put(channelId, this.channels.get(channelId));
                    }
                }
            }
        }

        this.channels.clear();
        this.channels.putAll(stagingChannels);
    }

    public void saveConfig() {
        try {
            Path configPath = dataFolder.toPath().resolve("config.yml");
            YamlConfigurations.save(configPath, MainConfig.class, mainConfig, PROPERTIES);
        } catch (Exception e) {
            Log.error("Failed to save config.yml changes: " + e.getMessage());
        }
    }

    public void saveMessages() {
        try {
            Path messagesPath = dataFolder.toPath().resolve("messages.yml");
            YamlConfigurations.save(messagesPath, MessageConfig.class, messageConfig, PROPERTIES);
        } catch (Exception e) {
            Log.error("Failed to save messages.yml changes: " + e.getMessage());
        }
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
