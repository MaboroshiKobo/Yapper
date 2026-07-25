package org.maboroshi.yapper.manager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.settings.MainConfig;
import org.maboroshi.yapper.menu.PreviewHolder;

public class MacroProcessor {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;

    public MacroProcessor(Yapper plugin) {
        this.plugin = plugin;
    }

    public List<TagResolver> buildMacroResolvers(Player sender, TagResolver papiResolver, boolean papiEnabled) {
        List<TagResolver> resolvers = new ArrayList<>();

        for (Map.Entry<String, MainConfig.MacroSetting> macroEntry :
                plugin.getConfigManager().getMainConfig().macros.entrySet()) {
            String rawMacroName = macroEntry.getKey();
            MainConfig.MacroSetting setting = macroEntry.getValue();
            String[] macroAliases = rawMacroName.split("\\|");

            for (String alias : macroAliases) {
                String normalizedAlias = alias.trim().toLowerCase();
                if (!sender.hasPermission("yapper.macro." + normalizedAlias)) {
                    continue;
                }

                String finalMacroValue =
                        plugin.getFormatUtils().resolveEmbeddedPlaceholders(sender, setting.value, papiEnabled);

                if (setting.action == MainConfig.MacroAction.INVENTORY) {
                    Component invComponent = MINI_MESSAGE
                            .deserialize(finalMacroValue, papiResolver)
                            .clickEvent(ClickEvent.callback(
                                    audience -> {
                                        if (audience instanceof Player viewer) {
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                Inventory previewGui = Bukkit.createInventory(
                                                        new PreviewHolder(),
                                                        45,
                                                        MINI_MESSAGE.deserialize("<dark_gray>" + sender.getName()
                                                                + "'s Inventory</dark_gray>"));
                                                previewGui.setContents(
                                                        sender.getInventory().getContents());
                                                viewer.openInventory(previewGui);
                                            });
                                        }
                                    },
                                    options -> options.uses(ClickCallback.UNLIMITED_USES)
                                            .lifetime(Duration.ofMinutes(setting.previewLifetime))));
                    resolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, ctx) -> Tag.selfClosingInserting(invComponent)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.ENDERCHEST) {
                    Component ecComponent = MINI_MESSAGE
                            .deserialize(finalMacroValue, papiResolver)
                            .clickEvent(ClickEvent.callback(
                                    audience -> {
                                        if (audience instanceof Player viewer) {
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                Inventory previewGui = Bukkit.createInventory(
                                                        new PreviewHolder(),
                                                        27,
                                                        MINI_MESSAGE.deserialize("<dark_gray>" + sender.getName()
                                                                + "'s Enderchest</dark_gray>"));
                                                previewGui.setContents(
                                                        sender.getEnderChest().getContents());
                                                viewer.openInventory(previewGui);
                                            });
                                        }
                                    },
                                    options -> options.uses(ClickCallback.UNLIMITED_USES)
                                            .lifetime(Duration.ofMinutes(setting.previewLifetime))));
                    resolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, ctx) -> Tag.selfClosingInserting(ecComponent)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.ITEM) {
                    ItemStack activeItem = sender.getInventory().getItemInMainHand();
                    if (activeItem.getType().isAir()) continue;

                    Component cleanName;
                    ItemMeta activeMeta = activeItem.getItemMeta();
                    HoverEvent<?> hoverEvent;

                    if (activeMeta != null) {
                        if (activeMeta.hasDisplayName()) cleanName = activeMeta.displayName();
                        else if (activeMeta.hasItemName()) cleanName = activeMeta.itemName();
                        else
                            cleanName = Component.translatable(
                                    activeItem.getType().translationKey(), getFallbackItemName(activeItem));

                        hoverEvent = activeMeta.isHideTooltip()
                                ? HoverEvent.showText(MINI_MESSAGE.deserialize("<gray>(Tooltip Hidden)</gray>"))
                                : activeItem.asHoverEvent();
                    } else {
                        cleanName = Component.translatable(
                                activeItem.getType().translationKey(), getFallbackItemName(activeItem));
                        hoverEvent = activeItem.asHoverEvent();
                    }

                    final Component finalCleanName = cleanName;
                    Component itemCore = cleanName
                            .hoverEvent(hoverEvent)
                            .clickEvent(ClickEvent.callback(
                                    audience -> {
                                        if (audience instanceof Player viewer) {
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                if (activeItem.getItemMeta() instanceof BlockStateMeta blockStateMeta
                                                        && blockStateMeta.getBlockState()
                                                                instanceof ShulkerBox shulkerBox) {
                                                    Inventory previewGui = Bukkit.createInventory(
                                                            new PreviewHolder(), 27, finalCleanName);
                                                    previewGui.setContents(shulkerBox
                                                            .getInventory()
                                                            .getContents());
                                                    viewer.openInventory(previewGui);
                                                } else {
                                                    Inventory previewGui = Bukkit.createInventory(
                                                            new PreviewHolder(), InventoryType.DROPPER, finalCleanName);
                                                    previewGui.setItem(4, activeItem.clone());
                                                    viewer.openInventory(previewGui);
                                                }
                                            });
                                        }
                                    },
                                    options -> options.uses(ClickCallback.UNLIMITED_USES)
                                            .lifetime(Duration.ofMinutes(setting.previewLifetime))));

                    TagResolver itemMacroResolver = TagResolver.resolver(
                            papiResolver, TagResolver.resolver("item_preview", Tag.selfClosingInserting(itemCore)));
                    Component compiledMacro = MINI_MESSAGE.deserialize(finalMacroValue, itemMacroResolver);
                    resolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, ctx) -> Tag.selfClosingInserting(compiledMacro)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.TEXT) {
                    resolvers.add(TagResolver.resolver(normalizedAlias, (args, ctx) -> {
                        try {
                            return Tag.selfClosingInserting(MINI_MESSAGE.deserialize(finalMacroValue, papiResolver));
                        } catch (Exception e) {
                            return Tag.selfClosingInserting(
                                    LegacyComponentSerializer.legacySection().deserialize(finalMacroValue));
                        }
                    }));
                }
            }
        }
        return resolvers;
    }

    private String getFallbackItemName(ItemStack item) {
        return capitalizeWords(item.getType().name().replace("_", " ").toLowerCase());
    }

    private String capitalizeWords(String input) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (c == ' ') {
                capitalizeNext = true;
                sb.append(c);
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
