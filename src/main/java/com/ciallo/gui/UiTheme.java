package com.ciallo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.ciallo.config.GlobalConfig;

/**
 * Small helpers so every HudBro screen uses the same theme / scale / opacity / blur settings.
 */
public final class UiTheme {
    private UiTheme() {
    }

    /** Scale factor applied to HudBro screens. */
    public static float scale() {
        return GlobalConfig.scale();
    }

    /** Accent colour, optionally faded. */
    public static int accent(float fade) {
        return GlobalConfig.accent(fade);
    }

    public static int accent() {
        return GlobalConfig.accent();
    }

    /** Panel background colour with the configured opacity. */
    public static int panel(int baseAlpha, float fade) {
        return GlobalConfig.panel(baseAlpha, fade);
    }

    /** Whether the configured background blur should be applied (call renderBlurredBackground). */
    public static boolean blurEnabled() {
        return GlobalConfig.blur() > 0;
    }

    /** Whether HudBro should override the vanilla menu blur radius for this screen. */
    public static boolean isHudBroScreen(Screen screen) {
        return screen instanceof HudMainScreen
                || screen instanceof HudSettingsScreen
                || screen instanceof HudEditorScreen
                || screen instanceof ColorPickerScreen;
    }

    public static boolean isHudBroScreenOpen() {
        return isHudBroScreen(Minecraft.getInstance().screen);
    }

    /** Scales a mouse coordinate from screen space into the scaled UI space. */
    public static double toUi(double coordinate) {
        float scale = scale();
        return scale <= 0.0f ? coordinate : coordinate / scale;
    }
}
