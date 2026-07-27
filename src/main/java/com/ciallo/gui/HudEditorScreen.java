package com.ciallo.gui;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;


import net.minecraft.network.chat.Component;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.module.ModuleManager;
import com.ciallo.util.MC;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {
    private AbstractHudModule draggedModule;
    private int dragOffsetX;
    private int dragOffsetY;
    private int mouseX;
    private int mouseY;
    private static final int SNAP_DISTANCE = 8;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    public static boolean isActive() {
        return MC.getMc().screen instanceof HudEditorScreen;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        context.fill(0, 0, screenWidth, screenHeight, 0x80000000);
        context.fill(centerX - 1, 0, centerX + 1, screenHeight, 0x40FFFFFF);
        context.fill(0, centerY - 1, screenWidth, centerY + 1, 0x40FFFFFF);

        renderHudElements(context, delta);
        renderDragPreview(context, mouseX, mouseY, centerX, centerY);

        context.drawCenteredString(MC.getMc().font, Component.literal("HUD Editor - ESC to close - /hudbro"), screenWidth / 2, 10, 0xFFFFFFFF);
    }

    private void renderHudElements(GuiGraphics context, float delta) {
        List<AbstractHudModule> huds = new ArrayList<>(ModuleManager.INSTANCE.getHudModules());
        for (AbstractHudModule hud : huds) {
            hud.setEditorMode(true);
            hud.render(context, delta);
        }
    }

    private void renderDragPreview(GuiGraphics context, int mouseX, int mouseY, int centerX, int centerY) {
        List<AbstractHudModule> huds = new ArrayList<>(ModuleManager.INSTANCE.getHudModules());
        for (AbstractHudModule hud : huds) {
            int x = hud.getX();
            int y = hud.getY();
            int w = hud.getWidth();
            int h = hud.getHeight();
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                context.fill(x, y, x + w, y + h, 0x30FFFFFF);
                context.drawString(MC.getMc().font, hud.getName(), x, y - 10, 0xFFFFFFFF, true);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int button = event.button();

        if (button == 0) {
            List<AbstractHudModule> huds = new ArrayList<>(ModuleManager.INSTANCE.getHudModules());
            for (int i = huds.size() - 1; i >= 0; i--) {
                AbstractHudModule hud = huds.get(i);
                int x = hud.getX();
                int y = hud.getY();
                int w = hud.getWidth();
                int h = hud.getHeight();
                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    draggedModule = hud;
                    dragOffsetX = (int) mouseX - x;
                    dragOffsetY = (int) mouseY - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (draggedModule != null) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;

            int w = draggedModule.getWidth();
            int h = draggedModule.getHeight();
            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
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
            return true;
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggedModule != null) {
            draggedModule = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            MC.getMc().setScreen(null);
            return true;
        }
        return super.keyPressed(event);
    }
}

