package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;

public abstract class AbstractHudModule extends Module implements Listener3 {
    protected boolean editing;

    public AbstractHudModule(String name, String description, Category category) {
        super(name, description, category);
    }

    public abstract int getX();

    public abstract int getY();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void setPosition(int x, int y);

    public abstract void render(GuiGraphics context, float partialTicks);

    public boolean isEditorMode() {
        return editing;
    }

    public void setEditorMode(boolean editing) {
        this.editing = editing;
    }
}

