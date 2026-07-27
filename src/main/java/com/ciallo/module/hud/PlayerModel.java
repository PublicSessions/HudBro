package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.util.MC;

public class PlayerModel extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 350.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 112.0, 0.0, 463.0, 1.0, 1.0));
    private final NumberSetting width = (NumberSetting) this.m28(new NumberSetting("Width", 50.0, 30.0, 160.0, 1.0));
    private final NumberSetting height = (NumberSetting) this.m28(new NumberSetting("Height", 80.0, 50.0, 220.0, 1.0));
    private final BooleanSetting emulateYaw = (BooleanSetting) this.m28(new BooleanSetting("EmulateYaw", true));
    private final BooleanSetting emulatePitch = (BooleanSetting) this.m28(new BooleanSetting("EmulatePitch", true));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private float value165 = 0.0f;
    private float value201 = 0.0f;
    private boolean flag150 = false;

    public PlayerModel() {
        super("PlayerModel", "Shows your player model.", Category.HUD);
        this.setChinese("玩家模型");
        this.setChineseDescription("显示玩家模型");
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
        return Math.round(width.getFloat() * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(height.getFloat() * scale.getFloat());
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
        int scaledWidth = getWidth();
        int scaledHeight = getHeight();

        if (MC.client3.player == null) {
            if (isEditorMode()) {
                context.fill(posX, posY, posX + scaledWidth, posY + scaledHeight, 1427445792);
                context.drawString(MC.client3.font, "Player", posX + 4, posY + 4, -1184275, true);
            }
            return;
        }

        float yaw = 0.0f;
        float pitch = 0.0f;

        if (emulateYaw.getValue() || emulatePitch.getValue()) {
            float currentYaw = MC.client3.player.getYRot();
            if (!flag150) {
                value201 = currentYaw;
                flag150 = true;
            }
            float delta = wrapDegrees(currentYaw - value201);
            value201 = currentYaw;
            value165 += delta;
            value165 *= 0.82f;
            value165 = clamp(value165, -30.0f, 30.0f);

            if (emulateYaw.getValue()) {
                yaw = value165;
            }
            if (emulatePitch.getValue()) {
                pitch = clamp(MC.client3.player.getXRot(), -30.0f, 30.0f);
            }
        }

        int size = Math.max(18, Math.min(scaledWidth, scaledHeight) / 2);
        float cx = (float)(posX + posX + scaledWidth) / 2.0f;
        float cy = (float)(posY + posY + scaledHeight) / 2.0f;
        float ex = cx - (float)Math.tan((double)(yaw / 20.0f)) * 40.0f;
        float ey = cy - (float)Math.tan((double)(-pitch / 20.0f)) * 40.0f;
        InventoryScreen.renderEntityInInventoryFollowsMouse(context, posX, posY, posX + scaledWidth, posY + scaledHeight, size, 0.0f, ex, ey, MC.client3.player);
    }

    private float wrapDegrees(float value) {
        value = value % 360.0f;
        if (value >= 180.0f) value -= 360.0f;
        if (value < -180.0f) value += 360.0f;
        return value;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

