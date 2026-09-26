package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

import java.util.Collection;

public class PotionEffectsHud extends AbstractHudModule implements Listener3 {
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting backgroundColor = (ColorSetting) this.m28(new ColorSetting("BackgroundColor", 0xDD000000));
    private final ColorSetting textColor = (ColorSetting) this.m28(new ColorSetting("TextColor", 0xFFFFFFFF));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting durationFormat = (TextSetting) this.m28(new TextSetting("DurationFormat", "mm:ss", "Duration format: mm:ss or ms"));

    public PotionEffectsHud() {
        super("PotionEffects", "Shows active potion effects.", Category.HUD);
        this.setChinese("药水效果");
        this.setChineseDescription("显示药水效果");
        this.relX.setValue(0.01);
        this.relY.setValue(0.3);
    }

    @Override
    public int getWidth() {
        return Math.round(120 * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(MC.getMc().font.lineHeight * scale.getFloat() * 5);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.getMc().player == null) return;

        int posX = getX();
        int posY = getY();
        float s = scale.getFloat();
        Collection<MobEffectInstance> effects = MC.getMc().player.getActiveEffects();
        int textHeight = (int) (MC.getMc().font.lineHeight * s);
        int rowHeight = textHeight + (int) (2 * s);
        int rowWidth = (int) (120 * s);

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);

        int currentY = 0;
        for (MobEffectInstance effect : effects) {
            if (!effect.isVisible()) continue;

            int bgColor = backgroundColor.getColor();
            int txtColor = textColor.getColor();

            context.fill(0, currentY, rowWidth, currentY + rowHeight, bgColor);

            String name = effect.getEffect().value().getDisplayName().getString();
            String duration = formatDuration(effect.getDuration());
            context.drawString(MC.getMc().font, name + " " + duration, (int) (2 * s), currentY + (int) (1 * s), txtColor, shadow.getValue());

            currentY += rowHeight;
        }

        context.pose().popMatrix();
    }

    private String formatDuration(int ticks) {
        String format = durationFormat.getValue().toLowerCase();
        if (format.equals("ms")) {
            return ticks * 50 + "ms";
        }
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

