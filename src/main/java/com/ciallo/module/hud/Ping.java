package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

public class Ping extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 42.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Ping : {Ping}", "Display format"));

    public Ping() {
        super("Ping", "Shows server latency.", Category.HUD);
        this.setChinese("延迟显示");
        this.setChineseDescription("显示网络延迟");
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
        int latency = 0;
        try {
            if (MC.getMc().player != null && MC.getMc().getConnection() != null) {
                ClientPacketListener connection = MC.getMc().getConnection();
                PlayerInfo info = connection.getPlayerInfo(MC.getMc().player.getUUID());
                if (info != null) {
                    latency = info.getLatency();
                }
            }
        } catch (Exception e) {
            latency = 0;
        }
        return format.getValue().replace("{Ping}", Integer.toString(latency));
    }
}

