package com.ciallo.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Key bindings for HudBro. The key can be changed in Options -> Controls.
 */
public final class KeyBinds {
    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("hudbro", "main"));

    /** Opens the HudBro main screen (theme / scale / opacity / blur). Default: H. */
    public static final KeyMapping OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.hudbro.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    ));

    /** Opens the HUD editor (element list + drag + settings). Default: right shift. */
    public static final KeyMapping OPEN_EDITOR = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.hudbro.open_editor",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            CATEGORY
    ));

    private KeyBinds() {
    }

    /**
     * Must be called from the client initializer: key bindings have to be registered before the
     * game options are created, so the class cannot be initialised lazily on first key press.
     */
    public static void init() {
    }
}
