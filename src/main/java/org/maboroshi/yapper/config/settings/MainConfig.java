package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;

@Configuration
public class MainConfig {
    public static MainConfig load(File dataFolder, YamlConfigurationProperties properties) {
        File configFile = new File(dataFolder, "config.yml");
        return YamlConfigurations.update(configFile.toPath(), MainConfig.class, properties);
    }

    @Comment("Enable debug mode to see detailed logs in the console.")
    public boolean debug = false;
}
