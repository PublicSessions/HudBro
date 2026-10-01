package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;

/**
 * NoRender module, ported from the original mod's {@code NoRender}.
 * Switches off individual overlays, particles and entity renders.
 *
 * <p>Implemented in this port: weather, title packets, particles (guardian / explosion / campfire /
 * firework / potion effect), entity renders (potion / experience bottle / arrow / egg / item),
 * entity fire, and GUI toasts.</p>
 *
 * <p>Not ported (no equivalent in the rewritten 1.21.11 render pipeline, or depending on modules
 * that do not exist in HudBro): the light map and lighting-engine toggles, the 2D item renderer,
 * entity transparency ({@code PlayerCollision}), and everything that belonged to Xray / Ambience /
 * ShaderModule / Zoom / Fov / AspectRatio / Freecam / HighLight / SpearModels.</p>
 */
public class NoRender extends Module {
    public static NoRender INSTANCE;

    private final BooleanSetting weather = this.m28(new BooleanSetting("Weather", true));
    private final BooleanSetting antiTitle = this.m28(new BooleanSetting("Title", false));
    private final BooleanSetting fireEntity = this.m28(new BooleanSetting("EntityFire", true));
    private final BooleanSetting guiToast = this.m28(new BooleanSetting("GuiToast", false));
    private final BooleanSetting fireOverlay = this.m28(new BooleanSetting("FireOverlay", true));
    private final BooleanSetting waterOverlay = this.m28(new BooleanSetting("WaterOverlay", true));
    private final BooleanSetting hurtCam = this.m28(new BooleanSetting("HurtCam", true));
    private final BooleanSetting totem = this.m28(new BooleanSetting("Totem", true));
    private final BooleanSetting blockOverlay = this.m28(new BooleanSetting("BlockOverlay", true));
    private final BooleanSetting portal = this.m28(new BooleanSetting("Portal", true));
    private final BooleanSetting nausea = this.m28(new BooleanSetting("Nausea", true));
    private final BooleanSetting potionsIcon = this.m28(new BooleanSetting("PotionsIcon", false));
    private final BooleanSetting darkness = this.m28(new BooleanSetting("Darkness", true));
    private final BooleanSetting castShadow = this.m28(new BooleanSetting("CastShadow", true));

    private final BooleanSetting potions = this.m28(new BooleanSetting("Potions", true));
    private final BooleanSetting xp = this.m28(new BooleanSetting("XP", true));
    private final BooleanSetting arrows = this.m28(new BooleanSetting("Arrows", false));
    private final BooleanSetting eggs = this.m28(new BooleanSetting("Eggs", false));
    private final BooleanSetting items = this.m28(new BooleanSetting("Items", false));

    private final BooleanSetting elderGuardian = this.m28(new BooleanSetting("Guardian", false));
    private final BooleanSetting explosions = this.m28(new BooleanSetting("Explosions", true));
    private final BooleanSetting campFire = this.m28(new BooleanSetting("CampFire", false));
    private final BooleanSetting fireworks = this.m28(new BooleanSetting("Fireworks", false));
    private final BooleanSetting effect = this.m28(new BooleanSetting("Effect", true));

    public NoRender() {
        super("NoRender", "Disables overlays, particles and entity renders.", Category.RENDER);
        this.setChinese("禁用渲染");
        this.setChineseDescription("关闭天气、标题、粒子与部分实体/特效渲染");
        INSTANCE = this;
    }

    private boolean on(BooleanSetting setting) {
        return this.isEnabled() && setting.getValue();
    }

    public boolean isWeather() {
        return on(weather);
    }

    public boolean isAntiTitle() {
        return on(antiTitle);
    }

    public boolean isFireEntity() {
        return on(fireEntity);
    }

    public boolean isGuiToast() {
        return on(guiToast);
    }

    public boolean isFireOverlay() {
        return on(fireOverlay);
    }

    public boolean isWaterOverlay() {
        return on(waterOverlay);
    }

    public boolean isHurtCam() {
        return on(hurtCam);
    }

    public boolean isTotem() {
        return on(totem);
    }

    public boolean isBlockOverlay() {
        return on(blockOverlay);
    }

    public boolean isPortal() {
        return on(portal);
    }

    public boolean isNausea() {
        return on(nausea);
    }

    public boolean isPotionsIcon() {
        return on(potionsIcon);
    }

    public boolean isPotions() {
        return on(potions);
    }

    public boolean isXp() {
        return on(xp);
    }

    public boolean isArrows() {
        return on(arrows);
    }

    public boolean isEggs() {
        return on(eggs);
    }

    public boolean isItems() {
        return on(items);
    }

    public boolean isDarkness() {
        return on(darkness);
    }

    public boolean isCastShadow() {
        return on(castShadow);
    }

    public boolean isElderGuardian() {
        return on(elderGuardian);
    }

    public boolean isExplosions() {
        return on(explosions);
    }

    public boolean isCampFire() {
        return on(campFire);
    }

    public boolean isFireworks() {
        return on(fireworks);
    }

    public boolean isEffect() {
        return on(effect);
    }
}
