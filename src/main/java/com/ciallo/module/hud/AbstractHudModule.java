package com.ciallo.module.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.NumberSetting;

public abstract class AbstractHudModule extends Module implements Listener3 {
    protected boolean editing;

    public AbstractHudModule(String name, String description, Category category) {
        super(name, description, category);
    }

    public final NumberSetting relX = (NumberSetting) this.m28(new NumberSetting("RelX", 0.0, 0.0, 1.0, 0.001));
    public final NumberSetting relY = (NumberSetting) this.m28(new NumberSetting("RelY", 0.0, 0.0, 1.0, 0.001));

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void render(GuiGraphics context, float partialTicks);

    public int getX() {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int x = (int) Math.round(relX.getValue() * screenWidth);
        int maxX = screenWidth - getWidth();
        if (maxX < 0) maxX = 0;
        return Math.max(0, Math.min(maxX, x));
    }

    public int getY() {
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int y = (int) Math.round(relY.getValue() * screenHeight);
        int maxY = screenHeight - getHeight();
        if (maxY < 0) maxY = 0;
        return Math.max(0, Math.min(maxY, y));
    }

    public void setPosition(int x, int y) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (screenWidth <= 0) screenWidth = 960;
        if (screenHeight <= 0) screenHeight = 540;
        int maxX = screenWidth - getWidth();
        int maxY = screenHeight - getHeight();
        if (maxX < 0) maxX = 0;
        if (maxY < 0) maxY = 0;
        x = Math.max(0, Math.min(maxX, x));
        y = Math.max(0, Math.min(maxY, y));
        this.relX.setValue((double) x / screenWidth);
        this.relY.setValue((double) y / screenHeight);
    }

    public boolean isEditorMode() {
        return editing;
    }

    public void setEditorMode(boolean editing) {
        this.editing = editing;
    }
}

