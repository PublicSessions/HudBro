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

public class ComboHud extends AbstractHudModule implements Listener3 {
    private static ComboHud instance;
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 90.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Combo : {Combo}", "Display format"));
    private final NumberSetting comboWindow = (NumberSetting) this.m28(new NumberSetting("Combo Window (ms)", 5000.0, 500.0, 10000.0, 100.0));

    private int comboCount = 0;
    private long lastHitTime = 0;
    private float lastHealth = -1.0f;

    public ComboHud() {
        super("Combo", "Shows hit combo count.", Category.HUD);
        this.setChinese("连击数");
        this.setChineseDescription("显示连击数");
        instance = this;
    }

    public static void onHit() {
        if (instance == null) return;
        instance.comboCount++;
        instance.lastHitTime = System.currentTimeMillis();
    }

    public static void resetCombo() {
        if (instance == null) return;
        instance.comboCount = 0;
        instance.lastHitTime = 0;
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
        String sample = format.getValue().replace("{Combo}", "99");
        return Math.round(MC.getMc().font.width(sample) * scale.getFloat());
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
        var player = MC.getMc().player;
        if (player != null) {
            float health = player.getHealth();
            if (lastHealth >= 0.0f && health < lastHealth) {
                resetCombo();
            }
            lastHealth = health;
        }

        long now = System.currentTimeMillis();
        long window = (long) comboWindow.getValue();
        if (comboCount > 0 && now - lastHitTime > window) {
            comboCount = 0;
        }

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
        return format.getValue().replace("{Combo}", Integer.toString(comboCount));
    }
}