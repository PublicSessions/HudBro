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

public class MovementHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 54.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting sprintText = (TextSetting) this.m28(new TextSetting("Sprint", "Sprinting", "Text shown while sprinting"));
    private final TextSetting sneakText = (TextSetting) this.m28(new TextSetting("Sneak", "Sneaking", "Text shown while sneaking"));
    private final TextSetting walkText = (TextSetting) this.m28(new TextSetting("Walk", "Walking", "Text shown while walking"));
    private final TextSetting idleText = (TextSetting) this.m28(new TextSetting("Idle", "Standing", "Text shown while standing still"));
    private final TextSetting crawlText = (TextSetting) this.m28(new TextSetting("Crawl", "Crawling", "Text shown while crawling"));
    private final TextSetting proneText = (TextSetting) this.m28(new TextSetting("Prone", "Prone", "Text shown while prone"));

    public MovementHud() {
        super("Movement", "Shows sprint/sneak/walk state.", Category.HUD);
        this.setChinese("移动状态");
        this.setChineseDescription("显示疾跑/蹲下/走路状态");
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
        int maxWidth = 0;
        for (String s : new String[]{sprintText.getValue(), sneakText.getValue(), walkText.getValue(), idleText.getValue(), crawlText.getValue(), proneText.getValue()}) {
            maxWidth = Math.max(maxWidth, MC.getMc().font.width(s));
        }
        return Math.round(maxWidth * scale.getFloat());
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
            if (MC.getMc().player != null) {
                var player = MC.getMc().player;
                boolean crawling = player.isVisuallyCrawling();
                boolean moving = player.getDeltaMovement().horizontalDistanceSqr() > 0.0001;

                if (crawling) {
                    return moving ? crawlText.getValue() : proneText.getValue();
                }
                if (player.isSprinting()) {
                    return sprintText.getValue();
                }
                if (player.isShiftKeyDown()) {
                    return sneakText.getValue();
                }
                if (moving) {
                    return walkText.getValue();
                }
                return idleText.getValue();
            }
        } catch (Exception e) {
            // fall through
        }
        return "";
    }
}
