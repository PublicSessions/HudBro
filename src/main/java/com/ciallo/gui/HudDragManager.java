package com.ciallo.gui;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.module.ModuleManager;
import com.ciallo.gui.HudSettingsScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HudDragManager {
    private static final HudDragManager INSTANCE = new HudDragManager();
    private AbstractHudModule draggedModule;
    private int dragOffsetX;
    private int dragOffsetY;
    private AbstractHudModule hoveredModule;
    private boolean wasRightDown = false;
    private boolean wasLeftDown = false;
    private static final int SNAP_DISTANCE = 8;

    private HudDragManager() {}

    public static HudDragManager getInstance() {
        return INSTANCE;
    }

    private static long getWindowHandle(Window w) {
        try { return (long) w.getClass().getMethod("handle").invoke(w); }
        catch (Exception e) { try { return (long) w.getClass().getMethod("getHandle").invoke(w); } catch (Exception e2) { return 0; } }
    }

    private static boolean isLeftDown() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return false;
        long h = getWindowHandle(client.getWindow());
        if (h == 0) return false;
        return GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
    }

    private static boolean isRightDown() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) return false;
        long h = getWindowHandle(client.getWindow());
        if (h == 0) return false;
        return GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
    }

    public void renderHoverHighlight(GuiGraphics context) {
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();
        if (window == null) return;
        double scale = window.getGuiScale();
        if (scale == 0) scale = 1;
        int mouseX = (int) (client.mouseHandler.xpos() / scale);
        int mouseY = (int) (client.mouseHandler.ypos() / scale);

        hoveredModule = null;
        List<AbstractHudModule> huds = new ArrayList<>(ModuleManager.INSTANCE.getHudModules());
        for (int i = huds.size() - 1; i >= 0; i--) {
            AbstractHudModule hud = huds.get(i);
            if (!hud.isEnabled()) continue;
            int x = hud.getX();
            int y = hud.getY();
            int w = hud.getWidth();
            int h = hud.getHeight();
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                hoveredModule = hud;
                context.fill(x, y, x + w, y + h, 0x40FFFFFF);
                context.drawString(Minecraft.getInstance().font, hud.getName(), x, y - 10, 0xFFFFFFFF, true);
                break;
            }
        }
    }

    public void update() {
        Minecraft client = Minecraft.getInstance();
        if (client.mouseHandler == null || client.player == null) return;

        Window window = client.getWindow();
        if (window == null) return;
        double scale = window.getGuiScale();
        if (scale == 0) scale = 1;
        int mouseX = (int) (client.mouseHandler.xpos() / scale);
        int mouseY = (int) (client.mouseHandler.ypos() / scale);

        boolean leftDown = isLeftDown();
        boolean rightDown = isRightDown();

        if (leftDown && !wasLeftDown && hoveredModule != null && draggedModule == null) {
            draggedModule = hoveredModule;
            dragOffsetX = mouseX - draggedModule.getX();
            dragOffsetY = mouseY - draggedModule.getY();
        }

        if (draggedModule != null) {
            if (leftDown) {
                int newX = mouseX - dragOffsetX;
                int newY = mouseY - dragOffsetY;
                int w = draggedModule.getWidth();
                int h = draggedModule.getHeight();
                int screenWidth = window.getGuiScaledWidth();
                int screenHeight = window.getGuiScaledHeight();
                int centerX = screenWidth / 2;
                int centerY = screenHeight / 2;
                int elemCenterX = newX + w / 2;
                int elemCenterY = newY + h / 2;
                if (Math.abs(elemCenterX - centerX) < SNAP_DISTANCE) {
                    newX = centerX - w / 2;
                }
                if (Math.abs(elemCenterY - centerY) < SNAP_DISTANCE) {
                    newY = centerY - h / 2;
                }
                draggedModule.setPosition(newX, newY);
            } else {
                com.ciallo.config.HudConfig.save();
                draggedModule = null;
            }
        }

        if (rightDown && !wasRightDown && hoveredModule != null) {
            client.setScreen(new HudSettingsScreen(hoveredModule));
        }

        wasLeftDown = leftDown;
        wasRightDown = rightDown;
    }

    public void renderAlignmentLines(GuiGraphics context) {
        if (draggedModule == null) return;
        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        context.fill(centerX - 1, 0, centerX + 1, screenHeight, 0x40FFFFFF);
        context.fill(0, centerY - 1, screenWidth, centerY + 1, 0x40FFFFFF);
    }
}