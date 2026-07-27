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

public class BrandHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Brand : {Brand}", "Display format"));

    public BrandHud() {
        super("Brand", "Shows server brand.", Category.HUD);
        this.setChinese("????");
        this.setChineseDescription("???????");
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
        String brand = "Vanilla";
        try {
            if (!MC.client3.isSingleplayer() && MC.client3.getConnection() != null) {
                try {
                    var method = MC.client3.getConnection().getClass().getMethod("getBrand");
                    brand = method.invoke(MC.client3.getConnection()).toString().replaceAll("\\(.*?\\)", "");
                } catch (Exception e) {
                    brand = "Unknown";
                }
            }
        } catch (Exception e) {
            brand = "Unknown";
        }
        return format.getValue().replace("{Brand}", brand);
    }
}

