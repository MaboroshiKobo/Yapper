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

    public enum MacroAction {
        ITEM,
        INVENTORY,
        ENDERCHEST,
        TEXT
    }

    @Comment("Enable debug mode to see detailed logs in the console.")
    public boolean debug = false;

    @Comment({
        "Custom tags that can be reused across any of your channel format layouts.",
        "For example, defining `prefix` here allows you to use <prefix> in your formats."
    })
    public Map<String, String> customTags = new LinkedHashMap<>(Map.of("prefix", "%luckperms_prefix%"));

    @Comment({
        "Chat macros that players with permission can type inline within their messages.",
        " ",
        "You can specify multiple aliases for a single macro by separating them with a pipe `|`.",
        "For example: 'balance|money|bal' allows players to use <balance>, <money>, or <bal>.",
        " ",
        "For interactive macros (ITEM, INVENTORY, ENDERCHEST), you can define `preview-lifetime`",
        "to control how many minutes the clickable preview remains active before expiring."
    })
    public Map<String, MacroSetting> macros = new LinkedHashMap<>(Map.of(
            "item",
                    new MacroSetting(
                            MacroAction.ITEM, "<dark_gray>[</dark_gray><item_preview><dark_gray>]</dark_gray>", 5),
            "inventory|inv",
                    new MacroSetting(
                            MacroAction.INVENTORY,
                            "<hover:show_text:'<gray>Click to view %player_name%'s inventory.</gray>'><dark_gray>[</dark_gray>%player_name%'s Inventory<dark_gray>]</dark_gray></hover>",
                            5),
            "enderchest|ec",
                    new MacroSetting(
                            MacroAction.ENDERCHEST,
                            "<hover:show_text:'<gray>Click to view %player_name%'s enderchest.</gray>'><dark_gray>[</dark_gray>%player_name%'s Ender Chest<dark_gray>]</dark_gray></hover>",
                            5),
            "money|balance|bal", new MacroSetting(MacroAction.TEXT, "<green>$%vault_eco_balance_fixed%</green>")));

    @Configuration
    public static class MacroSetting {
        public MacroAction action;
        public String value;
        public Integer previewLifetime;

        public MacroSetting() {}

        public MacroSetting(MacroAction action, String value) {
            this.action = action;
            this.value = value;
            this.previewLifetime = null;
        }

        public MacroSetting(MacroAction action, String value, int previewLifetime) {
            this.action = action;
            this.value = value;
            this.previewLifetime = previewLifetime;
        }
    }
}
