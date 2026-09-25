package com.ciallo.gui;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;


import net.minecraft.network.chat.Component;
import com.ciallo.module.Module;
import com.ciallo.setting.EnumSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.Setting;
import com.ciallo.config.HudConfig;
import com.ciallo.util.MC;

public class HudSettingsScreen extends Screen {
    private final Module module;
    private final Screen parent;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean draggingSlider = false;
    private int draggingSettingIdx = -1;
    private int openTicks = 0;

    private int panelOffsetX = 0;
    private int panelOffsetY = 0;
    private boolean draggingPanel = false;
    private int panelDragStartX = 0;
    private int panelDragStartY = 0;

    private int typingSettingIdx = -1;
    private String typingBuffer = "";
    private int cursorPosition = 0;
    private int cursorBlinkTimer = 0;

    private static final int PANEL_WIDTH = 220;
    private static final int SETTING_HEIGHT = 30;
    private static final int HEADER_HEIGHT = 28;
    private static final int PADDING = 10;
    private static final int FOOTER_HEIGHT = 20;

    public HudSettingsScreen(Module module) {
        this(module, null);
    }

    /** @param parent screen to return to on ESC, or {@code null} to just close */
    public HudSettingsScreen(Module module, Screen parent) {
        super(Component.literal("Settings: " + module.getName()));
        this.module = module;
        this.parent = parent;
    }

    /** Logical screen width after the configured UI scale. */
    private int uiWidth() {
        return Math.round(MC.getMc().getWindow().getGuiScaledWidth() / UiTheme.scale());
    }

    /** Logical screen height after the configured UI scale. */
    private int uiHeight() {
        return Math.round(MC.getMc().getWindow().getGuiScaledHeight() / UiTheme.scale());
    }

    /**
     * Keeps the panel inside the screen: it grows with the number of settings until it hits the
     * screen borders, and the setting list scrolls when there are more rows than fit.
     */
    private int clampPanelHeight(int contentHeight) {
        int screenHeight = uiHeight();
        int desired = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;
        int maxPanel = screenHeight - 20;
        int minPanel = HEADER_HEIGHT + FOOTER_HEIGHT + PADDING * 2 + SETTING_HEIGHT;
        return Math.max(minPanel, Math.min(desired, maxPanel));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        openTicks++;
        float uiScale = UiTheme.scale();

        // Blur + dim cover the whole (unscaled) screen.
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), 0x40101010);

        mouseX = (int) Math.round(UiTheme.toUi(mouseX));
        mouseY = (int) Math.round(UiTheme.toUi(mouseY));

        org.joml.Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();
        pose.scale(uiScale, uiScale);

        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int listHeight = panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PADDING;
        maxScroll = Math.max(0, contentHeight - listHeight);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        int basePanelX = (screenWidth - PANEL_WIDTH) / 2;
        int basePanelY = (screenHeight - panelHeight) / 2;
        int panelX = basePanelX + panelOffsetX;
        int panelY = basePanelY + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        float fadeIn = Math.min(1.0f, openTicks / 15.0f);
        int borderAlpha = (int) (160 * fadeIn);

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, UiTheme.panel(235, fadeIn));
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, (borderAlpha << 24) | (UiTheme.accent() & 0xFFFFFF));
        context.fill(panelX, panelY + panelHeight - 1, panelX + PANEL_WIDTH, panelY + panelHeight, (borderAlpha << 24) | (UiTheme.accent() & 0xFFFFFF));
        context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, (borderAlpha << 24) | (UiTheme.accent() & 0xFFFFFF));
        context.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, (borderAlpha << 24) | (UiTheme.accent() & 0xFFFFFF));

        context.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + HEADER_HEIGHT, UiTheme.panel(255, 0.75f * fadeIn));
        context.drawCenteredString(MC.getMc().font, Component.literal(module.getName() + " Settings"), panelX + PANEL_WIDTH / 2, panelY + 10, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));

        int settingsY = panelY + HEADER_HEIGHT;
        int visibleHeight = panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PADDING;
        int idx = 0;
        for (Setting setting : module.getSettings()) {
            int y = settingsY + idx * SETTING_HEIGHT - scrollOffset;
            if (y >= settingsY && y + SETTING_HEIGHT <= settingsY + visibleHeight) {
                if (setting instanceof NumberSetting num) {
                    renderNumberSetting(context, panelX, y, num, mouseX, mouseY, fadeIn, idx);
                } else if (setting instanceof ColorSetting color) {
                    renderColorSetting(context, panelX, y, color, mouseX, mouseY, fadeIn);
                } else if (setting instanceof TextSetting text) {
                    renderTextSetting(context, panelX, y, text, mouseX, mouseY, fadeIn, idx);
                } else if (setting instanceof BooleanSetting bool) {
                    renderBooleanSetting(context, panelX, y, bool, mouseX, mouseY, fadeIn, idx);
                } else if (setting instanceof EnumSetting<?> enumSetting) {
                    renderEnumSetting(context, panelX, y, enumSetting, fadeIn);
                }
            }
            idx++;
        }

        if (maxScroll > 0) {
            int trackX = panelX + PANEL_WIDTH - 5;
            int trackTop = settingsY;
            int trackBottom = settingsY + visibleHeight;
            context.fill(trackX, trackTop, trackX + 3, trackBottom, (int)(90 * fadeIn) << 24 | 0x888888);
            int barHeight = Math.max(12, (int)((float) visibleHeight * visibleHeight / (visibleHeight + maxScroll)));
            int barY = trackTop + (int)((float)(trackBottom - trackTop - barHeight) * scrollOffset / maxScroll);
            context.fill(trackX, barY, trackX + 3, barY + barHeight, (int)(220 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        }

        context.fill(panelX + 1, panelY + panelHeight - FOOTER_HEIGHT - 1, panelX + PANEL_WIDTH - 1, panelY + panelHeight - 1, UiTheme.panel(255, 0.75f * fadeIn));
        context.drawString(MC.getMc().font, parent != null ? "ESC = back" : "ESC = close", panelX + PADDING, panelY + panelHeight - FOOTER_HEIGHT / 2 - 2, (int)(200 * fadeIn) << 24 | 0xFFAAAAAA, true);

        // Clickable exit, so the screen can always be left even if a key is being swallowed.
        int buttonWidth = 44;
        int buttonX = panelX + PANEL_WIDTH - PADDING - buttonWidth;
        int buttonY = panelY + panelHeight - FOOTER_HEIGHT / 2 - 8;
        boolean buttonHovered = mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 14;
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 14, UiTheme.panel(buttonHovered ? 255 : 200, fadeIn));
        int buttonBorder = buttonHovered ? UiTheme.accent(fadeIn) : UiTheme.accent(0.6f * fadeIn);
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 1, buttonBorder);
        context.fill(buttonX, buttonY + 13, buttonX + buttonWidth, buttonY + 14, buttonBorder);
        context.drawCenteredString(MC.getMc().font, Component.literal(parent != null ? "Back" : "Done"), buttonX + buttonWidth / 2, buttonY + 3, 0xFFFFFFFF);

        pose.popMatrix();
    }

    /** Hit test for the exit button in the footer. */
    private boolean isMouseOverExitButton(double mouseX, double mouseY) {
        int contentHeight = module.getSettings().size() * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        int buttonWidth = 44;
        int buttonX = panelX + PANEL_WIDTH - PADDING - buttonWidth;
        int buttonY = panelY + panelHeight - FOOTER_HEIGHT / 2 - 8;
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + 14;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Blur may only be requested once per frame, so it lives here and not in render().
        if (UiTheme.blurEnabled()) {
            this.renderBlurredBackground(context);
        }
        context.fill(0, 0, context.guiWidth(), context.guiHeight(), 0x20101010);
    }

    private void renderNumberSetting(GuiGraphics context, int panelX, int y, NumberSetting setting, int mouseX, int mouseY, float fadeIn, int idx) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = 45;
        int labelWidth = MC.getMc().font.width(setting.getName()) + 8;
        int barWidth = width - labelWidth - valueWidth - 5;

        String displayValue = (typingSettingIdx == idx) ? typingBuffer : String.format("%.1f", setting.getValue());
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);
        context.drawString(MC.getMc().font, Component.literal(displayValue), x + width - valueWidth, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);

        if (typingSettingIdx == idx) {
            cursorBlinkTimer++;
            if ((cursorBlinkTimer / 12) % 2 == 0) {
                String before = typingBuffer.substring(0, Math.min(cursorPosition, typingBuffer.length()));
                int cx = x + width - valueWidth + MC.getMc().font.width(before);
                context.drawString(MC.getMc().font, "|", cx, y + 8, (int)(255 * fadeIn) << 24 | 0xFFFFFFFF, true);
            }
        }

        int barX = x + labelWidth + 5;
        int barY = y + 10;
        context.fill(barX, barY, barX + barWidth, barY + 4, (int)(100 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));

        double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;
        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            context.fill(barX, barY, barX + fillWidth, barY + 4, (int)(220 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        }

        int handleX = barX + fillWidth - 1;
        int handlePulse = draggingSlider && draggingSettingIdx == idx ? (int)(30 * Math.abs(Math.sin(openTicks * 0.3f))) : 0;
        int handleColor = ((int)(255 * fadeIn) << 24) | ((UiTheme.accent() & 0xFFFFFF) + handlePulse);
        context.fill(handleX, barY - 3, handleX + 3, barY + 7, handleColor);
    }

    private void renderColorSetting(GuiGraphics context, int panelX, int y, ColorSetting setting, int mouseX, int mouseY, float fadeIn) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);

        int colorBoxX = x + width - 30;
        int colorBoxY = y + 4;
        context.fill(colorBoxX, colorBoxY, colorBoxX + 25, colorBoxY + 12, setting.getColor());
        context.fill(colorBoxX, colorBoxY, colorBoxX + 1, colorBoxY + 12, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.fill(colorBoxX + 24, colorBoxY, colorBoxX + 25, colorBoxY + 12, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.fill(colorBoxX, colorBoxY, colorBoxX + 25, colorBoxY + 1, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.fill(colorBoxX, colorBoxY + 11, colorBoxX + 25, colorBoxY + 12, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
    }

    private void renderTextSetting(GuiGraphics context, int panelX, int y, TextSetting setting, int mouseX, int mouseY, float fadeIn, int idx) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = width - 60;

        String displayValue = (typingSettingIdx == idx) ? typingBuffer : setting.getValue();
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);
        context.drawString(MC.getMc().font, Component.literal(displayValue), x + 55, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);

        if (typingSettingIdx == idx) {
            cursorBlinkTimer++;
            if ((cursorBlinkTimer / 12) % 2 == 0) {
                String before = typingBuffer.substring(0, Math.min(cursorPosition, typingBuffer.length()));
                int cx = x + 55 + MC.getMc().font.width(before);
                context.drawString(MC.getMc().font, "|", cx, y + 8, (int)(255 * fadeIn) << 24 | 0xFFFFFFFF, true);
            }
        }
    }

    private void renderBooleanSetting(GuiGraphics context, int panelX, int y, BooleanSetting setting, int mouseX, int mouseY, float fadeIn, int idx) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int boxSize = 10;

        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);

        int boxX = x + width - boxSize - 30;
        int boxY = y + 8;
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, setting.getValue() ? (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF) : (int)(100 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.fill(boxX, boxY, boxX + 1, boxY + boxSize, 0xFFFFFFFF);
        context.fill(boxX + boxSize - 1, boxY, boxX + boxSize, boxY + boxSize, 0xFF333333);
        context.fill(boxX, boxY, boxX + boxSize, boxY + 1, 0xFFFFFFFF);
        context.fill(boxX, boxY + boxSize - 1, boxX + boxSize, boxY + boxSize, 0xFF333333);

        String text = setting.getValue() ? "ON" : "OFF";
        context.drawString(MC.getMc().font, Component.literal(text), boxX + boxSize + 5, y + 8, setting.getValue() ? (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF) : (int)(200 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);
    }

    private void renderEnumSetting(GuiGraphics context, int panelX, int y, EnumSetting<?> setting, float fadeIn) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);

        String value = setting.getNameValue();
        int boxWidth = 90;
        int boxX = x + width - boxWidth;
        int boxY = y + 4;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 14, (int)(120 * fadeIn) << 24 | 0xDDDDDD);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, (int)(200 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.fill(boxX, boxY + 13, boxX + boxWidth, boxY + 14, (int)(200 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF));
        context.drawString(MC.getMc().font, Component.literal(value), boxX + boxWidth - MC.getMc().font.width(value) - 4, y + 7, (int)(255 * fadeIn) << 24 | (UiTheme.accent() & 0xFFFFFF), true);
    }

    private boolean isMouseOverEnumSetting(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        int settingsY = panelY + HEADER_HEIGHT;
        int y = settingsY + settingIdx * SETTING_HEIGHT - scrollOffset;
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int boxWidth = 90;
        int boxX = x + width - boxWidth;
        int boxY = y + 4;

        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + 14;
    }

    private boolean isMouseOverSlider(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        int settingsY = panelY + HEADER_HEIGHT;
        int y = settingsY + settingIdx * SETTING_HEIGHT - scrollOffset;
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = 45;
        int labelWidth = MC.getMc().font.width(module.getSettings().get(settingIdx).getName()) + 8;
        int barWidth = width - labelWidth - valueWidth - 5;
        int barX = x + labelWidth + 5;
        int barY = y + 10;

        return mouseX >= barX - 4 && mouseX <= barX + barWidth + 4 && mouseY >= barY - 6 && mouseY <= barY + 10;
    }

    private boolean isMouseOverHeader(int mouseX, int mouseY) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + HEADER_HEIGHT;
    }

    private boolean isMouseOverTextSetting(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        int settingsY = panelY + HEADER_HEIGHT;
        int y = settingsY + settingIdx * SETTING_HEIGHT - scrollOffset;
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = width - 60;

        return mouseX >= x + 55 && mouseX <= x + 55 + valueWidth && mouseY >= y && mouseY <= y + SETTING_HEIGHT;
    }

    private boolean isMouseOverBooleanSetting(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        int settingsY = panelY + HEADER_HEIGHT;
        int y = settingsY + settingIdx * SETTING_HEIGHT - scrollOffset;
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int boxSize = 10;
        int boxX = x + width - boxSize - 30;

        return mouseX >= boxX && mouseX <= boxX + boxSize && mouseY >= y + 8 && mouseY <= y + 8 + boxSize;
    }

    private boolean isMouseOverColorBox(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = uiWidth();
        int screenHeight = uiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);

        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (screenHeight - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelY + panelHeight > screenHeight - 10) {
            panelY = screenHeight - panelHeight - 10;
        }
        if (panelX < 10) panelX = 10;
        if (panelX + PANEL_WIDTH > screenWidth - 10) {
            panelX = screenWidth - PANEL_WIDTH - 10;
        }

        int settingsY = panelY + HEADER_HEIGHT;
        int y = settingsY + settingIdx * SETTING_HEIGHT - scrollOffset;
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;

        int colorBoxX = x + width - 30;
        int colorBoxY = y + 4;

        return mouseX >= colorBoxX && mouseX <= colorBoxX + 25 && mouseY >= colorBoxY && mouseY <= colorBoxY + 12;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mx = UiTheme.toUi(event.x());
        double my = UiTheme.toUi(event.y());
        int button = event.button();

        if (button == 0) {
            if (isMouseOverExitButton(mx, my)) {
                Minecraft.getInstance().setScreen(parent);
                return true;
            }
            if (isMouseOverHeader((int) mx, (int) my)) {
                draggingPanel = true;
                panelDragStartX = (int) mx;
                panelDragStartY = (int) my;
                return true;
            }

            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (setting instanceof NumberSetting num) {
                    if (isMouseOverSlider((int) mx, (int) my, idx)) {
                        draggingSlider = true;
                        draggingSettingIdx = idx;
                        applySlider((int) mx, num);
                        return true;
                    }
                } else if (setting instanceof ColorSetting color) {
                    if (isMouseOverColorBox((int) mx, (int) my, idx)) {
                        Minecraft.getInstance().setScreen(new ColorPickerScreen(color));
                        return true;
                    }
                } else if (setting instanceof BooleanSetting bool) {
                    if (isMouseOverBooleanSetting((int) mx, (int) my, idx)) {
                        bool.setValue(!bool.getValue());
                        HudConfig.save();
                        return true;
                    }
                } else if (setting instanceof EnumSetting<?> enumSetting) {
                    if (isMouseOverEnumSetting((int) mx, (int) my, idx)) {
                        enumSetting.cycle();
                        HudConfig.save();
                        return true;
                    }
                }
                idx++;
            }
        } else if (button == 1) {
            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (setting instanceof NumberSetting num) {
                    if (isMouseOverSlider((int) mx, (int) my, idx)) {
                        typingSettingIdx = idx;
                        typingBuffer = String.format("%.1f", num.getValue());
                        cursorPosition = typingBuffer.length();
                        return true;
                    }
                } else if (setting instanceof TextSetting text) {
                    if (isMouseOverTextSetting((int) mx, (int) my, idx)) {
                        typingSettingIdx = idx;
                        typingBuffer = text.getValue();
                        cursorPosition = typingBuffer.length();
                        return true;
                    }
                } else if (setting instanceof EnumSetting<?> enumSetting) {
                    if (isMouseOverEnumSetting((int) mx, (int) my, idx)) {
                        enumSetting.cycleBack();
                        HudConfig.save();
                        return true;
                    }
                }
                idx++;
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        double mx = UiTheme.toUi(event.x());
        double my = UiTheme.toUi(event.y());

        if (draggingPanel) {
            panelOffsetX += (int) (mx - panelDragStartX);
            panelOffsetY += (int) (my - panelDragStartY);
            panelDragStartX = (int) mx;
            panelDragStartY = (int) my;
            return true;
        }

        if (draggingSlider && draggingSettingIdx >= 0) {
            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (idx == draggingSettingIdx && setting instanceof NumberSetting num) {
                    applySlider((int) mx, num);
                    return true;
                }
                idx++;
            }
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingPanel) {
            draggingPanel = false;
            return true;
        }
        if (draggingSlider) {
            draggingSlider = false;
            draggingSettingIdx = -1;
            HudConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // ESC always leaves the screen, even while a value is being typed.
        if (event.isEscape()) {
            typingSettingIdx = -1;
            typingBuffer = "";
            cursorPosition = 0;
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        if (typingSettingIdx >= 0) {

            if (event.key() == 259 && cursorPosition > 0) {
                typingBuffer = typingBuffer.substring(0, cursorPosition - 1) + typingBuffer.substring(cursorPosition);
                cursorPosition--;
                return true;
            }

            if (event.key() == 261 && cursorPosition < typingBuffer.length()) {
                typingBuffer = typingBuffer.substring(0, cursorPosition) + typingBuffer.substring(cursorPosition + 1);
                return true;
            }

            if (event.key() == 257) {
                int idx = 0;
                for (Setting setting : module.getSettings()) {
                    if (idx == typingSettingIdx) {
                        if (setting instanceof NumberSetting num) {
                            try {
                                double value = Double.parseDouble(typingBuffer);
                                num.setValueWithoutClamp(value);
                                HudConfig.save();
                            } catch (NumberFormatException e) {
                            }
                        } else if (setting instanceof TextSetting text) {
                            text.setValue(typingBuffer);
                            HudConfig.save();
                        }
                        break;
                    }
                    idx++;
                }
                typingSettingIdx = -1;
                typingBuffer = "";
                cursorPosition = 0;
                return true;
            }

            if (event.key() == 262) {
                if (cursorPosition < typingBuffer.length()) cursorPosition++;
                return true;
            }
            if (event.key() == 263) {
                if (cursorPosition > 0) cursorPosition--;
                return true;
            }
            if (event.key() == 268) {
                cursorPosition = 0;
                return true;
            }
            if (event.key() == 269) {
                cursorPosition = typingBuffer.length();
                return true;
            }

            if (isCtrlDown()) {
                if (event.key() == 65) {
                    cursorPosition = typingBuffer.length();
                    return true;
                }
                if (event.key() == 67) {
                    if (cursorPosition > 0) {
                        MC.getMc().keyboardHandler.setClipboard(typingBuffer);
                    }
                    return true;
                }
                if (event.key() == 86) {
                    String clip = MC.getMc().keyboardHandler.getClipboard();
                    if (clip != null) {
                        for (int i = 0; i < clip.length(); i++) {
                            char c = clip.charAt(i);
                            if (c >= 32 && c != 127) {
                                typingBuffer = typingBuffer.substring(0, cursorPosition) + c + typingBuffer.substring(cursorPosition);
                                cursorPosition++;
                            }
                        }
                    }
                    return true;
                }
            }

            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (idx == typingSettingIdx && setting instanceof TextSetting) {
                    return false;
                }
                idx++;
            }
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (typingSettingIdx >= 0) {
            int codepoint = event.codepoint();
            if (codepoint < 32 || codepoint == 127) {
                return true;
            }
            String typed = new String(Character.toChars(codepoint));
            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (idx == typingSettingIdx) {
                    if (setting instanceof TextSetting) {
                        typingBuffer = typingBuffer.substring(0, cursorPosition) + typed + typingBuffer.substring(cursorPosition);
                        cursorPosition += typed.length();
                        return true;
                    }
                    break;
                }
                idx++;
            }
            char c = typed.charAt(0);
            if (typed.length() == 1) {
                if (c >= '0' && c <= '9') {
                    typingBuffer = typingBuffer.substring(0, cursorPosition) + c + typingBuffer.substring(cursorPosition);
                    cursorPosition++;
                    return true;
                }
                if (c == '.') {
                    if (!typingBuffer.contains(".") || cursorPosition == typingBuffer.length()) {
                        typingBuffer = typingBuffer.substring(0, cursorPosition) + "." + typingBuffer.substring(cursorPosition);
                        cursorPosition++;
                    }
                    return true;
                }
                if (c == '-') {
                    if (cursorPosition == 0 && !typingBuffer.startsWith("-")) {
                        typingBuffer = "-" + typingBuffer;
                        cursorPosition++;
                    }
                    return true;
                }
            }
            return true;
        }
        return super.charTyped(event);
    }

    private long getWindowHandle() {
        try {
            Object result = MC.getMc().getWindow().getClass().getMethod("handle").invoke(MC.getMc().getWindow());
            return (Long) result;
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isCtrlDown() {
        long window = getWindowHandle();
        if (window == 0) return false;
        return org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS
            || org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private void applySlider(int mouseX, NumberSetting setting) {
        int screenWidth = uiWidth();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = clampPanelHeight(contentHeight);
        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (uiHeight() - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelX < 10) panelX = 10;

        int settingsY = panelY + HEADER_HEIGHT;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = 45;
        int labelWidth = MC.getMc().font.width(setting.getName()) + 8;
        int barWidth = width - labelWidth - valueWidth - 5;
        int barX = panelX + PADDING + labelWidth + 5;

        double progress = (mouseX - barX) / (double) barWidth;
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;

        double value = setting.getMin() + progress * (setting.getMax() - setting.getMin());
        double step = setting.getStep();
        value = Math.round(value / step) * step;
        setting.setValue(value);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int) (scrollY * SETTING_HEIGHT);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        return true;
    }
}

