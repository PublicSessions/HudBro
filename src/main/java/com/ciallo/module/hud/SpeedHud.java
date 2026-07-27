package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

public class SpeedHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Speed : {Speed}", "Display format"));

    private double lastX = 0.0;
    private double lastZ = 0.0;
    private long lastTime = System.currentTimeMillis();

    public SpeedHud() {
        super("Speed", "Shows movement speed.", Category.HUD);
        this.setChinese("速度显示");
        this.setChineseDescription("显示移动速度");
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
        return Math.round(MC.getMc().font.width(getText()) * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(MC.getMc().font.lineHeight * scale.getFloat());
    }

    @Override
    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        int posX = getX();
        int posY = getY();
        String text = getText();

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);
        context.pose().scale(scale.getFloat(), scale.getFloat());
        context.drawString(MC.getMc().font, text, 0, 0, color.getColor(), shadow.getValue());
        context.pose().popMatrix();
    }

    private String getText() {
        double speed = 0.0;
        try {
            if (MC.getMc().player != null) {
                double x = MC.getMc().player.getX();
                double z = MC.getMc().player.getZ();
                long now = System.currentTimeMillis();
                double dt = (now - lastTime) / 1000.0;
                if (dt > 0.001) {
                    double dx = x - lastX;
                    double dz = z - lastZ;
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    speed = (dist / dt) * 3.6;
                }
                lastX = x;
                lastZ = z;
                lastTime = now;
            }
        } catch (Exception e) {
            speed = 0.0;
        }
        return format.getValue().replace("{Speed}", String.format("%.1f", speed));
    }
}

