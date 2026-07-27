package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.util.MC;

public class InventoryViewer extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 180.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 6.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting background = (BooleanSetting) this.m28(new BooleanSetting("Background", true));
    private final BooleanSetting border = (BooleanSetting) this.m28(new BooleanSetting("Border", true));
    private final ColorSetting backgroundColor = (ColorSetting) this.m28(new ColorSetting("BackgroundColor", -2012213224));
    private final ColorSetting borderColor = (ColorSetting) this.m28(new ColorSetting("BorderColor", -9971969));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));

    public InventoryViewer() {
        super("InventoryViewer", "Shows the main inventory.", Category.HUD);
        this.setChinese("物品栏查看器");
        this.setChineseDescription("查看背包物品");
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
        return Math.round(162.0f * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(54.0f * scale.getFloat());
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

        boolean editorMode = isEditorMode();
        boolean showBackground = background.getValue();

        if (!editorMode && showBackground) {
            context.fill(0, 0, getWidth(), getHeight(), backgroundColor.getColor());
        }

        boolean showBorder = border.getValue();
        if (!editorMode && showBorder) {
            int borderColorVal = borderColor.getColor();
            context.fill(0, 0, getWidth(), 1, borderColorVal);
            context.fill(0, getHeight() - 1, getWidth(), getHeight(), borderColorVal);
            context.fill(0, 0, 1, getHeight(), borderColorVal);
            context.fill(getWidth() - 1, 0, getWidth(), getHeight(), borderColorVal);
        }

        if (MC.getMc().player == null) {
            if (editorMode) {
                context.drawString(MC.getMc().font, "Inventory", 4, 4, -1184275, true);
            }
            context.pose().popMatrix();
            return;
        }

        for (int i = 9; i < 36; i++) {
            ItemStack item = MC.getMc().player.getInventory().getItem(i);
            int slotIndex = i - 9;
            int slotX = 1 + slotIndex % 9 * 18;
            int slotY = 1 + slotIndex / 9 * 18;
            if (!item.isEmpty()) {
                context.renderItem(item, slotX, slotY);
                context.renderItemDecorations(MC.getMc().font, item, slotX, slotY, null);
            }
        }

        context.pose().popMatrix();
    }
}

