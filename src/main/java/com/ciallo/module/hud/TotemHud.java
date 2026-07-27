package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.util.MC;

public class TotemHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 10.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 10.0, 0.0, 463.0, 1.0, 1.0));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1));

    public TotemHud() {
        super("TotemHud", "Displays totem count.", Category.HUD);
        this.setChinese("图腾HUD");
        this.setChineseDescription("显示图腾数量");
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
        return Math.round(16.0f * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(24.0f * scale.getFloat());
    }

    @Override
    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.client3.player == null) {
            return;
        }

        int posX = getX();
        int posY = getY();
        int count = getTotemCount();

        context.pose().pushPose();
        context.pose().translate((double)posX, (double)posY, 0.0);
        context.pose().scale(scale.getFloat(), scale.getFloat(), 1.0f);

        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
        context.renderItem(totem, 0, 0);
        context.drawString(MC.client3.font, Integer.toString(count), 16, 8, color.getColor(), true);

        context.pose().popPose();
    }

    private int getTotemCount() {
        if (MC.client3.player == null) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = MC.client3.player.getInventory().getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }

        ItemStack offHand = MC.client3.player.getOffhandItem();
        if (offHand.is(Items.TOTEM_OF_UNDYING)) {
            count += offHand.getCount();
        }

        return count;
    }
}

