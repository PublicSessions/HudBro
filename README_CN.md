# HudBro 中文文档

一个面向 **Minecraft 1.21.11** 的 Fabric 客户端 HUD 定制模组：20+ 个可自由开关、拖拽摆位的 HUD 元素，配有可视化 HUD 编辑器和全局外观设置（主题色、界面缩放、透明度、背景模糊）。

## 打开方式

| 按键 / 命令 | 功能 |
| --- | --- |
| `H` | 打开主界面（主题色 / 缩放 / 透明度 / 模糊） |
| `Right Shift` | 打开 HUD 编辑器 |
| `/hudbro` | 打开主界面 |
| `/hudbro editor` | 打开 HUD 编辑器 |
| `/hudbro <模块名>` | 开关指定模块（支持 Tab 补全） |
| `/hudbro <模块名> setting` | 打开该模块的设置界面 |

两个按键都可以在 **选项 → 控制 → HudBro** 中自定义。

## 功能特性

### HUD 模块
TotemHud、CoordsHud、ArmorHud、FPS、Ping、PlayerModel、InventoryViewer、TPSHud、IPHud、TimeHud、SpeedHud、BrandHud、KeystrokesHud、PotionEffectsHud、MovementHud、InGameTime、CpsHud、DamageHud、ComboHud、ReachHud、TntHud。

### 主界面
- **主题色选择器** —— 为所有 HudBro 面板、边框、滑块、复选框统一设置强调色
- **界面缩放** (0.5 – 2.0)
- **面板透明度** (20 – 255)
- **背景模糊** (0 – 10，0 = 关闭)

### HUD 编辑器
- 半透明面板，列出 **所有** HUD 模块（无论启用与否）
- 复选框切换 HUD 开关；拖拽列表项或元素本身即可移动位置（自动吸附屏幕中心）
- 右键列表项或元素打开其设置（ESC 返回编辑器）
- 拖拽面板标题移动面板，鼠标滚轮滚动列表
- `Show disabled` 按钮显示已禁用的 HUD 以便定位；隐藏时既不渲染也不可点选
- 键盘操作：`↑/↓` 选择、`Enter`/`Space` 切换、`S` 打开设置、`Shift`+方向键 微调 1 像素

### 模块设置
- 支持滑块、颜色选择器、开关、枚举循环、文本输入
- 面板自适应屏幕并在行数过多时滚动；`Done`/`Back` 按钮 + ESC 关闭

### 附加模块
- **Nick** —— 在聊天、实体名牌、玩家列表、告示牌、记分板和死亡消息中显示假名
- **Chams** —— 自定义末影水晶渲染：颜色、透明度、缩放、旋转速度、上下浮动，支持逐层开关（外框、内框、核心）
- **PopChams** —— 图腾弹出时在原位留下逐渐消失的玩家模型副本（含自己）

## 配置文件

| 文件 | 内容 |
| --- | --- |
| `config/hudbro.json` | 所有模块的启用状态及其设置 |
| `config/hudbro-global.json` | 主题色、界面缩放、透明度、模糊、编辑器选项 |

修改设置或游戏关闭时自动写入。

## 依赖

- Minecraft 1.21.11
- Fabric Loader >= 0.19.3
- Fabric API

## 注意事项

- 仅客户端模组，服务端无需安装
- 1.21.11 的渲染管线移除了即时模式实体钩子，因此 `Chams` 仅覆盖末影水晶及逐层开关，不再包含原版的透视实体渲染功能