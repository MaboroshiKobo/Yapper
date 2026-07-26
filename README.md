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

## Custom chat channels, interactive inline macros, and proximity chat

Yapper is a lightweight chat management plugin that lets players switch target channels, share items, and open inline inventory previews directly from chat. It gives administrators full control over local proximity ranges, permission-based MiniMessage layouts, and custom macros to build a clean, modern chat experience on a server.

## Features

* Create global chat channels or local proximity channels with configurable block radii.
* Design dynamic, permission-based chat formats using MiniMessage syntax, custom tags, and PlaceholderAPI.
* Set up interactive inline macros that let players share held items, inventories, ender chests, or any custom PlaceholderAPI stat with hover previews.
* Connect directly with Towny to support dedicated town, nation, and alliance channels out of the box.
* Allow players to switch active channels, send quick one-off messages, or toggle channel visibility with hide and show commands.

## Prerequisites

Yapper is compatible with the following plugins:

* [Towny](https://townyadvanced.github.io) (Optional for town integration)
* [DiscordSRV](https://www.discordsrv.com) (Optional for basic Discord integration)
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
