# HudBro

A Fabric HUD customization mod for Minecraft 1.21.11 — 20+ draggable HUD widgets, a visual HUD editor,
and a global appearance screen (theme colour, UI scale, opacity, background blur).

一个面向 Minecraft 1.21.11 的 Fabric 客户端 HUD 定制模组：20+ 个可自由开关、拖拽摆位的 HUD 元素，
配有可视化 HUD 编辑器和全局外观设置（主题色、界面缩放、透明度、背景模糊）。

## Opening the UI / 打开方式

| Key / Command | Action |
| --- | --- |
| `H` | Main screen (theme / scale / opacity / blur) |
| `Right Shift` | HUD editor |
| `/hudbro` | Main screen |
| `/hudbro editor` | HUD editor |
| `/hudbro <module>` | Toggle a module (tab completable) |
| `/hudbro <module> setting` | Open that module's settings |

Both keys can be changed in **Options → Controls → HudBro**. 两个按键都可以在「选项 → 控制」里修改。

## Features / 功能

**HUD modules** — TotemHud, CoordsHud, ArmorHud, FPS, Ping, PlayerModel, InventoryViewer, TPSHud,
IPHud, TimeHud, SpeedHud, BrandHud, KeystrokesHud, PotionEffectsHud, MovementHud, InGameTime, CpsHud,
DamageHud, ComboHud, ReachHud, TntHud.

**Main screen / 总界面**
- Theme colour picker — accent colour for every HudBro panel, border, slider and check box
- UI scale (0.5 – 2.0), panel opacity (20 – 255), background blur (0 – 10, 0 = off)

**HUD editor / 编辑器**
- Small translucent panel listing **all** HUD modules, enabled or not
- Check box toggles a HUD; drag a row or the element itself to move it (snaps to the screen centre)
- Right click a row or an element to open its settings (ESC goes back to the editor)
- Drag the panel title to move the panel, mouse wheel scrolls the list
- `Show disabled` button reveals disabled HUDs so they can be positioned; while hidden they are
  neither drawn nor mouse-pickable
- Keyboard: `↑/↓` select, `Enter`/`Space` toggle, `S` settings, `Shift`+arrows nudge by 1 px

**Module settings / 单模块设置**
- Sliders, colour pickers, toggles, enum cycling and text input
- Panel auto-fits the screen and scrolls when a module has many rows; `Done`/`Back` button + ESC

**Extra modules / 附加模块**
- `Nick` — fake name in chat, name tags, tab list, signs, scoreboard and death messages
- `Chams` — custom end crystal tint / alpha / scale / spin, plus per layer render switches
  (`OuterFrame`, `InnerFrame`, `Core`)
- `PopChams` — fading copy of a player where their totem popped (your own included)

## Config

| File | Contents |
| --- | --- |
| `config/hudbro.json` | module enabled state and every module setting |
| `config/hudbro-global.json` | theme colour, UI scale, opacity, blur, editor options |

Both are written automatically when you change something and on game shutdown.

## Dependencies

- Minecraft 1.21.11
- Fabric Loader >= 0.19.3
- Fabric API

## Notes

- Client side only.
- The 1.21.11 render pipeline removed the immediate mode entity hooks, so `Chams` only covers the end
  crystal and the per-layer switches instead of the original through-wall entity chams.
