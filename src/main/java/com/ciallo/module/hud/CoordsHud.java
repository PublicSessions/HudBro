package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.util.MC;

public class CoordsHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "XYZ: {x} / {y} / {z}", "Display format"));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1));
    private int lastWidth = 100;
    private int lastHeight = 9;

    public CoordsHud() {
        super("CoordsHud", "Displays player coordinates.", Category.HUD);
        this.setChinese("坐标HUD");
        this.setChineseDescription("显示玩家坐标");
        this.relX.setValue(0.01);
        this.relY.setValue(0.05);
    }

    @Override
    public int getWidth() {
        return lastWidth;
    }

    @Override
    public int getHeight() {
        return lastHeight;
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.getMc().player == null) {
            return;
        }

        int posX = getX();
        int posY = getY();

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);
        context.pose().scale(scale.getFloat(), scale.getFloat());

        double px = MC.getMc().player.getX();
        double py = MC.getMc().player.getY();
        double pz = MC.getMc().player.getZ();
        String template = format.getValue();
        String text = template.replace("{x}", String.format("%.1f", px)).replace("{y}", String.format("%.1f", py)).replace("{z}", String.format("%.1f", pz));
        context.drawString(MC.getMc().font, text, 0, 0, color.getColor(), true);
        lastWidth = Math.round(MC.getMc().font.width(text) * scale.getFloat() + 1);
        lastHeight = Math.round(9.0f * scale.getFloat());

        context.pose().popMatrix();
    }
}

