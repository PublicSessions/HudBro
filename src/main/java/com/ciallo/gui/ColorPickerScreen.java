package com.ciallo.gui;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;


import net.minecraft.network.chat.Component;
import com.ciallo.setting.ColorSetting;
import com.ciallo.config.HudConfig;
import com.ciallo.util.MC;

public class ColorPickerScreen extends Screen {
    private final ColorSetting setting;
    private int currentColor;
    private boolean draggingHue = false;
    private boolean draggingSv = false;
    private boolean draggingAlpha = false;

    private static final int WIDTH = 220;
    private static final int HEIGHT = 180;
    private static final int PADDING = 10;

    public ColorPickerScreen(ColorSetting setting) {
        super(Component.literal("Pick Color"));
        this.setting = setting;
        this.currentColor = setting.getColor();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int x = (screenWidth - WIDTH) / 2;
        int y = (screenHeight - HEIGHT) / 2;
        if (y < 10) y = 10;
        if (y + HEIGHT > screenHeight - 10) y = screenHeight - HEIGHT - 10;

        int a = (currentColor >> 24) & 0xFF;
        int r = (currentColor >> 16) & 0xFF;
        int g = (currentColor >> 8) & 0xFF;
        int b = currentColor & 0xFF;

        context.fill(0, 0, screenWidth, screenHeight, 0xAA000000);
        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xDD1A1A1A);
        context.fill(x, y, x + WIDTH, y + 1, 0xFF444444);
        context.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFF333333);
        context.fill(x, y, x + 1, y + HEIGHT, 0xFF444444);
        context.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFF333333);

        int svWidth = WIDTH - PADDING * 2;
        int svHeight = 80;
        int svX = x + PADDING;
        int svY = y + 25;

        for (int py = 0; py < svHeight; py += 2) {
            for (int px = 0; px < svWidth; px += 2) {
                float s = px / (float) svWidth;
                float v = 1.0f - py / (float) svHeight;
                int rgb = hsvToRgb(getHue(currentColor), s, v);
                context.fill(svX + px, svY + py, svX + px + 2, svY + py + 2, (a << 24) | rgb);
            }
        }

        float hue = getHue(currentColor);
        float s = getSaturation(currentColor);
        float v = getValue(currentColor);
        int cursorX = svX + (int) (s * svWidth);
        int cursorY = svY + (int) ((1.0f - v) * svHeight);
        context.fill(cursorX - 2, cursorY - 2, cursorX + 3, cursorY + 3, 0xFFFFFFFF);
        context.fill(cursorX - 1, cursorY - 1, cursorX + 2, cursorY + 2, 0xFF000000);

        int hueY = y + 115;
        int hueHeight = 14;
        int hueWidth = WIDTH - PADDING * 2;
        int hueX = x + PADDING;
        for (int i = 0; i < hueWidth; i += 2) {
            float h = i / (float) hueWidth;
            int rgb = hsvToRgb(h * 360.0f, 1.0f, 1.0f);
            context.fill(hueX + i, hueY, hueX + i + 2, hueY + hueHeight, (a << 24) | rgb);
        }
        int hueCursorX = hueX + (int) (hue / 360.0f * hueWidth);
        context.fill(hueCursorX - 2, hueY - 2, hueCursorX + 3, hueY + hueHeight + 3, 0xFFFFFFFF);
        context.fill(hueCursorX - 1, hueY - 1, hueCursorX + 2, hueY + hueHeight + 2, 0xFF000000);

        int alphaY = y + 140;
        int alphaHeight = 14;
        int alphaWidth = WIDTH - PADDING * 2;
        int alphaX = x + PADDING;
        for (int i = 0; i < alphaWidth; i += 2) {
            int alpha = (int) (i / (float) alphaWidth * 255);
            int argb = (alpha << 24) | (r << 16) | (g << 8) | b;
            context.fill(alphaX + i, alphaY, alphaX + i + 2, alphaY + alphaHeight, argb);
        }
        int alphaCursorX = alphaX + (int) (a / 255.0f * alphaWidth);
        context.fill(alphaCursorX - 2, alphaY - 2, alphaCursorX + 3, alphaY + alphaHeight + 3, 0xFFFFFFFF);
        context.fill(alphaCursorX - 1, alphaY - 1, alphaCursorX + 2, alphaY + alphaHeight + 2, 0xFF000000);

        context.fill(x + PADDING, y + 8, x + 35, y + 20, currentColor);
        context.fill(x + PADDING, y + 8, x + PADDING + 1, y + 20, 0xFFFFFFFF);
        context.fill(x + 34, y + 8, x + 35, y + 20, 0xFF333333);
        context.fill(x + PADDING, y + 8, x + 35, y + 9, 0xFFFFFFFF);
        context.fill(x + PADDING, y + 19, x + 35, y + 20, 0xFF333333);

        context.drawString(MC.getMc().font, Component.literal("R:" + r), x + 40, y + 8, 0xFFFF0000, true);
        context.drawString(MC.getMc().font, Component.literal("G:" + g), x + 80, y + 8, 0xFF00FF00, true);
        context.drawString(MC.getMc().font, Component.literal("B:" + b), x + 120, y + 8, 0xFF0000FF, true);
        context.drawString(MC.getMc().font, Component.literal("A:" + a), x + 160, y + 8, 0xFFFFFFFF, true);

        context.drawCenteredString(MC.getMc().font, Component.literal("ESC: cancel | ENTER: apply"), x + WIDTH / 2, y + HEIGHT - 12, 0xFF888888);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        if (button == 0) {
            int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
            int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
            int x = (screenWidth - WIDTH) / 2;
            int y = (screenHeight - HEIGHT) / 2;
            if (y < 10) y = 10;
            if (y + HEIGHT > screenHeight - 10) y = screenHeight - HEIGHT - 10;

            int svWidth = WIDTH - PADDING * 2;
            int svHeight = 80;
            int svX = x + PADDING;
            int svY = y + 25;
            if (mx >= svX && mx <= svX + svWidth && my >= svY && my <= svY + svHeight) {
                draggingSv = true;
                updateSv((int) mx, (int) my, svX, svY, svWidth, svHeight);
                return true;
            }

            int hueY = y + 115;
            int hueHeight = 14;
            int hueWidth = WIDTH - PADDING * 2;
            int hueX = x + PADDING;
            if (mx >= hueX && mx <= hueX + hueWidth && my >= hueY && my <= hueY + hueHeight) {
                draggingHue = true;
                updateHue((int) mx, hueX, hueWidth);
                return true;
            }

            int alphaY = y + 140;
            int alphaHeight = 14;
            int alphaWidth = WIDTH - PADDING * 2;
            int alphaX = x + PADDING;
            if (mx >= alphaX && mx <= alphaX + alphaWidth && my >= alphaY && my <= alphaY + alphaHeight) {
                draggingAlpha = true;
                updateAlpha((int) mx, alphaX, alphaWidth);
                return true;
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        double mx = event.x();
        double my = event.y();
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int x = (screenWidth - WIDTH) / 2;
        int y = (screenHeight - HEIGHT) / 2;
        if (y < 10) y = 10;
        if (y + HEIGHT > screenHeight - 10) y = screenHeight - HEIGHT - 10;

        if (draggingSv) {
            int svWidth = WIDTH - PADDING * 2;
            int svHeight = 80;
            int svX = x + PADDING;
            int svY = y + 25;
            updateSv((int) mx, (int) my, svX, svY, svWidth, svHeight);
            return true;
        }
        if (draggingHue) {
            int hueWidth = WIDTH - PADDING * 2;
            int hueX = x + PADDING;
            updateHue((int) mx, hueX, hueWidth);
            return true;
        }
        if (draggingAlpha) {
            int alphaWidth = WIDTH - PADDING * 2;
            int alphaX = x + PADDING;
            updateAlpha((int) mx, alphaX, alphaWidth);
            return true;
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingSv || draggingHue || draggingAlpha) {
            draggingSv = false;
            draggingHue = false;
            draggingAlpha = false;
            setting.setColor(currentColor);
            HudConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (event.key() == 257) {
            setting.setColor(currentColor);
            HudConfig.save();
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }

    private void updateSv(int mouseX, int mouseY, int svX, int svY, int svWidth, int svHeight) {
        float s = (mouseX - svX) / (float) svWidth;
        float v = 1.0f - ((mouseY - svY) / (float) svHeight);
        s = Math.max(0.0f, Math.min(1.0f, s));
        v = Math.max(0.0f, Math.min(1.0f, v));
        int a = (currentColor >> 24) & 0xFF;
        currentColor = (a << 24) | hsvToRgb(getHue(currentColor), s, v);
    }

    private void updateHue(int mouseX, int hueX, int hueWidth) {
        float hue = ((mouseX - hueX) / (float) hueWidth) * 360.0f;
        hue = Math.max(0.0f, Math.min(360.0f, hue));
        int a = (currentColor >> 24) & 0xFF;
        int rgb = hsvToRgb(hue, getSaturation(currentColor), getValue(currentColor));
        currentColor = (a << 24) | rgb;
    }

    private void updateAlpha(int mouseX, int alphaX, int alphaWidth) {
        int alpha = (int) (((mouseX - alphaX) / (float) alphaWidth) * 255);
        alpha = Math.max(0, Math.min(255, alpha));
        int rgb = currentColor & 0x00FFFFFF;
        currentColor = (alpha << 24) | rgb;
    }

    private float getHue(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        if (max == min) return 0.0f;
        float delta = max - min;
        float hue;
        if (max == rf) hue = ((gf - bf) / delta) % 6.0f;
        else if (max == gf) hue = (bf - rf) / delta + 2.0f;
        else hue = (rf - gf) / delta + 4.0f;
        hue *= 60.0f;
        if (hue < 0.0f) hue += 360.0f;
        return hue;
    }

    private float getSaturation(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        if (max == 0.0f) return 0.0f;
        return (max - min) / max;
    }

    private float getValue(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return Math.max(r / 255.0f, Math.max(g / 255.0f, b / 255.0f));
    }

    private int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1.0f - Math.abs((h / 60.0f) % 2.0f - 1.0f));
        float m = v - c;
        float r1, g1, b1;
        if (h < 60.0f) { r1 = c; g1 = x; b1 = 0.0f; }
        else if (h < 120.0f) { r1 = x; g1 = c; b1 = 0.0f; }
        else if (h < 180.0f) { r1 = 0.0f; g1 = c; b1 = x; }
        else if (h < 240.0f) { r1 = 0.0f; g1 = x; b1 = c; }
        else if (h < 300.0f) { r1 = x; g1 = 0.0f; b1 = c; }
        else { r1 = c; g1 = 0.0f; b1 = x; }
        int r = (int) ((r1 + m) * 255.0f);
        int g = (int) ((g1 + m) * 255.0f);
        int b = (int) ((b1 + m) * 255.0f);
        return (r << 16) | (g << 8) | b;
    }
}

