package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

public class InGameTime extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 66.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "In-Game Time : {Time}", "Display format"));

    public InGameTime() {
        super("InGameTime", "Shows in-game time.", Category.HUD);
        this.setChinese("游戏内时间");
        this.setChineseDescription("显示 Minecraft 游戏内时间");
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
        try {
            if (MC.getMc().level != null) {
                long dayTime = MC.getMc().level.getDayTime();
                long ticksInDay = dayTime % 24000;
                if (ticksInDay < 0) ticksInDay += 24000;
                long hours = ticksInDay / 1000;
                long minutes = (ticksInDay % 1000) * 60 / 1000;
                String time = String.format("%02d:%02d", hours, minutes);
                return format.getValue().replace("{Time}", time);
            }
        } catch (Exception e) {
            // fall through
        }
        return format.getValue().replace("{Time}", "--:--");
    }
}