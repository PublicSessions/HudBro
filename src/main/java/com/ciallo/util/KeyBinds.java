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

    /**
     * Starts FreeLook using the perspective configured in the module settings. Default: unbound, the
     * player picks a key in Options -> Controls.
     */
    public static final KeyMapping FREE_LOOK = unbound("key.hudbro.free_look");

    /** Starts FreeLook directly in first person. Default: unbound. */
    public static final KeyMapping FREE_LOOK_FIRST_PERSON = unbound("key.hudbro.free_look_first");

    /** Starts FreeLook directly in the front facing third person view. Default: unbound. */
    public static final KeyMapping FREE_LOOK_SECOND_PERSON = unbound("key.hudbro.free_look_second");

    /** Starts FreeLook directly in the back facing third person view. Default: unbound. */
    public static final KeyMapping FREE_LOOK_THIRD_PERSON = unbound("key.hudbro.free_look_third");

    private KeyBinds() {
    }

    /** Registers a key that starts out unbound. */
    private static KeyMapping unbound(String name) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                name,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));
    }

    /**
     * Must be called from the client initializer: key bindings have to be registered before the
     * game options are created, so the class cannot be initialised lazily on first key press.
     */
    public static void init() {
    }
}
