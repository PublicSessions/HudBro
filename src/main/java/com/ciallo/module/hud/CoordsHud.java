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
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 10.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "XYZ: {x} / {y} / {z}", "Display format"));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1));
    private int lastWidth = 100;
    private int lastHeight = 9;

    public CoordsHud() {
        super("CoordsHud", "Displays player coordinates.", Category.HUD);
        this.setChinese("坐标HUD");
        this.setChineseDescription("显示玩家坐标");
    }

    @Override
    public int getX() {
        return x.getInt();
    }

    @Override
    public int getY() {
        return y.getInt();
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
    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.client3.player == null) {
            return;
        }

        int posX = getX();
        int posY = getY();

        context.pose().pushPose();
        context.pose().translate((double)posX, (double)posY, 0.0);
        context.pose().scale(scale.getFloat(), scale.getFloat(), 1.0f);

        double px = MC.client3.player.getX();
        double py = MC.client3.player.getY();
        double pz = MC.client3.player.getZ();
        String template = format.getValue();
        String text = template.replace("{x}", String.format("%.1f", px)).replace("{y}", String.format("%.1f", py)).replace("{z}", String.format("%.1f", pz));
        context.drawString(MC.client3.font, text, 0, 0, color.getColor(), true);
        lastWidth = Math.round(MC.client3.font.width(text) * scale.getFloat() + 1);
        lastHeight = Math.round(9.0f * scale.getFloat());

        context.pose().popPose();
    }
}

