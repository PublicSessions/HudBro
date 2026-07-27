# HudBro

A Fabric HUD customization mod for Minecraft.

## Features

- 15+ HUD modules: Totems, Coordinates, Armor Durability, FPS, Ping, Player Model, Inventory Viewer, TPS, IP, Time, Speed, Server Brand, Keystrokes, Potion Effects
- Drag & drop positioning with snap-to-center alignment
- In-game settings GUI with sliders, color pickers, toggles, and text input
- Customizable text formats with `{x}` `{Fps}` `{Ping}` `{Speed}` `{Tps}` `{Ip}` `{Time}` `{Brand}` placeholders
- Per-module color, background, shadow, and scale settings
- Automatic config save & restore

## Commands

```
/hudbro <module>          — Toggle a HUD module on/off
/hudbro <module> setting  — Open the settings panel for that module
```

Available modules: `TotemHud`, `CoordsHud`, `ArmorHud`, `FPS`, `Ping`, `PlayerModel`, `InventoryViewer`, `TPS`, `IP`, `Time`, `Speed`, `Brand`, `Keystrokes`, `PotionEffects`

## How to Use

1. **Enable a module**: `/hudbro TotemHud`
2. **Move it**: Left-click and drag the HUD element
3. **Configure it**: Right-click the HUD element, or use `/hudbro TotemHud setting`
4. **Customize labels** (Keystrokes): Right-click `KeyW`/`KeyA`/etc. in settings to edit key text
5. **Customize formats** (CoordsHud/FPS/Ping/Speed/TPS/IP/Time/Brand): Right-click `Format` in settings to edit the display text

## Dependencies

- Minecraft 1.21.11
- Fabric Loader >=0.19.3
- Fabric API

## Config

Settings are stored in `config/hudbro.json`.
