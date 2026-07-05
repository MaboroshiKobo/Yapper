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
    private MainConfig mainConfig;
    private MessageConfig messageConfig;
    private final Map<String, ChannelTemplate> channels;

    private static final YamlConfigurationProperties PROPERTIES = ConfigLib.BUKKIT_DEFAULT_PROPERTIES.toBuilder()
            .setNameFormatter(NameFormatters.LOWER_KEBAB_CASE)
            .build();

    public ConfigManager(File dataFolder) {
        this.dataFolder = dataFolder;
        this.channels = new HashMap<>();
    }

    public void loadConfig() {
        this.mainConfig = MainConfig.load(dataFolder, PROPERTIES);
        loadChannels();
    }

    public void loadMessages() {
        this.messageConfig = MessageConfig.load(dataFolder, PROPERTIES);
    }

    private void loadChannels() {
        channels.clear();

        File folder = new File(dataFolder, "channels");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File defaultFile = new File(folder, "default.yml");
        if (!defaultFile.exists()) {
            ChannelTemplate defaultChannel = new ChannelTemplate();
            YamlConfigurations.save(defaultFile.toPath(), ChannelTemplate.class, defaultChannel, PROPERTIES);
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();

                if (fileName.contains(" ")) {
                    Log.warn("Channel file '" + fileName + "' contains spaces and was skipped.");
                    continue;
                }

                String id = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
                ChannelTemplate channel = ChannelTemplate.load(file, PROPERTIES);
                channels.put(id, channel);
            }
        }
    }

    public void saveConfig() {
        Path settingsPath = new File(dataFolder, "config.yml").toPath();
        YamlConfigurations.save(settingsPath, MainConfig.class, mainConfig, PROPERTIES);
    }

    public void saveMessages() {
        Path path = new File(dataFolder, "messages.yml").toPath();
        YamlConfigurations.save(path, MessageConfig.class, messageConfig, PROPERTIES);
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public ChannelTemplate getChannel(String id) {
        return channels.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<String> getChannelIds() {
        return channels.keySet();
    }
}
