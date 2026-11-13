[![Yapper Banner](https://raw.githubusercontent.com/MaboroshiKobo/branding/refs/heads/main/projects/yapper/banners/yapper_2048.png)](https://docs.maboroshi.org/projects/yapper)

<div align="center">
  <p>
    <img alt="paper" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/paper_vector.svg">
    <img alt="purpur" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/purpur_vector.svg">
    <img alt="spigot" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/unsupported/spigot_vector.svg">
  </p>

  <p>
    <a href="https://github.com/MaboroshiKobo/Yapper"><img alt="github" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/github_vector.svg"></a>
    <a href="https://hangar.papermc.io/Maboroshi/Yapper"><img alt="hangar" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/hangar_vector.svg"></a>
    <a href="https://modrinth.com/plugin/yapper"><img alt="modrinth" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_vector.svg"></a>
  </p>

  <p>
    <a href="https://docs.maboroshi.org/projects/yapper"><img alt="documentation" height="56" src="https://raw.githubusercontent.com/MaboroshiKobo/branding/refs/heads/main/socials/128x/domain_icon_bg.png"></a>
    <a href="https://discord.maboroshi.org"><img alt="discord" height="56" src="https://raw.githubusercontent.com/MaboroshiKobo/branding/refs/heads/main/socials/128x/discord_icon_bg.png"></a>
  </p>
</div>

## MiniMessage chat channels, clickable item previews, and Towny & DiscordSRV support

Yapper is a modern chat management plugin built to replace bulky, outdated chat and item-sharing setups in a single lightweight package. Combine global and proximity channels, reusable MiniMessage tags, customizable private messaging, and clickable inventory previews—all out of the box without needing PacketEvents or ProtocolLib.

## Features

- Run global channels or local proximity chat with custom block radii, quick one-off channel messages, and per-player hide/show toggles.
- Style every channel using MiniMessage, reusable custom tags, permission-based formats, and PlaceholderAPI.
- Share held items (including shulker box contents), full inventories, ender chests, and live PlaceholderAPI stats inline—with clickable previews that expire automatically and require **no ProtocolLib or PacketEvents**.
- Connect directly with Towny to route town, nation, and alliance channels without extra configuration.
- Bridge chat with DiscordSRV, keeping Discord role colors, linked player placeholders, and clear reply indicators intact in-game.
- Replace basic `/msg` and `/r` commands with customizable private messaging and automatic daily chat logs.

## Prerequisites

Yapper is compatible with the following plugins:

- [Towny](https://townyadvanced.github.io) (Optional for town, nation, and alliance channels)
- [DiscordSRV](https://www.discordsrv.com) (Optional for Discord chat sync, in-game reply indicators, and linked account styling)
- [PlaceholderAPI](https://placeholderapi.com) (Optional)
- [PluginUpdater](https://modrinth.com/plugin/plugin-updater) (Optional for update checking and automatic updates)

## Documentation & Support

For configurations, commands, and permissions, check out our [wiki](https://docs.maboroshi.org/projects/yapper). For bugs, questions, or updates, visit our [Discord server](https://discord.maboroshi.org) or open a [GitHub Issue](https://github.com/MaboroshiKobo/Yapper/issues).

## Statistics

This plugin utilizes [bStats](https://bstats.org/plugin/bukkit/Yapper/32126) to collect anonymous usage metrics.

![bStats Metrics](https://bstats.org/signatures/bukkit/Yapper.svg)

## Building

To build the project from source, ensure you have a Java 25 environment configured.

```bash
./gradlew build
```
