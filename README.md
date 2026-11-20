<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
  <img src="https://raw.githubusercontent.com/ViaVersion/ViaForge/master/src/main/resources/icon.png" width="150" alt="ViaForge logo">
  <h1>ViaForge</h1>
  <img src="https://img.shields.io/badge/Loaders-Forge%20%7C%20NeoForge-lightyellow" alt="Loaders: Forge and NeoForge">
  <img src="https://img.shields.io/badge/Environment-Client-purple" alt="Environment: Client">
  <a href="https://discord.gg/viaversion"><img src="https://img.shields.io/discord/316206679014244363?color=0098DB&label=Discord&logo=discord&logoColor=0098DB" alt="ViaVersion Discord"></a>
  <br>
  <a href="https://git.oxod.nl/oxod/ViaForge/actions"><img src="https://git.oxod.nl/oxod/ViaForge/actions/workflows/commitly-release.yml/badge.svg?branch=main" alt="Forgejo build status"></a>
  <a href="https://modrinth.com/mod/viaforge"><img src="https://img.shields.io/badge/dynamic/json?color=158000&label=downloads&prefix=+%20&query=downloads&url=https://api.modrinth.com/v2/project/Z6se2s8f&logo=modrinth" alt="Modrinth downloads"></a>
  <a href="https://curseforge.com/minecraft/mc-mods/viaforge"><img src="https://cf.way2muchnoise.eu/full_418933_downloads.svg" alt="CurseForge downloads"></a>

  <p>Client-side ViaVersion implementation for Minecraft Forge and NeoForge.</p>
</div>

This fork keeps the legacy and modern ViaForge implementations together on the
`main` branch. It lets a supported Minecraft client connect to servers running
other protocol versions without requiring ViaVersion on the server.

## Supported clients

| Minecraft | Loader |
| --- | --- |
| 1.8.9 | Forge |
| 1.12.2 | Forge |
| 1.16.5 | Forge |
| 1.17.1 | Forge |
| 1.18.2 | Forge |
| 1.19.2 | Forge |
| 1.19.4 | Forge |
| 1.20.1 | Forge / NeoForge |
| 1.20.4 | Forge |
| 1.20.6 | Forge / NeoForge |
| 1.21.11 | Forge / NeoForge |

For newer clients, consider
[ViaFabricPlus](https://modrinth.com/mod/viafabricplus) or
[ViaProxy](https://github.com/ViaVersion/ViaProxy).

## Supported server protocols

The bundled ViaVersion family provides translation for:

- Release protocols from Minecraft 1.0 through 26.1
- Beta b1.0 through b1.8.1
- Alpha a1.0.15 through a1.2.6
- Classic c0.0.15 through c0.30, including
  [CPE](https://wiki.vg/Classic_Protocol_Extension)
- Supported April Fools versions, including 3D Shareware, 20w14infinite, and
  25w14craftmine
- Combat Test 8c

Protocol availability is determined by the bundled ViaVersion, ViaBackwards,
ViaRewind, ViaLegacy, and ViaAprilFools versions.

## Downloads

Every push to `main` builds all client modules and creates a prerelease on the
[Forgejo releases page](https://git.oxod.nl/oxod/ViaForge/releases). Each jar is
named after its Minecraft module; install only the jar matching your client.

## Legacy extended-height support

The 1.8.9 build includes an experimental `extended-height-workaround` option,
enabled by default. It exposes the lowest 256 blocks of a modern extended-height
dimension to the legacy client and translates player, chunk, block-entity, and
block-update coordinates into that window.

This is a compatibility window, not full extended-height rendering. Blocks
outside the selected 256-block range cannot be shown by a 1.8.9 client.

## Building

Java 21 is required to build the project. The build scripts downgrade artifacts
for Minecraft versions that require Java 8.

Build every supported client:

```bash
./gradlew build --max-workers=1
```

Build one client, for example Minecraft 1.8.9:

```bash
./gradlew :viaforge-mc189:build --max-workers=1
```

Artifacts are written to `viaforge-mc<version>/build/libs`.

## Anti-cheat integration

ViaForge can send the player's protocol details using the
[Server and Player Details Protocol](https://github.com/ViaVersion/ViaVersion/wiki/Server-and-Player-Details-Protocol).
Servers must explicitly support that payload. A public server may otherwise see
the translated target protocol instead of the client's native version, which can
cause false positives in movement-sensitive anti-cheats.

The 1.8.9 build adjusts its periodic position reminder when translating to a
post-1.8 protocol to avoid GrimAC `BadPacketsE` violations caused by the vanilla
1.8 twenty-tick reminder cadence.

## Support

Report fork-specific problems on the
[Forgejo issue tracker](https://git.oxod.nl/oxod/ViaForge/issues). Include the
matching Minecraft log and the exact ViaForge commit or release name.

General ViaVersion questions can also be discussed in the
[ViaVersion Discord](https://discord.gg/viaversion).

## Disclaimer

Some servers prohibit protocol translation mods, and translated legacy behavior
can differ from a native modern client. Check the server's rules before using
ViaForge. Compatibility with every anti-cheat cannot be guaranteed.
