package org.maboroshi.yapper.config.settings;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class MainConfig {
    public static MainConfig load(File dataFolder, YamlConfigurationProperties properties) {
        File configFile = new File(dataFolder, "config.yml");
        return YamlConfigurations.update(configFile.toPath(), MainConfig.class, properties);
    }

    @Comment("Enable debug mode to see detailed logs in the console.")
    public boolean debug = false;

    @Comment("Chat components can be used in channel formatting.")
    public Map<String, String> components = new LinkedHashMap<>(
            Map.of(
                    "prefix",
                    "<click:open_url:'https://store.server.com'><hover:show_text:'Click to visit the store!'>%luckperms_prefix%</hover></click>"));

    @Comment("Chat macros can be used in the server.")
    public Map<String, String> macros = new LinkedHashMap<>(
            Map.of(
                    "balance",
                    "<click:suggest_command:'/pay %player_name% '><hover:show_text:'Click to send money!'><dark_gray>[</dark_gray><gray>Money: <gold>$%vault_eco_balance_commas%</gold></gray><dark_gray>]</dark_gray></hover></click>"));
}
