package com.ciallo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;
import com.ciallo.config.GlobalConfig;
import com.ciallo.setting.NumberSetting;
import com.ciallo.util.KeyBinds;
import com.ciallo.util.MC;

/**
 * The main HudBro screen: theme colour, UI scale, panel opacity and background blur, plus entry
 * points to the HUD editor and the per module settings.
 *
 * <p>Opened with {@code /hudbro} or the configured key bind (default H).</p>
 */
public class HudMainScreen extends Screen {
    private static final int PANEL_WIDTH = 230;
    private static final int HEADER_HEIGHT = 26;
    private static final int ROW_HEIGHT = 22;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PADDING = 10;
    private static final int PANEL_HEIGHT = HEADER_HEIGHT + ROW_HEIGHT * 4 + BUTTON_HEIGHT + PADDING * 2;

    private int draggingSlider = -1;
    private int openTicks = 0;

    public HudMainScreen() {
        super(Component.literal("HudBro"));
    }

    private int panelX(float scale, int guiWidth) {
        return Math.round((guiWidth / scale - PANEL_WIDTH) / 2.0f);
    }

    private int panelY(float scale, int guiHeight) {
        return Math.round((guiHeight / scale - PANEL_HEIGHT) / 2.0f);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        openTicks++;
        float scale = UiTheme.scale();
        float fadeIn = Math.min(1.0f, openTicks / 12.0f);

        int guiWidth = context.guiWidth();
        int guiHeight = context.guiHeight();

        // Background blur + translucent dim, drawn unscaled so it covers the whole screen.
        context.fill(0, 0, guiWidth, guiHeight, 0x40101010);

        int logicalMouseX = Math.round((float) UiTheme.toUi(mouseX));
        int logicalMouseY = Math.round((float) UiTheme.toUi(mouseY));

        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();
        pose.scale(scale, scale);

        int x = panelX(scale, guiWidth);
        int y = panelY(scale, guiHeight);

        // Panel
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, UiTheme.panel(225, fadeIn));
        drawBorder(context, x, y, PANEL_WIDTH, PANEL_HEIGHT, UiTheme.accent(0.85f * fadeIn));
        context.fill(x + 1, y + 1, x + PANEL_WIDTH - 1, y + HEADER_HEIGHT, UiTheme.panel(255, 0.55f * fadeIn));
        context.drawCenteredString(MC.getMc().font, Component.literal("HudBro"), x + PANEL_WIDTH / 2, y + 9, UiTheme.accent(fadeIn));

        int rowY = y + HEADER_HEIGHT;
        renderColorRow(context, x, rowY, logicalMouseX, logicalMouseY, fadeIn);
        renderSliderRow(context, x, rowY + ROW_HEIGHT, "UI Scale", GlobalConfig.uiScale, 0, logicalMouseX, logicalMouseY, fadeIn);
        renderSliderRow(context, x, rowY + ROW_HEIGHT * 2, "Opacity", GlobalConfig.uiOpacity, 1, logicalMouseX, logicalMouseY, fadeIn);
        renderSliderRow(context, x, rowY + ROW_HEIGHT * 3, "Blur", GlobalConfig.uiBlur, 2, logicalMouseX, logicalMouseY, fadeIn);

        int buttonY = y + HEADER_HEIGHT + ROW_HEIGHT * 4 + 6;
        int half = (PANEL_WIDTH - PADDING * 2 - 6) / 2;
        renderButton(context, x + PADDING, buttonY, half, "HUD Editor", isHovered(logicalMouseX, logicalMouseY, x + PADDING, buttonY, half, BUTTON_HEIGHT), fadeIn);
        renderButton(context, x + PADDING + half + 6, buttonY, half, "Done", isHovered(logicalMouseX, logicalMouseY, x + PADDING + half + 6, buttonY, half, BUTTON_HEIGHT), fadeIn);

        pose.popMatrix();

        // Footer hints are drawn unscaled so they stay readable at any UI scale.
        String hint = "Open: " + KeyBinds.OPEN_MENU.getTranslatedKeyMessage().getString()
                + " / " + KeyBinds.OPEN_EDITOR.getTranslatedKeyMessage().getString()
                + "   |   /hudbro  /hudbro editor";
        context.drawCenteredString(MC.getMc().font, Component.literal(hint), guiWidth / 2, guiHeight - 14, 0xFFBBBBBB);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Blur may only be requested once per frame, so it lives here and not in render().
        if (UiTheme.blurEnabled()) {
            this.renderBlurredBackground(context);
        }
    }

    private void renderColorRow(GuiGraphics context, int x, int y, int mouseX, int mouseY, float fadeIn) {
        context.drawString(MC.getMc().font, Component.literal("Theme Color"), x + PADDING, y + 7, 0xFFEEEEEE, true);

        int boxX = x + PANEL_WIDTH - PADDING - 46;
        int boxY = y + 4;
        boolean hovered = isHovered(mouseX, mouseY, boxX, boxY, 46, 14);
        context.fill(boxX, boxY, boxX + 46, boxY + 14, GlobalConfig.themeColor.getColor());
        drawBorder(context, boxX, boxY, 46, 14, hovered ? 0xFFFFFFFF : UiTheme.accent(0.8f * fadeIn));
    }

    private void renderSliderRow(GuiGraphics context, int x, int y, String label, NumberSetting setting, int index, int mouseX, int mouseY, float fadeIn) {
        context.drawString(MC.getMc().font, Component.literal(label), x + PADDING, y + 7, 0xFFEEEEEE, true);

        String value = String.format("%.2f", setting.getValue());
        context.drawString(MC.getMc().font, Component.literal(value), x + PANEL_WIDTH - PADDING - MC.getMc().font.width(value), y + 7, 0xFFBDBDBD, true);

        int barX = x + PADDING;
        int barWidth = PANEL_WIDTH - PADDING * 2;
        int barY = y + 17;
        context.fill(barX, barY, barX + barWidth, barY + 3, 0x60FFFFFF);

        double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        int fill = (int) Math.round(barWidth * Math.max(0.0, Math.min(1.0, progress)));
        context.fill(barX, barY, barX + fill, barY + 3, UiTheme.accent(fadeIn));
        context.fill(barX + fill - 1, barY - 2, barX + fill + 2, barY + 5, 0xFFFFFFFF);
    }

    private void renderButton(GuiGraphics context, int x, int y, int width, String label, boolean hovered, float fadeIn) {
        context.fill(x, y, x + width, y + BUTTON_HEIGHT, UiTheme.panel(hovered ? 255 : 190, fadeIn));
        drawBorder(context, x, y, width, BUTTON_HEIGHT, hovered ? UiTheme.accent(fadeIn) : UiTheme.accent(0.6f * fadeIn));
        context.drawCenteredString(MC.getMc().font, Component.literal(label), x + width / 2, y + 7, hovered ? 0xFFFFFFFF : 0xFFDDDDDD);
    }

    private void drawBorder(GuiGraphics context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private int sliderIndexAt(int mouseX, int mouseY, int x, int y) {
        for (int i = 0; i < 3; i++) {
            int rowTop = y + HEADER_HEIGHT + ROW_HEIGHT * (i + 1);
            if (mouseY >= rowTop + 12 && mouseY <= rowTop + ROW_HEIGHT && mouseX >= x + PADDING && mouseX <= x + PANEL_WIDTH - PADDING) {
                return i;
            }
        }
        return -1;
    }

    private NumberSetting sliderAt(int index) {
        return switch (index) {
            case 0 -> GlobalConfig.uiScale;
            case 1 -> GlobalConfig.uiOpacity;
            default -> GlobalConfig.uiBlur;
        };
    }

    private void applySlider(int index, int mouseX, int x) {
        NumberSetting setting = sliderAt(index);
        int barX = x + PADDING;
        int barWidth = PANEL_WIDTH - PADDING * 2;
        double progress = (mouseX - barX) / (double) barWidth;
        progress = Math.max(0.0, Math.min(1.0, progress));
        double value = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        double step = setting.getStep();
        setting.setValue(Math.round(value / step) * step);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        float scale = UiTheme.scale();
        int mouseX = Math.round((float) UiTheme.toUi(event.x()));
        int mouseY = Math.round((float) UiTheme.toUi(event.y()));
        int guiWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int guiHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int x = panelX(scale, guiWidth);
        int y = panelY(scale, guiHeight);

        if (event.button() == 0) {
            // Theme colour swatch
            int boxX = x + PANEL_WIDTH - PADDING - 46;
            if (isHovered(mouseX, mouseY, boxX, y + HEADER_HEIGHT + 4, 46, 14)) {
                Minecraft.getInstance().setScreen(new ColorPickerScreen(GlobalConfig.themeColor, this));
                return true;
            }

            int slider = sliderIndexAt(mouseX, mouseY, x, y);
            if (slider >= 0) {
                draggingSlider = slider;
                applySlider(slider, mouseX, x);
                GlobalConfig.save();
                return true;
            }

            int buttonY = y + HEADER_HEIGHT + ROW_HEIGHT * 4 + 6;
            int half = (PANEL_WIDTH - PADDING * 2 - 6) / 2;
            if (isHovered(mouseX, mouseY, x + PADDING, buttonY, half, BUTTON_HEIGHT)) {
                Minecraft.getInstance().setScreen(new HudEditorScreen());
                return true;
            }
            if (isHovered(mouseX, mouseY, x + PADDING + half + 6, buttonY, half, BUTTON_HEIGHT)) {
                Minecraft.getInstance().setScreen(null);
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (draggingSlider >= 0) {
            float scale = UiTheme.scale();
            int logicalX = Math.round((float) UiTheme.toUi(event.x()));
            int x = panelX(scale, MC.getMc().getWindow().getGuiScaledWidth());
            applySlider(draggingSlider, logicalX, x);
            return true;
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingSlider >= 0) {
            draggingSlider = -1;
            GlobalConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        GlobalConfig.save();
        super.removed();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }
}
