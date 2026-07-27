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
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting backgroundColor = (ColorSetting) this.m28(new ColorSetting("BackgroundColor", 0xDD000000));
    private final ColorSetting textColor = (ColorSetting) this.m28(new ColorSetting("TextColor", 0xFFFFFFFF));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting durationFormat = (TextSetting) this.m28(new TextSetting("DurationFormat", "mm:ss", "Duration format: mm:ss or ms"));

    public PotionEffectsHud() {
        super("PotionEffects", "Shows active potion effects.", Category.HUD);
        this.setChinese("药水效果");
        this.setChineseDescription("显示药水效果");
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
        return Math.round(120 * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(MC.client3.font.lineHeight * scale.getFloat() * 5);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.client3.player == null) return;

        int posX = getX();
        int posY = getY();
        float s = scale.getFloat();
        Collection<MobEffectInstance> effects = MC.client3.player.getActiveEffects();
        int textHeight = (int) (MC.client3.font.lineHeight * s);
        int rowHeight = textHeight + (int) (2 * s);
        int rowWidth = (int) (120 * s);

        context.pose().pushPose();
        context.pose().translate((double)posX, (double)posY, 0.0);

        int currentY = 0;
        for (MobEffectInstance effect : effects) {
            if (!effect.isVisible()) continue;

            int bgColor = backgroundColor.getColor();
            int txtColor = textColor.getColor();

            context.fill(0, currentY, rowWidth, currentY + rowHeight, bgColor);

            String name = effect.getEffect().value().getDisplayName().getString();
            String duration = formatDuration(effect.getDuration());
            context.drawString(MC.client3.font, name + " " + duration, (int) (2 * s), currentY + (int) (1 * s), txtColor, shadow.getValue());

            currentY += rowHeight;
        }

        context.pose().popPose();
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

