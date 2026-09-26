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

public class DamageHud extends AbstractHudModule implements Listener3 {
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Health : {Health} / {MaxHealth} | Damage : {Damage}", "Display format"));
    private final BooleanSetting showAbsorption = (BooleanSetting) this.m28(new BooleanSetting("Show Absorption", true));

    private float lastHealth = -1.0f;
    private float totalDamage = 0.0f;
    private long lastDamageTime = 0L;
    private static final long DAMAGE_RESET_MS = 2000L;

    public DamageHud() {
        super("DamageHud", "Shows health and damage taken.", Category.HUD);
        this.setChinese("伤害HUD");
        this.setChineseDescription("显示生命值和受到的伤害");
        this.relX.setValue(0.01);
        this.relY.setValue(0.15);
    }

    @Override
    public int getWidth() {
        String sample = format.getValue()
                .replace("{Health}", "20")
                .replace("{MaxHealth}", "20")
                .replace("{Absorption}", "0")
                .replace("{Damage}", "0.0")
                .replace("{Percent}", "100%");
        return Math.round(MC.getMc().font.width(sample) * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(MC.getMc().font.lineHeight * scale.getFloat());
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.getMc().player == null || MC.getMc().level == null) {
            lastHealth = -1.0f;
            totalDamage = 0.0f;
            return;
        }

        float currentHealth = Math.max(0.0f, MC.getMc().player.getHealth());
        float maxHealth = MC.getMc().player.getMaxHealth();
        float absorption = MC.getMc().player.getAbsorptionAmount();
        float currentPool = currentHealth + absorption;

        if (lastHealth >= 0.0f && currentPool < lastHealth) {
            totalDamage = lastHealth - currentPool;
            lastDamageTime = System.currentTimeMillis();
        }
        if (currentHealth == 0.0f && lastHealth > 0.0f) {
            totalDamage = 0.0f;
            lastDamageTime = 0L;
        }
        if (totalDamage > 0.0f && lastDamageTime != 0L
                && System.currentTimeMillis() - lastDamageTime > DAMAGE_RESET_MS) {
            totalDamage = 0.0f;
            lastDamageTime = 0L;
        }
        lastHealth = currentPool;

        int posX = getX();
        int posY = getY();
        String text = getText(currentHealth, maxHealth, absorption);

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);
        context.pose().scale(scale.getFloat(), scale.getFloat());
        context.drawString(MC.getMc().font, text, 0, 0, color.getColor(), shadow.getValue());
        context.pose().popMatrix();
    }

    private String getText(float health, float maxHealth, float absorption) {
        String fmt = format.getValue();
        fmt = fmt.replace("{Health}", String.format("%.1f", health));
        fmt = fmt.replace("{MaxHealth}", String.format("%.1f", maxHealth));
        fmt = fmt.replace("{Damage}", String.format("%.1f", totalDamage));
        fmt = fmt.replace("{Percent}", String.format("%.0f%%", (health / maxHealth) * 100.0f));
        fmt = fmt.replace("{Absorption}", showAbsorption.getValue() ? String.format("%.1f", absorption) : "0");
        return fmt;
    }
}
