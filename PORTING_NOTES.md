# bro/ → HudBro 迁移说明（Minecraft 1.21.11 / Mojang 官方映射）

原来的 `bro/` 文件夹是从另一个 mod（包名 `dev.lumn`，Yarn 映射）反编译出来的源码。
本次迁移把它按 HudBro 的包结构（`com.ciallo`）、Mojang 官方映射和 1.21.11 的渲染管线
重写了一遍。`bro/` 原文件保持原样未改动。

## 1. 新增 / 修改的文件

### 模块（`src/main/java/com/ciallo/module/render/`）
| 文件 | 说明 |
| --- | --- |
| `Nick.java` | 假名模块，由 `bro/Nick.java` 移植 |
| `Chams.java` | 水晶 / 实体上色模块，由 `bro/Chams.java` 移植（功能有裁剪，见 §3） |
| `PopChams.java` | 图腾爆开特效模块，由 `bro/PopChams.java` 移植 |

### Mixin（`src/main/java/com/ciallo/mixin/`）
| 新文件 | 对应 `bro/mixins/` 原文件 | 作用 |
| --- | --- | --- |
| `MixinChatComponent.java` | `MixinChatHud.java` | Nick：收到的聊天消息替换名字 |
| `MixinClientPacketListener.java` | `MixinClientPlayNetworkHandler.java` | Nick：发出的聊天替换名字；PopChams：图腾事件检测 |
| `MixinPlayerTabOverlay.java` | `PlayerListHudMixin.java` | Nick：Tab 列表替换名字 |
| `MixinPlayerTeam.java` | `MixinTeam.java` | Nick：计分板队伍名替换 |
| `MixinSignText.java` | `MixinSignText.java` | Nick：告示牌文字替换 |
| `MixinDeathScreen.java` | `MixinDeathScreen.java` | Nick：死亡界面消息替换 |
| `MixinEntityRenderer.java` | `MixinEntityRenderer.java` | Nick：自己头顶名牌替换 |
| `MixinEndCrystalRenderer.java` | `MixinEndCrystalEntityRenderer.java` | Chams：末影水晶自定义渲染 + 三层开关 |
| ~~`MixinLivingEntityRenderer.java`~~ | `MixinLivingEntityRenderer.java` | 已删除（实体透视描边功能按要求移除） |
| `MixinLevelRenderer.java` | `MixinWorldRenderer.java` | PopChams 渲染挂点 |

### 基础设施
- `com/ciallo/setting/EnumSetting.java`（新增，用于 PopChams 的 Ease 设置）
- `com/ciallo/util/Easing.java`、`com/ciallo/util/Animation.java`（新增，移植自原 mod 同名工具）
- `com/ciallo/util/RenderUtil.java`（新增，1.21.11 的 `CustomGeometryRenderer` 回调只给
  `PoseStack.Pose`，这里用 `new PoseStack(); last().set(pose)` 还原成 `PoseStack` 再交给
  `ModelPart#render` / `Model#renderToBuffer`）
- `com/ciallo/config/HudConfig.java`（新增 EnumSetting 的读写）
- `com/ciallo/gui/HudSettingsScreen.java`（改为接受任意 `Module`，新增枚举设置控件）
- `com/ciallo/command/CommandManager.java`（任何模块都能用 `hudbro <模块> setting` 打开设置，
  命令补全也改成列出全部模块）
- `com/ciallo/HudBro.java`（注册三个模块、客户端 tick 回调）
- `src/main/resources/hudbro.mixins.json`（注册全部 mixin，共 9 个）

## 2. Yarn → Mojang 映射对照（本次用到的）

| Yarn | Mojang 1.21.11 |
| --- | --- |
| `MinecraftClient` | `net.minecraft.client.Minecraft` |
| `Text` / `MutableText` | `net.minecraft.network.chat.Component` / `MutableComponent` |
| `ChatHud` | `net.minecraft.client.gui.components.ChatComponent` |
| `PlayerListHud` | `net.minecraft.client.gui.components.PlayerTabOverlay` |
| `PlayerListEntry` | `net.minecraft.client.multiplayer.PlayerInfo` |
| `Team` | `net.minecraft.world.scores.PlayerTeam` |
| `ClientPlayNetworkHandler` | `net.minecraft.client.multiplayer.ClientPacketListener` |
| `sendChatMessage(String)` | `sendChat(String)` |
| `EntityStatusS2CPacket` | `net.minecraft.network.protocol.game.ClientboundEntityEventPacket` |
| `EndCrystalEntityRenderer` | `net.minecraft.client.renderer.entity.EndCrystalRenderer` |
| `EndCrystalEntity` | `net.minecraft.world.entity.boss.enderdragon.EndCrystal` |
| `WorldRenderer` | `net.minecraft.client.renderer.LevelRenderer` |
| `LivingEntityRenderer` | 同名，但 `render(...)` 已变为 `submit(...)` |
| `MatrixStack` | `com.mojang.blaze3d.vertex.PoseStack`（`push`→`pushPose` 等） |
| `RenderLayer.getEntityTranslucent(id)` | `RenderTypes.entityTranslucent(id)`（新包 `...renderer.rendertype`） |
| `ItemRenderer.getItemGlintConsumer` | `ItemRenderer.getFoilBuffer` |
| `OverlayTexture.DEFAULT_UV` | `OverlayTexture.NO_OVERLAY` |
| `PlayerEntityRenderer` | `AvatarRenderer` + `AvatarRenderState` |

## 3. 因为 1.21.11 渲染管线重写而做的行为变更

1.21.11 删除了大量即时模式渲染 API，原代码里的以下调用**已经不存在**：
`RenderSystem.setShaderColor / enableBlend / enableDepthTest / polygonOffset / depthMask /
setShader / setShaderTexture / applyModelViewMatrix`，`LivingEntityRenderer.render`，
`LevelRenderer.renderEntity`。因此：

- **Chams 水晶**：原代码取消 `render` 后自己画模型；现在取消 `EndCrystalRenderer#submit`，
  通过 `SubmitNodeCollector#submitCustomGeometry` 提交水晶模型的各个 `ModelPart`，
  颜色 / 缩放 / 转速 / 弹跳 / Y 偏移都保留。**注意**：自定义渲染时不再绘制治疗光束
  （原代码在最后调用 `super.render` 画光束）。
- **Chams 实体**：原来的"穿墙 + 半透明重绘"无法实现（渲染状态里没有 alpha 字段，深度测试写死在
  `RenderPipeline` 里）。现在改为使用渲染状态自带的 `outlineColor` 做**彩色描边**，
  并把 `LevelRenderState.haveGlowingEntities` 打开，效果是"穿墙可见的彩色轮廓"。
- **Chams 手部颜色 / 发光（Hand / HandColor / HandGlow / Glow\*）**：没有替代 API，
  相关设置已移除。
- **Chams 其他被移除的设置**：`Depth`、`ChamsTexture`、`Glint`、`Texture`、`SpinSync` 之外的
  纹理类开关、`Wireframe / WireColor`、`PlayerCustom / PlayerFill / PlayerLine / PlayerScale`。
- **Nick 告示牌**：原代码还会改**发出**的 `UpdateSignC2SPacket`（用反射猜方法名，本来就不稳）。
  现在只保留显示侧（`SignText#getMessage`）的替换，效果就是你看到的告示牌文字被替换。
- **PopChams**：原代码用 `ModelPlayer` + 立即模式画半透明玩家模型；现在改成
  `EntityRenderDispatcher#extractEntity` 取到 `AvatarRenderState`，再用
  `SubmitNodeCollector#submitCustomGeometry` 通过 `model.renderToBuffer(..., color)` 渲染，
  仍然支持淡出（alpha）、Y 偏移、缩放、旋转。`Line` 设置保留但当前只使用 `Fill`
  （1.21.11 的模型渲染只有单次着色，画不出原版那种描边线条）。
- **其他原 mixin 里引用的模块**（`NoRender`、`HighLight`、`Freecam`、`AspectRatio`、`Zoom`、
  `SkyBox`、`ShaderModule`、`InteractTweaks`、`AntiPacket`、`ExtraTab`、`CustomDeathText`、
  `NameTags`、`FakeName`、`ParrotPet`、`ClientSetting`、`HUD`、`RotationManager`）在 HudBro 中
  并不存在，也没有本次移植的对应物，因此这些逻辑未包含（即 `MixinGameRenderer`、
  `MixinWorldRenderer`、`MixinChatHud`、`PlayerListHudMixin` 中与它们相关的部分）。

## 4. 验证状态

### 已通过：`./gradlew build` 成功（compileJava + remapJar）

编译过程中发现并修掉的 1.21.11 差异：

| 位置 | 原写法 | 正确写法 |
| --- | --- | --- |
| `Camera` | `getPosition()` | `position()` |
| `GameProfile`（authlib 7.0.61 已改为 record） | `getName()` | `name()` |
| 村民类 | `net.minecraft.world.entity.npc.Villager` | `net.minecraft.world.entity.npc.villager.Villager` |
| 流浪商人 | `net.minecraft.world.entity.npc.WanderingTrader` | `net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader` |

### 已通过：mixin 目标静态校验

`build/libs` 里的产物中，所有 mixin 注解里的方法名都已由 Loom 正确重映射成 intermediary，
即运行期能解析到目标方法（含编译器生成的桥接方法歧义问题，已通过写全描述符解决）：

```
MixinChatComponent        addMessage -> method_44811(Component, MessageSignature, GuiMessageTag) / method_1812(Component)
MixinClientPacketListener sendChat -> method_11148 ; handleEntityEvent -> method_45729
MixinDeathScreen          <init>
MixinEndCrystalRenderer   submit(EndCrystalRenderState,PoseStack,SubmitNodeCollector,CameraRenderState) -> method_3908
MixinEntityRenderer       getNameTag -> method_62426
MixinLevelRenderer        submitEntities -> method_72916
MixinLivingEntityRenderer extractRenderState(LivingEntity,LivingEntityRenderState,float) -> method_62355
MixinPlayerTabOverlay     getNameForDisplay -> method_1918
MixinPlayerTeam           getDisplayName -> method_1140
MixinSignText             getMessage -> method_49859
```

同时用 `javap` 直接对映射后的 1.21.11 客户端 jar 核实过：
`EntityEvent.PROTECTED_FROM_DEATH = 35`、`EntityRenderState.NO_OUTLINE = 0`、
`ModelPart#render(PoseStack, VertexConsumer, int, int, int)`、
`Model#renderToBuffer(PoseStack, VertexConsumer, int, int, int)`、
`RenderTypes.entityTranslucent(Identifier)`、
`OrderedSubmitNodeCollector#submitCustomGeometry(PoseStack, RenderType, CustomGeometryRenderer)`、
`SubmitNodeCollector$CustomGeometryRenderer#render(PoseStack$Pose, VertexConsumer)`、
`OverlayTexture.NO_OVERLAY`、`LevelRenderState#cameraRenderState/haveGlowingEntities`、
`PlayerModel#setupAnim(AvatarRenderState)`、`LivingEntityRenderer#getModel()`。

### 已通过：`./gradlew runClient` 实际启动

第一次启动时 mixin 应用失败并崩溃：

```
InvalidInjectionException: @ModifyVariable handler before super() invocation must be static
  in injector DeathScreen::hudbro$replaceNickInDeathMessage
```

原因：`DeathScreen` 的 `@ModifyVariable` 注入点在 `super()` 之前，Mixin 要求这种 handler
必须是 `static`。把 `MixinDeathScreen#hudbro$replaceNickInDeathMessage` 改成 `private static`
之后重新启动，客户端正常进入主菜单：

```
[hudbro] HudBro initialized!
```

日志中没有任何 mixin 应用错误（只剩开发环境正常的 401 登录 / Realms 报错），
说明 10 个 mixin 全部注入成功。

### 仍未在实机操作中确认：观感

游戏能启动、mixin 能注入，但以下效果需要在游戏里实际触发一次才能确认：

- `EntityRenderState#outlineColor` + `LevelRenderState#haveGlowingEntities` 是否真能让描边出现。
- PopChams 的模型朝向/缩放（用了 `scale(-1,-1,1)` + `translate(0,-1.501,0)` 的通用惯例）。
- Tab 列表/告示牌/计分板/死亡界面的假名替换是否在所有场景生效。

## 5. 第二轮修复（实机反馈）

| 反馈 | 原因 | 修复 |
| --- | --- | --- |
| Nick 改名时打字母变成空格、显示的名字也是空格 | `HudSettingsScreen#charTyped` 用反射调用 `getCodepoint()`，1.21.11 的 `CharacterEvent` 是 record，方法名是 `codepoint()`，反射失败后回退成 `' '` | 直接调用 `event.codepoint()`，并用 `Character.toChars` 支持完整码点 |
| 水晶外面多了一个旋转的水晶 | 1.21.11 把水晶的自转/弹跳全部移进了 `EndCrystalModel#setupAnim(state)`，而移植时又按旧版逻辑手工叠加了 spin / 45° 旋转 / 1.5+bounce/2 平移，等于动画做了两遍，模型部件还停在初始姿势 | 按反编译出来的原版结构重写：只改渲染状态（`ageInTicks`、`showsBottom`）、外层 pose（scale / Y 偏移）与 tint，其余交给 `submitModel`；同时补回了光束 `EnderDragonRenderer.submitCrystalBeams` |
| PopChams 自己看不到自己的图腾特效 | `NoSelf` 默认 `true` | 默认改成 `false`（仍可手动打开） |
| 设置界面超出屏幕、要改界面尺寸才能看全 | 面板高度 = 设置数量 × 30 无上限；且 `mouseScrolled(double,double,double)` 不是 `Screen` 的重载（1.21.11 是 4 参数），滚动回调根本没被调用过 | 面板高度钳制在屏幕内（`clampPanelHeight`），列表区可滚动，改用正确的 `mouseScrolled(double,double,double,double)` 重载并加了滚动条 |

水晶相关设置：`Crystal`、`Custom`、`Color`、`Scale`、`SpinSpeed`、`SpinSync`、`YOffset`
（`Custom` 关闭或颜色不透明时使用原版 `entityCutoutNoCull` 渲染类型，观感与原版一致）。

## 6. 第三轮修改：去掉实体透视、还原三层渲染开关

- **删除 Chams 的透视生物功能**：删掉 `MixinLivingEntityRenderer.java` 及其在 `hudbro.mixins.json`
  里的注册，`MixinLevelRenderer` 不再改写 `LevelRenderState#haveGlowingEntities`；
  Chams 模块里的 `ThroughWall`、`Players`、`Mobs`、`Animals`、`Villagers`、`Fill`、`VanillaAlpha`
  设置一并移除。现在 Chams 只负责末影水晶。
- **还原三层渲染开关**：新增 `OuterFrame`、`InnerFrame`、`Core` 三个布尔开关（默认都开，
  对应原版的 `outerFrame` / `innerFrame` / `core` 的 `injectBoolean(true)`），
  实现方式是设置 `EndCrystalModel` 对应 `ModelPart` 的 `skipDraw`：

  ```java
  this.model.outerGlass.skipDraw = !module.isOuterFrame();
  this.model.innerGlass.skipDraw = !module.isInnerFrame();
  this.model.cube.skipDraw      = !module.isCore();
  ```

  用 `skipDraw` 而不是 `visible`，是因为反编译看到 `ModelPart#render` 在 `visible == false` 时
  **直接 return（子部件也不画）**，而 `skipDraw == true` 只跳过自己的 cuboid、子部件照画，
  这样三层开关互不影响（例如关掉最外层、保留里面两层旋转渲染）。

现在的 Chams 设置：`Crystal`、`Custom`、`Color`、`OuterFrame`、`InnerFrame`、`Core`、
`SpinSync`、`Scale`、`SpinSpeed`、`YOffset`。

建议的实测步骤：进单人世界 → `/hudbro Nick` 开启假名并把 `Name` 设成别的名字 → 发一条含自己名字的聊天；
`/hudbro Chams` 开 `Crystal`，然后分别关 `OuterFrame` / `InnerFrame` / `Core` 看水晶三层是否独立隐藏。

## 7. 第四轮：按键 + 列表式 HUD 编辑器

### 打开方式
- **按键**：默认 **右 Shift** 打开编辑器（可在「选项 → 控制」的 `HudBro` 分类里改键），
  注册在 `com/ciallo/util/KeyBinds.java`。
- **指令**：`/hudbro` 直接打开编辑器；`/hudbro <模块>` 开关模块；`/hudbro <模块> setting` 打开设置。
- 仍然可以开关 `HudEditor` 模块打开编辑器。

### 编辑器界面（重写 `HudEditorScreen`）
- 左侧一个 **132px 宽的半透明面板**，列出**全部** HUD 模块（开着的和没开的都在里面），
  面板高度按屏幕自适应，超出时用滚轮滚动，右侧有滚动条。
- **勾选框**（左侧小方块）左键点击：开关该 HUD；绿色=开，灰色=关。
- **拖动**：在列表里按住某一行左右拖 → 移动该 HUD；也可以直接在画布上拖动 HUD 本体。
  没开启的 HUD 会以半透明+红字名字显示，一样能拖动摆位。靠近屏幕中线会自动吸附。
- **右键** 列表行（或画布上的 HUD）→ 打开该模块的设置界面；设置界面里按 ESC 会**返回编辑器**。
- 背景 `0x40000000`、面板 `0x99101010`，都是半透明的，编辑时能看到游戏画面。

### 踩到的坑
`KeyBindingHelper.registerKeyBinding` **必须在 mod 初始化阶段调用**。
一开始按键绑定写成了懒加载（第一次按键时才初始化 `KeyBinds` 类），运行时报：

```
java.lang.IllegalStateException: GameOptions has already been initialised
```

所以加了 `KeyBinds.init()` 并在 `HudBro.onInitializeClient()` 里最先调用。

另外 `HudSettingsScreen` 增加了可选的 `parent` 参数（`new HudSettingsScreen(module, this)`），
ESC 时返回上一级界面而不是直接关掉。

## 8. 第五轮：总体界面（主题 / 大小 / 透明度 / 模糊度）

### 打开方式
- `/hudbro` → **总界面**（HudMainScreen）；`/hudbro editor` → HUD 编辑器；`/hudbro settings` 同 `/hudbro`。
- 按键：**H** = 总界面，**右 Shift** = HUD 编辑器（都在「选项 → 控制」的 `HudBro` 分类里可改）。

### 总界面（`HudMainScreen`）
| 控件 | 说明 |
| --- | --- |
| `Theme Color` | 点色块打开取色器，改所有 HudBro 界面的主色（边框、标题、滑块、勾选） |
| `UI Scale` | 0.5 ~ 2.0，缩放设置界面（用 `GuiGraphics.pose()` 缩放绘制，鼠标坐标反向换算） |
| `Opacity` | 20 ~ 255，面板背景不透明度 |
| `Blur` | 0 ~ 10，界面背景模糊（0 = 关闭） |
| `HUD Editor` / `Done` | 打开编辑器 / 关闭 |

### 新增文件
- `com/ciallo/config/GlobalConfig.java`：存 `config/hudbro-global.json`
  （`themeColor` / `uiScale` / `uiOpacity` / `uiBlur`）。
- `com/ciallo/gui/UiTheme.java`：各界面共用的主题、缩放、透明度、模糊与坐标换算。
- `com/ciallo/gui/HudMainScreen.java`：总界面。
- `com/ciallo/mixin/MixinOptions.java`：覆写 `Options#getMenuBackgroundBlurriness()`
  （→ intermediary `method_57703`），**只在 HudBro 界面打开时**返回我们自己的模糊值，
  这样不用改原版的无障碍选项就能单独控制本 mod 界面的模糊度。
- `assets/hudbro/lang/{en_us,zh_cn}.json` 增加 `key.hudbro.open_menu`。

### 其它调整
- `ColorPickerScreen` 增加 `parent` + `onApply` 回调，ESC / ENTER 返回上一级；
  从总界面改主题色不再直接关掉界面。
- `HudSettingsScreen` 的面板宽度/高度/滚动、`HudEditorScreen` 的面板都改用 `UiTheme`
  的透明度和主色；`renderBackground` 保持透明，模糊单独调用 `renderBlurredBackground`。

### 编译期踩到的两个点
- `GuiGraphics.pose()` 在 1.21.11 返回的是 `org.joml.Matrix3x2fStack`（不是 `PoseStack`），
  用 `pushMatrix()` / `scale()` / `popMatrix()`。
- `Screen#renderBlurredBackground` 是 `protected`，只能在 Screen 子类里 `this.` 调用，
  不能由工具类代调，所以 `UiTheme` 只提供 `blurEnabled()` 判断。

## 9. 第六轮：模糊闪退 + 编辑器操作

### 1. 改模糊度直接闪退
崩溃报告：

```
java.lang.IllegalStateException: Can only blur once per frame
  at GuiRenderState.blurBeforeThisStratum
  at Screen.renderBlurredBackground
  at com.ciallo.gui.HudMainScreen.render
```

原因：`Screen` 默认的 `renderBackground` 已经调用过一次 `renderBlurredBackground()`
（里面就是 `blurBeforeThisStratum()`），而为了加模糊我又在 `render()` 里调了一次
→ 一帧模糊两次，直接抛异常。`HudEditorScreen` / `HudSettingsScreen` 有同样的问题。

修复：模糊只在 `renderBackground(GuiGraphics, int, int, float)` 重写里调用**一次**，
`render()` 里只画半透明压暗，不再碰模糊。

### 2. 编辑器「点不动」
用日志探针实测（`[EDITOR-DBG]`）后结论是：**编辑器的鼠标事件、行命中、拖动其实都是好的**——
日志里能看到成功的拖动（`drag ... module=TntHud`），以及点击落在列表标题栏时 `row=-1`。
那几次点击的 y 坐标是 13.8 / 15.8 / 5.0，而列表第一行从 y=21 开始，也就是点到了标题栏。

针对「不好点 / 不好用」做的改进：

- 列表行高 12 → **14**，面板宽 132 → **150**，勾选框 8 → **9**
- **面板可以按住标题栏拖动**（和设置界面一致）
- 选中行高亮，并支持**键盘操作**（鼠标不好使时也能用）：
  `↑/↓` 选择、`Enter/Space` 开关、`S` 打开设置、`Shift+方向键` 微调 1 像素
- 拖动时把元素**限制在屏幕内**，避免拖出屏幕找不回来
- 单个 HUD 在编辑器里渲染抛异常时不再拖垮整个编辑器（try/catch + debug 日志）

## 10. 第七轮：退出设置界面 + 编辑器默认隐藏未开启 HUD

### 1. 设置界面退不出来
原因：`keyPressed` 里 ESC 的处理分成两段——**正在输入文本时第一次 ESC 只是取消输入**
（`typingSettingIdx` 复位），要按第二次才退出。实际用起来就像"卡住了"。

修复：
- ESC 改成在 `keyPressed` 最前面、**无条件退出**（同时清掉输入状态），
  和原版一样用 `event.isEscape()` 判断（不再手写 `key == 256`）
- 页脚右侧新增可点的 **`Done` / `Back` 按钮**，鼠标点击也能退出（作为 ESC 之外的保险）
- `HudMainScreen`、`ColorPickerScreen` 的 ESC / 回车也统一改用 `isEscape()` / `isConfirmation()`

### 2. 编辑器默认不显示未开启的 HUD
面板底部新增一个按钮：**`Show disabled: OFF / ON`**（默认 OFF）。

- **OFF**：编辑器和游戏里看到的一致——没开启的 HUD 完全不画
- **ON**：把没开启的 HUD 以半透明 + 红字名字画出来，方便摆位后再开启

该开关存在 `hudbro-global.json` 的 `editorShowDisabled` 里，重启后保持。

## 11. 第八轮：不可选中的隐藏 HUD + 指令补全

### 1. 编辑器里没显示的 HUD 不能再被鼠标选中
`hudAt()` 以前会把所有模块都拿去命中测试（包括关掉的），于是画面上看不见的地方也
能抓到隐藏的 HUD。现在：

- 画布命中测试 `hudAt()`：`Show disabled` 为 OFF 时**跳过所有未开启的 HUD**
- 列表行拖动：未开启且未显示时只选中、不拖动
- 拖动预览框：隐藏的 HUD 不画
- `Shift+方向键` 微调：隐藏的 HUD 也不动

要摆未开启的 HUD，先点面板底部的 `Show disabled` 打开显示再拖。

### 2. 指令自动补全
- `/hudbro <模块>` 的参数类型从 `StringArgumentType.string()` 改成 **`StringArgumentType.word()`**：
  原来 `string()` 会当成带引号的字符串处理，TAB 补全会变成 `"CoordsHud"` 这种带引号的形式；
  改成 `word()` 后可以正常补全成 `CoordsHud`，也不需要引号
- 补全内容改成按已输入前缀过滤（`startsWith` / `contains`），`/hudbro co` → `CoordsHud` / `ComboHud` / `CpsHud`
- 根节点已有 `editor` / `settings` 字面量，Brigadier 会自动在 `/hudbro ` 时提示它们








