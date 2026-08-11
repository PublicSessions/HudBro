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

import java.util.ArrayDeque;

public class CpsHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 78.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "{LeftCPS} | {RightCPS}", "Display format"));

    private final ArrayDeque<Long> leftClicks = new ArrayDeque<>();
    private final ArrayDeque<Long> rightClicks = new ArrayDeque<>();
    private boolean wasLeftDown = false;
    private boolean wasRightDown = false;

    public CpsHud() {
        super("CPS", "Shows left/right clicks per second.", Category.HUD);
        this.setChinese("点击速度");
        this.setChineseDescription("显示左右键CPS");
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
        String sample = format.getValue().replace("{LeftCPS}", "99").replace("{RightCPS}", "99");
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
        long now = System.currentTimeMillis();
        boolean leftDown = isLeftDown();
        boolean rightDown = isRightDown();

        if (leftDown && !wasLeftDown) {
            leftClicks.addLast(now);
        }
        if (rightDown && !wasRightDown) {
            rightClicks.addLast(now);
        }
        wasLeftDown = leftDown;
        wasRightDown = rightDown;

        while (!leftClicks.isEmpty() && leftClicks.peekFirst() < now - 1000) {
            leftClicks.pollFirst();
        }
        while (!rightClicks.isEmpty() && rightClicks.peekFirst() < now - 1000) {
            rightClicks.pollFirst();
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

    private static boolean isLeftDown() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return false;
        long h = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        if (h == 0) {
            return client.mouseHandler != null && client.mouseHandler.isLeftPressed();
        }
        return org.lwjgl.glfw.GLFW.glfwGetMouseButton(h, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private static boolean isRightDown() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return false;
        long h = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        if (h == 0) {
            return client.mouseHandler != null && client.mouseHandler.isRightPressed();
        }
        return org.lwjgl.glfw.GLFW.glfwGetMouseButton(h, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private String getText() {
        return format.getValue()
                .replace("{LeftCPS}", Integer.toString(leftClicks.size()))
                .replace("{RightCPS}", Integer.toString(rightClicks.size()));
    }
}