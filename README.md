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
    <a href="https://docs.maboroshi.org/projects/yapper"><img alt="generic" height="56" src="https://raw.githubusercontent.com/MaboroshiKobo/branding/refs/heads/main/socials/128x/domain_icon_bg.png"></a>
    <a href="https://discord.maboroshi.org"><img alt="discord-singular" height="56" src="https://raw.githubusercontent.com/MaboroshiKobo/branding/refs/heads/main/socials/128x/discord_icon_bg.png"></a>
  </p>
</div>

## Feature-rich chat channels and interactive inline macros

Yapper is a modern chat management plugin designed to bring flexible channel controls and rich interactivity to server communication. It gives administrators full control over global and proximity-based chat channels, dynamic permission-based formats, and customizable inline macros for players.

## Features

* Create custom chat channels with configurable local block radius or global ranges.
* Design dynamic permission-based chat formats using MiniMessage syntax and reusable tags.
* Set up inline macros for items, inventories, ender chests, and any other stats (via PlaceholderAPI) with interactive hover previews.
* Give players full channel control to easily switch active rooms, view channel info, or hide unwanted channels.

## Prerequisites

Yapper is compatible with the following plugins:

* [Towny](https://townyadvanced.github.io) (Optional for town integration)
* [DiscordSRV](https://www.discordsrv.com) (Optional for Discord integration)
* [PlaceholderAPI](https://placeholderapi.com) (Optional)
* [PluginUpdater](https://modrinth.com/plugin/plugin-updater) (Optional for update checking and automatic updates)

## Documentation & Support

For configurations, commands, and permissions, check out our [wiki](https://docs.maboroshi.org/projects/yapper). For bugs, questions, or updates, visit our [Discord server](https://discord.maboroshi.org) or open a [GitHub Issue](https://github.com/MaboroshiKobo/Yapper/issues).

### Statistics

This plugin utilizes [bStats](https://bstats.org/plugin/bukkit/Yapper/32126) to collect anonymous usage metrics.

![bStats Metrics](https://bstats.org/signatures/bukkit/Yapper.svg)

## Building

To build the project from source, ensure you have a Java 25 environment configured.

```bash
./gradlew build
```
