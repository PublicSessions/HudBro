package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.util.MC;

public class ArmorHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 54.0, 0.0, 463.0, 1.0, 1.0));
    public final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    public final BooleanSetting durability = (BooleanSetting) this.m28(new BooleanSetting("Durability", true));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));

    public ArmorHud() {
        super("ArmorHud", "Shows armor status.", Category.HUD);
        this.setChinese("??HUD");
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
        return Math.round(80.0f * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round((durability.getValue() ? 28.0f : 16.0f) * scale.getFloat());
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

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);
        context.pose().scale(scale.getFloat(), scale.getFloat());

        if (MC.getMc().player == null) {
            if (isEditorMode()) {
                context.fill(0, 0, getWidth(), getHeight(), 1427445792);
                context.drawString(MC.getMc().font, "Armor HUD", 4, 2, -1184275, shadow.getValue());
            }
            context.pose().popMatrix();
            return;
        }

        int itemX = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack item = MC.getMc().player.getInventory().getItem(36 + i);
            if (!item.isEmpty()) {
                context.renderItem(item, itemX + 2, 0);
                context.renderItemDecorations(MC.getMc().font, item, itemX + 2, 0, null);
                if (durability.getValue() && item.getMaxDamage() > 0) {
                    int maxDamage = item.getMaxDamage();
                    int damage = item.getDamageValue();
                    int percent = (int) ((float) (maxDamage - damage) / maxDamage * 100.0f);
                    int color = getDurabilityColor(percent);
                    String text = percent + "%";
                    int textWidth = MC.getMc().font.width(text);
                    int textX = itemX + 10 - textWidth / 2;
                    context.drawString(MC.getMc().font, text, textX, 18, color, shadow.getValue());
                }
            } else if (isEditorMode()) {
                context.fill(itemX + 2, 0, itemX + 18, 16, 0x22FFFFFF);
            }
            itemX += 20;
        }

        context.pose().popMatrix();
    }

    private int getDurabilityColor(int percent) {
        float f = Math.max(0.0f, Math.min(1.0f, percent / 100.0f));
        int red = (int) (196.0f + -196.0f * f);
        int green = (int) (0.0f + 227.0f * f);
        return 0xFF000000 | red << 16 | green << 8;
    }
}

