package com.ciallo.gui;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;


import net.minecraft.network.chat.Component;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.Setting;
import com.ciallo.config.HudConfig;
import com.ciallo.util.MC;

public class HudSettingsScreen extends Screen {
    private final AbstractHudModule module;
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

    public HudSettingsScreen(AbstractHudModule module) {
        super(Component.literal("Settings: " + module.getName()));
        this.module = module;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        openTicks++;
        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

        maxScroll = Math.max(0, contentHeight + FOOTER_HEIGHT - (screenHeight - HEADER_HEIGHT - PADDING * 4));
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
        int panelAlpha = (int) (120 * fadeIn);
        int borderAlpha = (int) (160 * fadeIn);

        context.fill(0, 0, screenWidth, screenHeight, 0x11000000);

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, (panelAlpha << 24) | 0xEEEEEE);
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, (borderAlpha << 24) | 0xFF4444);
        context.fill(panelX, panelY + panelHeight - 1, panelX + PANEL_WIDTH, panelY + panelHeight, (borderAlpha << 24) | 0xFF4444);
        context.fill(panelX, panelY, panelX + 1, panelY + panelHeight, (borderAlpha << 24) | 0xFF4444);
        context.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, (borderAlpha << 24) | 0xFF4444);

        context.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + HEADER_HEIGHT, ((int)(panelAlpha * 0.9f) << 24) | 0xDDDDDD);
        context.drawCenteredString(MC.getMc().font, Component.literal(module.getName() + " Settings"), panelX + PANEL_WIDTH / 2, panelY + 10, (int)(255 * fadeIn) << 24 | 0xFF0000);

        int settingsY = panelY + HEADER_HEIGHT;
        int visibleHeight = panelHeight - HEADER_HEIGHT - FOOTER_HEIGHT - PADDING;
        int idx = 0;
        for (Setting setting : module.getSettings()) {
            int y = settingsY + idx * SETTING_HEIGHT - scrollOffset;
            if (y + SETTING_HEIGHT >= settingsY && y <= settingsY + visibleHeight) {
                if (setting instanceof NumberSetting num) {
                    renderNumberSetting(context, panelX, y, num, mouseX, mouseY, fadeIn, idx);
                } else if (setting instanceof ColorSetting color) {
                    renderColorSetting(context, panelX, y, color, mouseX, mouseY, fadeIn);
                } else if (setting instanceof TextSetting text) {
                    renderTextSetting(context, panelX, y, text, mouseX, mouseY, fadeIn, idx);
                } else if (setting instanceof BooleanSetting bool) {
                    renderBooleanSetting(context, panelX, y, bool, mouseX, mouseY, fadeIn, idx);
                }
            }
            idx++;
        }

        context.fill(panelX + 1, panelY + panelHeight - FOOTER_HEIGHT - 1, panelX + PANEL_WIDTH - 1, panelY + panelHeight - 1, ((int)(panelAlpha * 0.9f) << 24) | 0xDDDDDD);
        context.drawCenteredString(MC.getMc().font, Component.literal("ESC to close"), panelX + PANEL_WIDTH / 2, panelY + panelHeight - FOOTER_HEIGHT / 2 - 2, (int)(200 * fadeIn) << 24 | 0xFF0000);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    private void renderNumberSetting(GuiGraphics context, int panelX, int y, NumberSetting setting, int mouseX, int mouseY, float fadeIn, int idx) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int labelWidth = 55;
        int valueWidth = 45;
        int barWidth = width - labelWidth - valueWidth - 5;

        String displayValue = (typingSettingIdx == idx) ? typingBuffer : String.format("%.1f", setting.getValue());
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);
        context.drawString(MC.getMc().font, Component.literal(displayValue), x + width - valueWidth, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);

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
        context.fill(barX, barY, barX + barWidth, barY + 4, (int)(100 * fadeIn) << 24 | 0xFF4444);

        double progress = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;
        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            context.fill(barX, barY, barX + fillWidth, barY + 4, (int)(220 * fadeIn) << 24 | 0xFF0000);
        }

        int handleX = barX + fillWidth - 1;
        int handlePulse = draggingSlider && draggingSettingIdx == idx ? (int)(30 * Math.abs(Math.sin(openTicks * 0.3f))) : 0;
        int handleColor = ((int)(255 * fadeIn) << 24) | (0xFF4444 + handlePulse);
        context.fill(handleX, barY - 3, handleX + 3, barY + 7, handleColor);
    }

    private void renderColorSetting(GuiGraphics context, int panelX, int y, ColorSetting setting, int mouseX, int mouseY, float fadeIn) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);

        int colorBoxX = x + width - 30;
        int colorBoxY = y + 4;
        context.fill(colorBoxX, colorBoxY, colorBoxX + 25, colorBoxY + 12, setting.getColor());
        context.fill(colorBoxX, colorBoxY, colorBoxX + 1, colorBoxY + 12, (int)(255 * fadeIn) << 24 | 0xFF4444);
        context.fill(colorBoxX + 24, colorBoxY, colorBoxX + 25, colorBoxY + 12, (int)(255 * fadeIn) << 24 | 0xFF4444);
        context.fill(colorBoxX, colorBoxY, colorBoxX + 25, colorBoxY + 1, (int)(255 * fadeIn) << 24 | 0xFF4444);
        context.fill(colorBoxX, colorBoxY + 11, colorBoxX + 25, colorBoxY + 12, (int)(255 * fadeIn) << 24 | 0xFF4444);
    }

    private void renderTextSetting(GuiGraphics context, int panelX, int y, TextSetting setting, int mouseX, int mouseY, float fadeIn, int idx) {
        int x = panelX + PADDING;
        int width = PANEL_WIDTH - PADDING * 2;
        int valueWidth = width - 60;

        String displayValue = (typingSettingIdx == idx) ? typingBuffer : setting.getValue();
        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);
        context.drawString(MC.getMc().font, Component.literal(displayValue), x + 55, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);

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

        context.drawString(MC.getMc().font, Component.literal(setting.getName()), x, y + 8, (int)(255 * fadeIn) << 24 | 0xFF0000, true);

        int boxX = x + width - boxSize - 30;
        int boxY = y + 8;
        context.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, setting.getValue() ? (int)(255 * fadeIn) << 24 | 0xFF4444 : (int)(100 * fadeIn) << 24 | 0xFF4444);
        context.fill(boxX, boxY, boxX + 1, boxY + boxSize, 0xFFFFFFFF);
        context.fill(boxX + boxSize - 1, boxY, boxX + boxSize, boxY + boxSize, 0xFF333333);
        context.fill(boxX, boxY, boxX + boxSize, boxY + 1, 0xFFFFFFFF);
        context.fill(boxX, boxY + boxSize - 1, boxX + boxSize, boxY + boxSize, 0xFF333333);

        String text = setting.getValue() ? "ON" : "OFF";
        context.drawString(MC.getMc().font, Component.literal(text), boxX + boxSize + 5, y + 8, setting.getValue() ? (int)(255 * fadeIn) << 24 | 0xFF4444 : (int)(200 * fadeIn) << 24 | 0xFF4444, true);
    }

    private boolean isMouseOverSlider(int mouseX, int mouseY, int settingIdx) {
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

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
        int labelWidth = 55;
        int valueWidth = 45;
        int barWidth = width - labelWidth - valueWidth - 5;
        int barX = x + labelWidth + 5;
        int barY = y + 10;

        return mouseX >= barX - 4 && mouseX <= barX + barWidth + 4 && mouseY >= barY - 6 && mouseY <= barY + 10;
    }

    private boolean isMouseOverHeader(int mouseX, int mouseY) {
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

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
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

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
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

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
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;

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
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        if (button == 0) {
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
                }
                idx++;
            }
        }

        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (draggingPanel) {
            panelOffsetX += (int) (mouseX - panelDragStartX);
            panelOffsetY += (int) (mouseY - panelDragStartY);
            panelDragStartX = (int) mouseX;
            panelDragStartY = (int) mouseY;
            return true;
        }

        if (draggingSlider && draggingSettingIdx >= 0) {
            int idx = 0;
            for (Setting setting : module.getSettings()) {
                if (idx == draggingSettingIdx && setting instanceof NumberSetting num) {
                    applySlider((int) mouseX, num);
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
        if (typingSettingIdx >= 0) {

            if (event.key() == 256) {
                typingSettingIdx = -1;
                typingBuffer = "";
                cursorPosition = 0;
                return true;
            }

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
                    return super.keyPressed(event);
                }
                idx++;
            }
            return true;
        }

        if (event.key() == 256) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        if (typingSettingIdx >= 0) {
            char c;
            try {
                c = (char) event.getClass().getMethod("getCodepoint").invoke(event);
            } catch (Exception ex) {
                c = ' ';
            }
            if (c >= 32 && c != 127) {
                int idx = 0;
                for (Setting setting : module.getSettings()) {
                    if (idx == typingSettingIdx) {
                        if (setting instanceof TextSetting) {
                            typingBuffer = typingBuffer.substring(0, cursorPosition) + c + typingBuffer.substring(cursorPosition);
                            cursorPosition++;
                            return true;
                        }
                        break;
                    }
                    idx++;
                }
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
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int settingsCount = module.getSettings().size();
        int contentHeight = settingsCount * SETTING_HEIGHT;
        int panelHeight = HEADER_HEIGHT + contentHeight + FOOTER_HEIGHT + PADDING * 2;
        int panelX = (screenWidth - PANEL_WIDTH) / 2 + panelOffsetX;
        int panelY = (MC.getMc().getWindow().getGuiScaledHeight() - panelHeight) / 2 + panelOffsetY;
        if (panelY < 10) panelY = 10;
        if (panelX < 10) panelX = 10;

        int settingsY = panelY + HEADER_HEIGHT;
        int width = PANEL_WIDTH - PADDING * 2;
        int labelWidth = 55;
        int valueWidth = 45;
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

    public boolean mouseScrolled(double x, double y, double delta) {
        scrollOffset += delta * 10;
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        return true;
    }
}

