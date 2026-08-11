package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

public class ReachHud extends AbstractHudModule implements Listener3 {
    private static ReachHud instance;
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 108.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "Reach : {Reach}", "Display format"));

    private double lastDistance = 0.0;
    private boolean hasHit = false;

    public ReachHud() {
        super("Reach", "Shows last hit distance.", Category.HUD);
        this.setChinese(" Reach");
        this.setChineseDescription("显示上次攻击距离");
        instance = this;
    }

    public static void onAttack(Player player, Entity entity) {
        if (instance != null && player != null && entity != null) {
            Vec3 eyePos = player.getEyePosition();
            double reach = distanceToBox(eyePos, entity.getBoundingBox());
            instance.lastDistance = reach;
            instance.hasHit = true;
        }
    }

    private static double distanceToBox(Vec3 point, net.minecraft.world.phys.AABB box) {
        double closestX = Math.max(box.minX, Math.min(point.x, box.maxX));
        double closestY = Math.max(box.minY, Math.min(point.y, box.maxY));
        double closestZ = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        Vec3 closest = new Vec3(closestX, closestY, closestZ);
        return point.distanceTo(closest);
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
        if (!hasHit) {
            return format.getValue().replace("{Reach}", "N/A");
        }
        return format.getValue().replace("{Reach}", String.format("%.2f", lastDistance));
    }
}
