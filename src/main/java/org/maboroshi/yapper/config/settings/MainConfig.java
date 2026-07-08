package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class MainConfig {

    public static MainConfig load(File dataFolder, YamlConfigurationProperties properties) {
        Path configPath = dataFolder.toPath().resolve("config.yml");
        return YamlConfigurations.update(configPath, MainConfig.class, properties);
    }

    @Comment("Enable debug mode to see detailed tag resolution and hook logs in the console.")
    public boolean debug = false;

    @Comment({
        "Custom tags that can be reused across any of your channel format layouts.",
        "For example, defining 'prefix' here allows you to use <prefix> in your formats.",
        "PlaceholderAPI strings will be fully evaluated before rendering."
    })
    public Map<String, String> customTags = new LinkedHashMap<>(Map.of("prefix", "%luckperms_prefix%"));

    @Comment({
        "Chat macros that players with permission can type inline within their messages.",
        "You can specify multiple aliases for a single macro by separating them with a pipe '|'.",
        "For example: 'balance|money|bal' allows players to use <balance>, <money>, or <bal>."
    })
    public Map<String, String> macros = new LinkedHashMap<>(Map.of(
            "item|i", "<dark_gray>[</dark_gray><held_item><dark_gray>]</dark_gray>",
            "balance|money|bal", "<green>$<papi:vault_eco_balance_fixed></green>"));
}
