package org.maboroshi.yapper.renderer;

import io.papermc.paper.chat.ChatRenderer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.config.settings.ChannelTemplate;
import org.maboroshi.yapper.config.settings.ChannelTemplate.ChannelFormat;
import org.maboroshi.yapper.config.settings.MainConfig;
import org.maboroshi.yapper.menu.PreviewHolder;

public class YapperRenderer implements ChatRenderer.ViewerUnaware {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final Yapper plugin;
    private final ChannelTemplate channel;

    public YapperRenderer(Yapper plugin, ChannelTemplate channel) {
        this.plugin = plugin;
        this.channel = channel;
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message) {
        ChannelFormat matchedFormat = null;
        for (ChannelFormat format : channel.formats.values()) {
            if (format.permission == null || format.permission.isEmpty() || source.hasPermission(format.permission)) {
                matchedFormat = format;
                break;
            }
        }

        if (matchedFormat == null) {
            return message;
        }

        String layoutTemplate = matchedFormat.format;
        List<TagResolver> layoutResolvers = new ArrayList<>();
        boolean placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if (placeholderApiEnabled) {
            layoutResolvers.add(TagResolver.resolver("papi", (args, context) -> {
                String papiQuery = args.popOr("The papi tag requires an internal identifier argument")
                        .value();
                String papiText = PlaceholderAPI.setPlaceholders(source, "%" + papiQuery + "%");
                Component placeholderComponent =
                        LegacyComponentSerializer.legacySection().deserialize(papiText);
                return Tag.selfClosingInserting(placeholderComponent);
            }));
        } else {
            layoutResolvers.add(
                    TagResolver.resolver("papi", (args, context) -> Tag.selfClosingInserting(Component.empty())));
        }

        for (Map.Entry<String, String> tagEntry :
                plugin.getConfigManager().getMainConfig().customTags.entrySet()) {
            String processedTagValue = tagEntry.getValue();
            if (placeholderApiEnabled) {
                processedTagValue = PlaceholderAPI.setPlaceholders(source, processedTagValue);
            }
            Component tagComponent = MINI_MESSAGE.deserialize(processedTagValue);
            layoutResolvers.add(Placeholder.component(tagEntry.getKey(), tagComponent));
        }

        List<TagResolver> playerMsgResolvers = new ArrayList<>();
        for (Map.Entry<String, MainConfig.MacroSetting> macroEntry :
                plugin.getConfigManager().getMainConfig().macros.entrySet()) {
            String rawMacroName = macroEntry.getKey();
            MainConfig.MacroSetting setting = macroEntry.getValue();
            String[] macroAliases = rawMacroName.split("\\|");

            for (String alias : macroAliases) {
                String normalizedAlias = alias.trim().toLowerCase();

                if (!source.hasPermission("yapper.macro." + normalizedAlias)) {
                    continue;
                }

                String processedMacroValue = setting.value;
                if (placeholderApiEnabled) {
                    processedMacroValue = PlaceholderAPI.setPlaceholders(source, processedMacroValue);
                }
                final String finalMacroValue = processedMacroValue;

                if (setting.action == MainConfig.MacroAction.INVENTORY) {
                    Component invComponent = MINI_MESSAGE
                            .deserialize(finalMacroValue)
                            .clickEvent(ClickEvent.callback(
                                    audience -> {
                                        if (audience instanceof Player viewer) {
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                Inventory previewGui = Bukkit.createInventory(
                                                        new PreviewHolder(),
                                                        45,
                                                        MINI_MESSAGE.deserialize("<dark_gray>" + source.getName()
                                                                + "'s Inventory</dark_gray>"));
                                                previewGui.setContents(
                                                        source.getInventory().getContents());
                                                viewer.openInventory(previewGui);
                                            });
                                        }
                                    },
                                    options -> options.uses(ClickCallback.UNLIMITED_USES)
                                            .lifetime(Duration.ofMinutes(5))));
                    playerMsgResolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, context) -> Tag.selfClosingInserting(invComponent)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.ENDERCHEST) {
                    Component ecComponent = MINI_MESSAGE
                            .deserialize(finalMacroValue)
                            .clickEvent(ClickEvent.callback(
                                    audience -> {
                                        if (audience instanceof Player viewer) {
                                            Bukkit.getScheduler().runTask(plugin, () -> {
                                                Inventory previewGui = Bukkit.createInventory(
                                                        new PreviewHolder(),
                                                        27,
                                                        MINI_MESSAGE.deserialize("<dark_gray>" + source.getName()
                                                                + "'s Enderchest</dark_gray>"));
                                                previewGui.setContents(
                                                        source.getEnderChest().getContents());
                                                viewer.openInventory(previewGui);
                                            });
                                        }
                                    },
                                    options -> options.uses(ClickCallback.UNLIMITED_USES)
                                            .lifetime(Duration.ofMinutes(5))));
                    playerMsgResolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, context) -> Tag.selfClosingInserting(ecComponent)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.ITEM) {
                    ItemStack activeItem = source.getInventory().getItemInMainHand();
                    if (activeItem.getType().isAir()) {
                        continue;
                    }

                    Component cleanName;
                    ItemMeta activeMeta = activeItem.getItemMeta();
                    HoverEvent<?> hoverEvent;

                    if (activeMeta != null) {
                        if (activeMeta.hasDisplayName()) {
                            cleanName = activeMeta.displayName();
                        } else if (activeMeta.hasItemName()) {
                            cleanName = activeMeta.itemName();
                        } else {
                            cleanName =
                                    Component.translatable(activeItem.getType().translationKey());
                        }
                        hoverEvent = activeMeta.isHideTooltip()
                                ? HoverEvent.showText(MINI_MESSAGE.deserialize("<gray>(Tooltip Hidden)</gray>"))
                                : activeItem.asHoverEvent();
                    } else {
                        cleanName = Component.translatable(activeItem.getType().translationKey());
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
                                            .lifetime(Duration.ofMinutes(5))));

                    Component compiledMacro = MINI_MESSAGE.deserialize(
                            finalMacroValue, TagResolver.resolver("item_preview", Tag.selfClosingInserting(itemCore)));
                    playerMsgResolvers.add(TagResolver.resolver(
                            normalizedAlias, (args, context) -> Tag.selfClosingInserting(compiledMacro)));
                    continue;
                }

                if (setting.action == MainConfig.MacroAction.TEXT) {
                    playerMsgResolvers.add(TagResolver.resolver(normalizedAlias, (args, context) -> {
                        try {
                            return Tag.selfClosingInserting(MINI_MESSAGE.deserialize(finalMacroValue));
                        } catch (Exception e) {
                            return Tag.selfClosingInserting(
                                    LegacyComponentSerializer.legacySection().deserialize(finalMacroValue));
                        }
                    }));
                }
            }
        }

        String plainTextMessage = PlainTextComponentSerializer.plainText().serialize(message);
        MiniMessage playerChatParser = plugin.getYapperUtils().getChatParser(source);
        Component formattedPlayerMessage =
                playerChatParser.deserialize(plainTextMessage, TagResolver.resolver(playerMsgResolvers));

        layoutResolvers.add(Placeholder.parsed("name", source.getName()));
        layoutResolvers.add(Placeholder.component("displayname", sourceDisplayName));
        layoutResolvers.add(Placeholder.parsed("channel", channel.name));
        layoutResolvers.add(Placeholder.parsed("world", source.getWorld().getName()));
        layoutResolvers.add(Placeholder.component("message", formattedPlayerMessage));

        TagResolver finalRegistry =
                TagResolver.builder().resolvers(layoutResolvers).build();
        return MINI_MESSAGE.deserialize(layoutTemplate, finalRegistry);
    }
}
