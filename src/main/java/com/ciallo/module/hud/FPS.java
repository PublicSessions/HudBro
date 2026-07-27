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

public class FPS extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "FPS : {Fps}", "Display format"));

    public FPS() {
        super("FPS", "Shows current FPS.", Category.HUD);
        this.setChinese("帧率显示");
        this.setChineseDescription("显示帧率");
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
        return Math.round(MC.client3.font.width(getText()) * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(MC.client3.font.lineHeight * scale.getFloat());
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

        context.pose().pushPose();
        context.pose().translate((double)posX, (double)posY, 0.0);
        context.pose().scale(scale.getFloat(), scale.getFloat(), 1.0f);
        context.drawString(MC.client3.font, text, 0, 0, color.getColor(), shadow.getValue());
        context.pose().popPose();
    }

    private String getText() {
        int fps = 0;
        try {
            java.lang.reflect.Method m = MC.client3.getClass().getMethod("getFps");
            fps = (Integer) m.invoke(MC.client3);
        } catch (Exception e) {
            try {
                java.lang.reflect.Method m = MC.client3.getClass().getMethod("getCurrentFps");
                fps = (Integer) m.invoke(MC.client3);
            } catch (Exception ex) {
                try {
                    for (java.lang.reflect.Method m : MC.client3.getClass().getMethods()) {
                        if (m.getReturnType() == int.class && m.getParameterCount() == 0) {
                            String name = m.getName();
                            if (name.toLowerCase().contains("fps") || name.toLowerCase().contains("framerate")) {
                                fps = (Integer) m.invoke(MC.client3);
                                break;
                            }
                        }
                    }
                } catch (Exception ex2) {
                    fps = 0;
                }
            }
        }
        return format.getValue().replace("{Fps}", Integer.toString(fps));
    }
}

