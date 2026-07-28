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

import org.lwjgl.glfw.GLFW;

public class KeystrokesHud extends AbstractHudModule implements Listener3 {
    private final NumberSetting x = (NumberSetting) this.m28(new NumberSetting("X", 6.0, 0.0, 960.0, 1.0, 1.0));
    private final NumberSetting y = (NumberSetting) this.m28(new NumberSetting("Y", 30.0, 0.0, 463.0, 1.0, 1.0));
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting backgroundColor = (ColorSetting) this.m28(new ColorSetting("BackgroundColor", 0xFF222222));
    private final ColorSetting pressedColor = (ColorSetting) this.m28(new ColorSetting("PressedColor", 0xFF5555FF));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final BooleanSetting showWASD = (BooleanSetting) this.m28(new BooleanSetting("ShowWASD", true));
    private final BooleanSetting showMouse = (BooleanSetting) this.m28(new BooleanSetting("ShowMouse", true));
    private final BooleanSetting showSpace = (BooleanSetting) this.m28(new BooleanSetting("ShowSpace", true));
    private final TextSetting labelW = (TextSetting) this.m28(new TextSetting("KeyW", "W", "W key label"));
    private final TextSetting labelA = (TextSetting) this.m28(new TextSetting("KeyA", "A", "A key label"));
    private final TextSetting labelS = (TextSetting) this.m28(new TextSetting("KeyS", "S", "S key label"));
    private final TextSetting labelD = (TextSetting) this.m28(new TextSetting("KeyD", "D", "D key label"));
    private final TextSetting labelLMB = (TextSetting) this.m28(new TextSetting("KeyLMB", "LMB", "Left mouse label"));
    private final TextSetting labelRMB = (TextSetting) this.m28(new TextSetting("KeyRMB", "RMB", "Right mouse label"));
    private final TextSetting labelSpace = (TextSetting) this.m28(new TextSetting("KeySpace", "SPACE", "Space key label"));

    public KeystrokesHud() {
        super("Keystrokes", "Shows pressed keys.", Category.HUD);
        this.setFlag3(true);
        this.setChinese("按键显示");
        this.setChineseDescription("显示 WASD、鼠标和空格键的按下状态");
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
        return Math.round(58 * scale.getFloat());
    }

    @Override
    public int getHeight() {
        return Math.round(78 * scale.getFloat());
    }

    @Override
    public void setPosition(int x, int y) {
        this.x.setInt(x);
        this.y.setInt(y);
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        if (MC.getMc().player == null || MC.getMc().level == null) return;

        int posX = getX();
        int posY = getY();
        float s = scale.getFloat();
        int keySize = (int) (18 * s);
        int spacing = (int) (2 * s);
        int mouseWidth = (int) (28 * s);
        int spaceWidth = (int) (58 * s);
        int spaceHeight = (int) (12 * s);

        context.pose().pushMatrix();
        context.pose().translate(posX, posY);

        int currentY = 0;

        if (showWASD.getValue()) {
            int wX = keySize + spacing;
            drawKey(context, labelW.getValue(), isKeyDown(GLFW.GLFW_KEY_W), wX, currentY, keySize, keySize);
            currentY += keySize + spacing;

            drawKey(context, labelA.getValue(), isKeyDown(GLFW.GLFW_KEY_A), 0, currentY, keySize, keySize);
            drawKey(context, labelS.getValue(), isKeyDown(GLFW.GLFW_KEY_S), wX, currentY, keySize, keySize);
            drawKey(context, labelD.getValue(), isKeyDown(GLFW.GLFW_KEY_D), wX * 2, currentY, keySize, keySize);
            currentY += keySize + spacing;
        }

        if (showMouse.getValue()) {
            drawKey(context, labelLMB.getValue(), isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT), 0, currentY, mouseWidth, keySize);
            drawKey(context, labelRMB.getValue(), isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT), mouseWidth + spacing, currentY, mouseWidth, keySize);
            currentY += keySize + spacing;
        }

        if (showSpace.getValue()) {
            drawKey(context, labelSpace.getValue(), isKeyDown(GLFW.GLFW_KEY_SPACE), 0, currentY, spaceWidth, spaceHeight);
        }

        context.pose().popMatrix();
    }

    private void drawKey(GuiGraphics context, String text, boolean pressed, int x, int y, int width, int height) {
        int color = pressed ? pressedColor.getColor() : backgroundColor.getColor();
        context.fill(x, y, x + width, y + height, color);
        context.drawString(MC.getMc().font, text, x + width / 2 - MC.getMc().font.width(text) / 2, y + height / 2 - MC.getMc().font.lineHeight / 2, shadow.getValue() ? 0xFF000000 : 0xFFFFFFFF, shadow.getValue());
    }

    private boolean isKeyDown(int key) {
        long handle = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        if (handle == 0) return false;
        return org.lwjgl.glfw.GLFW.glfwGetKey(handle, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private boolean isMouseDown(int button) {
        long handle = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
        if (handle == 0) return false;
        return org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, button) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }
}

