package com.ciallo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.ciallo.config.GlobalConfig;
import com.ciallo.config.HudConfig;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.module.ModuleManager;
import com.ciallo.util.MC;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact HUD editor: a small translucent list of every HUD module plus the live HUD elements.
 *
 * <ul>
 *   <li>Left click the check box to enable / disable a HUD (disabled ones can still be moved).</li>
 *   <li>Left click and drag a list row to move that HUD (alignment snapping to the screen centre).</li>
 *   <li>Left drag a HUD element on the canvas directly.</li>
 *   <li>Right click a list row (or a HUD on the canvas) to open its settings; ESC there comes back.</li>
 *   <li>Drag the panel title bar to move the panel itself; mouse wheel scrolls the list.</li>
 *   <li>Keyboard: UP / DOWN select, ENTER or SPACE toggles, S or RIGHT opens settings,
 *       SHIFT + arrows nudge the selected HUD by one pixel.</li>
 * </ul>
 */
public class HudEditorScreen extends Screen {
    private static final int PANEL_WIDTH = 150;
    private static final int ROW_HEIGHT = 14;
    private static final int HEADER_HEIGHT = 16;
    private static final int FOOTER_HEIGHT = 28;
    private static final int CHECKBOX_SIZE = 9;
    private static final int SNAP_DISTANCE = 8;
    private static final int MAX_ROWS = 16;

    private int panelOffsetX = 0;
    private int panelOffsetY = 0;
    private boolean draggingPanel;
    private int panelDragStartX;
    private int panelDragStartY;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int selectedIndex = -1;

    private AbstractHudModule draggedModule;
    private boolean draggingFromList;
    private int dragOffsetX;
    private int dragOffsetY;
    private float dragMouseX;
    private float dragMouseY;

    private int mouseX;
    private int mouseY;

    public HudEditorScreen() {
        super(Component.literal("HUD Editor"));
    }

    public static boolean isActive() {
        return MC.getMc().screen instanceof HudEditorScreen;
    }

    private List<AbstractHudModule> hudList() {
        return new ArrayList<>(ModuleManager.INSTANCE.getHudModules());
    }

    private int panelX() {
        int screenWidth = MC.getMc().getWindow().getGuiScaledWidth();
        int x = 6 + panelOffsetX;
        if (x < 0) x = 0;
        if (x + PANEL_WIDTH > screenWidth) x = screenWidth - PANEL_WIDTH;
        return x;
    }

    private int panelY() {
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        int y = 6 + panelOffsetY;
        if (y < 0) y = 0;
        if (y + panelHeight(hudList().size()) > screenHeight) y = Math.max(0, screenHeight - panelHeight(hudList().size()));
        return y;
    }

    private int availableRows() {
        int screenHeight = MC.getMc().getWindow().getGuiScaledHeight();
        return Math.max(1, (screenHeight - 12 - HEADER_HEIGHT - FOOTER_HEIGHT) / ROW_HEIGHT);
    }

    private int visibleRows(int rowCount) {
        return Math.max(1, Math.min(rowCount, Math.min(MAX_ROWS, availableRows())));
    }

    private int panelHeight(int rowCount) {
        return HEADER_HEIGHT + visibleRows(rowCount) * ROW_HEIGHT + FOOTER_HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Translucent backdrop so the world stays visible while editing.
        context.fill(0, 0, screenWidth, screenHeight, 0x30101010);
        context.fill(centerX - 1, 0, centerX + 1, screenHeight, 0x30FFFFFF);
        context.fill(0, centerY - 1, screenWidth, centerY + 1, 0x30FFFFFF);

        renderHudElements(context, delta);
        renderPanel(context, mouseX, mouseY);

        if (draggedModule != null && (draggedModule.isEnabled() || GlobalConfig.editorShowDisabled.getValue())) {
            int x = draggedModule.getX();
            int y = draggedModule.getY();
            context.fill(x, y, x + draggedModule.getWidth(), y + draggedModule.getHeight(), 0x40FFFFFF);
            context.drawString(MC.getMc().font, draggedModule.getName(), x, y - 10, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Blur may only be requested once per frame, so it lives here and not in render().
        if (UiTheme.blurEnabled()) {
            this.renderBlurredBackground(context);
        }
    }

    private void renderHudElements(GuiGraphics context, float delta) {
        boolean showDisabled = GlobalConfig.editorShowDisabled.getValue();
        for (AbstractHudModule hud : hudList()) {
            hud.setEditorMode(true);
            if (!hud.isEnabled() && !showDisabled) {
                // Disabled HUDs are hidden by default; use the panel button to reveal them.
                continue;
            }
            try {
                hud.render(context, delta);
            } catch (Exception e) {
                // A HUD that cannot render outside a world must not take the whole editor down.
                com.ciallo.HudBro.LOGGER.debug("Failed to render {} in the editor: {}", hud.getName(), e.toString());
            }
            if (!hud.isEnabled()) {
                // Disabled HUDs are drawn faded so they can still be positioned.
                int x = hud.getX();
                int y = hud.getY();
                int w = Math.max(hud.getWidth(), 4);
                int h = Math.max(hud.getHeight(), 4);
                context.fill(x, y, x + w, y + h, 0x60000000);
                context.drawString(MC.getMc().font, hud.getName(), x, y + h + 2, 0xFFFF5555, true);
            }
        }

        AbstractHudModule hovered = hudAt(mouseX, mouseY);
        if (hovered != null) {
            context.fill(hovered.getX(), hovered.getY(), hovered.getX() + hovered.getWidth(), hovered.getY() + hovered.getHeight(), 0x30FFFFFF);
        }
    }

    private void renderPanel(GuiGraphics context, int mouseX, int mouseY) {
        List<AbstractHudModule> huds = hudList();
        int rows = visibleRows(huds.size());
        int height = panelHeight(huds.size());
        int panelX = panelX();
        int panelY = panelY();

        maxScroll = Math.max(0, huds.size() - rows);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + height, UiTheme.panel(225, 1.0f));
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 1, UiTheme.accent(0.7f));
        context.fill(panelX, panelY + height - 1, panelX + PANEL_WIDTH, panelY + height, UiTheme.accent(0.7f));
        context.fill(panelX, panelY, panelX + 1, panelY + height, UiTheme.accent(0.7f));
        context.fill(panelX + PANEL_WIDTH - 1, panelY, panelX + PANEL_WIDTH, panelY + height, UiTheme.accent(0.7f));
        context.fill(panelX + 1, panelY + 1, panelX + PANEL_WIDTH - 1, panelY + HEADER_HEIGHT, UiTheme.panel(255, 0.8f));
        context.drawString(MC.getMc().font, "HUD Editor", panelX + 4, panelY + 4, 0xFFFFFFFF, true);

        int listTop = panelY + HEADER_HEIGHT;
        for (int i = 0; i < rows; i++) {
            int index = i + scrollOffset;
            if (index >= huds.size()) {
                break;
            }
            AbstractHudModule hud = huds.get(index);
            int rowY = listTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= panelX + 1 && mouseX <= panelX + PANEL_WIDTH - 1
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;

            if (index == selectedIndex) {
                context.fill(panelX + 1, rowY, panelX + PANEL_WIDTH - 1, rowY + ROW_HEIGHT, UiTheme.accent(0.35f));
            } else if (hovered) {
                context.fill(panelX + 1, rowY, panelX + PANEL_WIDTH - 1, rowY + ROW_HEIGHT, 0x40FFFFFF);
            }

            int boxX = panelX + 5;
            int boxY = rowY + 3;
            boolean on = hud.isEnabled();
            context.fill(boxX, boxY, boxX + CHECKBOX_SIZE, boxY + CHECKBOX_SIZE, on ? 0xFF00C853 : 0x40FFFFFF);
            context.fill(boxX, boxY, boxX + CHECKBOX_SIZE, boxY + 1, 0xFF000000);
            context.fill(boxX, boxY + CHECKBOX_SIZE - 1, boxX + CHECKBOX_SIZE, boxY + CHECKBOX_SIZE, 0xFF000000);
            context.fill(boxX, boxY, boxX + 1, boxY + CHECKBOX_SIZE, 0xFF000000);
            context.fill(boxX + CHECKBOX_SIZE - 1, boxY, boxX + CHECKBOX_SIZE, boxY + CHECKBOX_SIZE, 0xFF000000);

            String name = hud.getName();
            int maxNameWidth = PANEL_WIDTH - (CHECKBOX_SIZE + 12) - 6;
            while (MC.getMc().font.width(name) > maxNameWidth && name.length() > 1) {
                name = name.substring(0, name.length() - 1);
            }
            context.drawString(MC.getMc().font, name, boxX + CHECKBOX_SIZE + 4, rowY + 3, on ? 0xFFFFFFFF : 0xFF9E9E9E, true);
        }

        if (maxScroll > 0) {
            int trackX = panelX + PANEL_WIDTH - 4;
            context.fill(trackX, listTop, trackX + 2, listTop + rows * ROW_HEIGHT, 0x60FFFFFF);
            int barHeight = Math.max(8, rows * ROW_HEIGHT * rows / huds.size());
            int barY = listTop + (rows * ROW_HEIGHT - barHeight) * scrollOffset / maxScroll;
            context.fill(trackX, barY, trackX + 2, barY + barHeight, UiTheme.accent(0.75f));
        }

        context.drawString(MC.getMc().font, "LMB=move RMB=settings", panelX + 4, panelY + height - 11, 0xFFBDBDBD, true);
        renderDisabledToggle(context, panelX, panelY + height - FOOTER_HEIGHT + 2, mouseX, mouseY);
    }

    private boolean showDisabledButtonHovered(int mouseX, int mouseY) {
        return mouseX >= panelX() + 4 && mouseX <= panelX() + PANEL_WIDTH - 4
                && mouseY >= panelY() + panelHeight(hudList().size()) - FOOTER_HEIGHT + 2
                && mouseY <= panelY() + panelHeight(hudList().size()) - FOOTER_HEIGHT + 14;
    }

    private void renderDisabledToggle(GuiGraphics context, int panelX, int buttonY, int mouseX, int mouseY) {
        boolean on = GlobalConfig.editorShowDisabled.getValue();
        boolean hovered = showDisabledButtonHovered(mouseX, mouseY);
        String label = on ? "Show disabled: ON" : "Show disabled: OFF";
        context.fill(panelX + 4, buttonY, panelX + PANEL_WIDTH - 4, buttonY + 12, UiTheme.panel(hovered ? 255 : 200, 1.0f));
        int border = hovered ? UiTheme.accent(1.0f) : UiTheme.accent(0.6f);
        context.fill(panelX + 4, buttonY, panelX + PANEL_WIDTH - 4, buttonY + 1, border);
        context.fill(panelX + 4, buttonY + 11, panelX + PANEL_WIDTH - 4, buttonY + 12, border);
        context.drawString(MC.getMc().font, label, panelX + 8, buttonY + 3, on ? 0xFF7CFF7C : 0xFFCCCCCC, true);
    }

    /** Row index under the cursor, or -1. */
    private int rowIndexAt(double mouseX, double mouseY) {
        List<AbstractHudModule> huds = hudList();
        int rows = visibleRows(huds.size());
        int panelX = panelX();
        int listTop = panelY() + HEADER_HEIGHT;
        if (mouseX < panelX || mouseX > panelX + PANEL_WIDTH) {
            return -1;
        }
        for (int i = 0; i < rows; i++) {
            int rowY = listTop + i * ROW_HEIGHT;
            if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                int index = i + scrollOffset;
                return index < huds.size() ? index : -1;
            }
        }
        return -1;
    }

    private boolean overHeader(double mouseX, double mouseY) {
        int panelX = panelX();
        int panelY = panelY();
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + HEADER_HEIGHT;
    }

    private boolean overPanel(double mouseX, double mouseY) {
        int panelX = panelX();
        int panelY = panelY();
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH && mouseY >= panelY && mouseY <= panelY + panelHeight(hudList().size());
    }

    private AbstractHudModule hudAt(double mouseX, double mouseY) {
        List<AbstractHudModule> huds = hudList();
        boolean showDisabled = GlobalConfig.editorShowDisabled.getValue();
        for (int i = huds.size() - 1; i >= 0; i--) {
            AbstractHudModule hud = huds.get(i);
            if (!hud.isEnabled() && !showDisabled) {
                // Hidden HUDs must not be pickable on the canvas.
                continue;
            }
            int x = hud.getX();
            int y = hud.getY();
            int w = Math.max(hud.getWidth(), 4);
            int h = Math.max(hud.getHeight(), 4);
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                return hud;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        if (button == 0 && overHeader(mx, my)) {
            draggingPanel = true;
            panelDragStartX = (int) mx;
            panelDragStartY = (int) my;
            return true;
        }

        if (button == 0 && showDisabledButtonHovered((int) mx, (int) my)) {
            GlobalConfig.editorShowDisabled.setValue(!GlobalConfig.editorShowDisabled.getValue());
            GlobalConfig.save();
            return true;
        }

        int rowIndex = rowIndexAt(mx, my);
        if (rowIndex >= 0) {
            AbstractHudModule hud = hudList().get(rowIndex);
            selectedIndex = rowIndex;
            if (button == 0) {
                // Click on the check box toggles the module, anywhere else starts moving it.
                if (mx <= panelX() + 5 + CHECKBOX_SIZE + 3) {
                    hud.toggle();
                    HudConfig.save();
                    return true;
                }
                if (!hud.isEnabled() && !GlobalConfig.editorShowDisabled.getValue()) {
                    // Not shown on the canvas, so it cannot be dragged by the mouse either.
                    return true;
                }
                draggedModule = hud;
                draggingFromList = true;
                dragMouseX = (float) mx;
                dragMouseY = (float) my;
                return true;
            }
            if (button == 1) {
                Minecraft.getInstance().setScreen(new HudSettingsScreen(hud, this));
                return true;
            }
            return true;
        }

        if (button == 0 && !overPanel(mx, my)) {
            AbstractHudModule hud = hudAt(mx, my);
            if (hud != null) {
                draggedModule = hud;
                draggingFromList = false;
                dragOffsetX = (int) mx - hud.getX();
                dragOffsetY = (int) my - hud.getY();
                return true;
            }
        }
        // Right clicking an element on the canvas opens its settings too.
        if (button == 1 && !overPanel(mx, my)) {
            AbstractHudModule hud = hudAt(mx, my);
            if (hud != null) {
                Minecraft.getInstance().setScreen(new HudSettingsScreen(hud, this));
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (draggingPanel) {
            panelOffsetX += (int) event.x() - panelDragStartX;
            panelOffsetY += (int) event.y() - panelDragStartY;
            panelDragStartX = (int) event.x();
            panelDragStartY = (int) event.y();
            return true;
        }
        if (draggedModule != null) {
            int newX;
            int newY;
            if (draggingFromList) {
                newX = draggedModule.getX() + Math.round((float) event.x() - dragMouseX);
                newY = draggedModule.getY() + Math.round((float) event.y() - dragMouseY);
                dragMouseX = (float) event.x();
                dragMouseY = (float) event.y();
            } else {
                newX = (int) event.x() - dragOffsetX;
                newY = (int) event.y() - dragOffsetY;
            }
            moveDragged(newX, newY);
            return true;
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    private void moveDragged(int newX, int newY) {
        int w = draggedModule.getWidth();
        int h = draggedModule.getHeight();
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        if (Math.abs(newX + w / 2 - centerX) < SNAP_DISTANCE) {
            newX = centerX - w / 2;
        }
        if (Math.abs(newY + h / 2 - centerY) < SNAP_DISTANCE) {
            newY = centerY - h / 2;
        }
        newX = Math.max(0, Math.min(screenWidth - w, newX));
        newY = Math.max(0, Math.min(screenHeight - h, newY));
        
        int maxX = screenWidth - w;
        int maxY = screenHeight - h;
        if (maxX < 0) maxX = 0;
        if (maxY < 0) maxY = 0;
        newX = Math.max(0, Math.min(maxX, newX));
        newY = Math.max(0, Math.min(maxY, newY));
        
        draggedModule.setPosition(newX, newY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingPanel) {
            draggingPanel = false;
            return true;
        }
        if (draggedModule != null) {
            draggedModule = null;
            draggingFromList = false;
            HudConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (overPanel(mouseX, mouseY) || maxScroll > 0) {
            scrollOffset -= (int) Math.signum(scrollY);
            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        for (AbstractHudModule hud : hudList()) {
            hud.setEditorMode(false);
        }
        HudConfig.save();
        super.removed();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        List<AbstractHudModule> huds = hudList();

        if (event.isEscape()) {
            MC.getMc().setScreen(null);
            return true;
        }

        // SHIFT + arrows nudge the selected HUD by one pixel.
        if (event.hasShiftDown() && selectedIndex >= 0 && selectedIndex < huds.size()
                && (event.isLeft() || event.isRight() || event.isUp() || event.isDown())) {
            AbstractHudModule hud = huds.get(selectedIndex);
            if (!hud.isEnabled() && !GlobalConfig.editorShowDisabled.getValue()) {
                return true;
            }
            int dx = event.isLeft() ? -1 : event.isRight() ? 1 : 0;
            int dy = event.isUp() ? -1 : event.isDown() ? 1 : 0;
            draggedModule = hud;
            moveDragged(hud.getX() + dx, hud.getY() + dy);
            draggedModule = null;
            HudConfig.save();
            return true;
        }

        if (event.isDown() || event.isUp()) {
            if (!huds.isEmpty()) {
                int step = event.isDown() ? 1 : -1;
                selectedIndex = selectedIndex < 0 ? 0 : (selectedIndex + step + huds.size()) % huds.size();
                // Keep the selection inside the visible window.
                int rows = visibleRows(huds.size());
                if (selectedIndex < scrollOffset) {
                    scrollOffset = selectedIndex;
                } else if (selectedIndex >= scrollOffset + rows) {
                    scrollOffset = selectedIndex - rows + 1;
                }
            }
            return true;
        }

        if (selectedIndex >= 0 && selectedIndex < huds.size()) {
            AbstractHudModule hud = huds.get(selectedIndex);

            if (event.isConfirmation() || event.input() == 32) { // enter / space
                hud.toggle();
                HudConfig.save();
                return true;
            }
            if (event.input() == 83) { // S
                Minecraft.getInstance().setScreen(new HudSettingsScreen(hud, this));
                return true;
            }
        }
        return super.keyPressed(event);
    }
}
