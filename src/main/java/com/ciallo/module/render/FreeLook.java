package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.EnumSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.KeyBinds;
import com.ciallo.util.MC;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Free look: turn the camera without turning the body, so the player keeps moving the way they were
 * facing. Ported from the {@code freelook} mod by Celibistrial.
 *
 * <p>How it works: {@link MixinEntityFreeLook} diverts the mouse rotation into a camera rotation stored
 * on the player and cancels {@code Entity#turn}, so the body never moves; {@link MixinCameraFreeLook}
 * then points the camera at that rotation. Free looking is driven by the key binds, not by the module
 * toggle: the module being enabled is only the master switch.</p>
 *
 * <p>Two modes, as in the original. In hold mode the keys are held down and the view springs back on
 * release; in toggle mode a key press starts free looking and pressing the same perspective again stops
 * it. Each of the three perspectives can have its own key, plus one that uses the configured
 * perspective.</p>
 */
public class FreeLook extends Module {
    public static FreeLook INSTANCE;

    /** Ticks the camera takes to slide between first and third person, about a quarter second. */
    private static final int TRANSITION_TICKS = 5;

    private final BooleanSetting holdMode = this.m28(new BooleanSetting("HoldMode", true));
    private final EnumSetting<CameraType> perspective =
            this.m28(new EnumSetting<>("Perspective", CameraType.THIRD_PERSON_BACK));
    private final NumberSetting maxHeadYaw =
            this.m28(new NumberSetting("MaxHeadYaw", 360.0, 5.0, 360.0, 5.0));
    private final TextSetting disabledServers =
            this.m28(new TextSetting("DisabledServers", "hypixel.net", "Comma separated"));

    /** Whether the free look rotation is currently being applied. Read by both mixins. */
    private static boolean active;
    /** True while connected to a server the free look is blocked on. */
    private static boolean blocked;

    private CameraType lastPerspective;
    private CameraType freeLookPerspective;
    private KeyMapping currentHoldKey;
    /** Whether the perspective has been swapped away from {@link #lastPerspective} and still needs restoring. */
    private boolean perspectiveOverridden;
    /** Whether the slide between the head and the detached camera is still running. */
    private boolean transitioning;
    private boolean restoringPerspective;
    private int transitionTicks;
    private float startFactor;

    public FreeLook() {
        super("FreeLook", "Look around without turning your body.", Category.RENDER);
        this.setChinese("自由视角");
        this.setChineseDescription("只转动视角，不改变身体朝向；可在模块设置里调整模式、默认视角与转头范围");
        // Free look is started by its key binds rather than from the HUD list, so it starts switched on:
        // the module toggle only acts as a master switch that stops a running free look.
        this.setFlag3(true);
        INSTANCE = this;
    }

    /** Whether the free look camera rotation is being applied right now. */
    public boolean isFreeLooking() {
        return active;
    }

    /** Whether free looking is switched off for the server that is connected to. */
    public static boolean isBlocked() {
        return blocked;
    }

    /** Widest the head may turn away from the direction it was facing, in degrees. */
    public float getMaxHeadYaw() {
        return (float) this.maxHeadYaw.getValue();
    }

    @Override
    public void onDisable() {
        // The module was switched off mid free look, so hand the camera back before losing the module.
        stopFreeLooking(MC.getMc());
    }

    /**
     * Called every client tick: drives the mode, the keys and the perspective animation.
     */
    public void onTick(Minecraft client) {
        if (!this.isEnabled() || blocked) {
            if (active) {
                stopFreeLooking(client);
            }
            return;
        }
        if (this.holdMode.getValue()) {
            this.tickHoldMode(client);
        } else {
            this.tickToggleMode(client);
        }
    }

    private void tickHoldMode(Minecraft client) {
        KeyMapping pressed = this.getPressedHoldKey();
        if (!active) {
            if (pressed != null) {
                startFreeLooking(client, perspectiveFor(pressed));
                this.currentHoldKey = pressed;
            }
        } else if (this.currentHoldKey != null && !this.currentHoldKey.isDown()) {
            stopFreeLooking(client);
        }
    }

    private void tickToggleMode(Minecraft client) {
        CameraType requested = this.consumeToggleKey();
        if (requested == null) {
            return;
        }
        if (!active) {
            startFreeLooking(client, requested);
        } else if (this.freeLookPerspective == requested) {
            // Pressing the same perspective again turns free looking off.
            stopFreeLooking(client);
        } else {
            setFreeLookPerspective(client, requested);
        }
    }

    private KeyMapping getPressedHoldKey() {
        if (KeyBinds.FREE_LOOK_FIRST_PERSON.isDown()) {
            return KeyBinds.FREE_LOOK_FIRST_PERSON;
        }
        if (KeyBinds.FREE_LOOK_SECOND_PERSON.isDown()) {
            return KeyBinds.FREE_LOOK_SECOND_PERSON;
        }
        if (KeyBinds.FREE_LOOK_THIRD_PERSON.isDown()) {
            return KeyBinds.FREE_LOOK_THIRD_PERSON;
        }
        if (KeyBinds.FREE_LOOK.isDown()) {
            return KeyBinds.FREE_LOOK;
        }
        return null;
    }

    private CameraType consumeToggleKey() {
        if (KeyBinds.FREE_LOOK_FIRST_PERSON.consumeClick()) {
            return CameraType.FIRST_PERSON;
        }
        if (KeyBinds.FREE_LOOK_SECOND_PERSON.consumeClick()) {
            return CameraType.THIRD_PERSON_FRONT;
        }
        if (KeyBinds.FREE_LOOK_THIRD_PERSON.consumeClick()) {
            return CameraType.THIRD_PERSON_BACK;
        }
        if (KeyBinds.FREE_LOOK.consumeClick()) {
            return this.perspective.getValue();
        }
        return null;
    }

    private CameraType perspectiveFor(KeyMapping key) {
        if (key == KeyBinds.FREE_LOOK_FIRST_PERSON) {
            return CameraType.FIRST_PERSON;
        }
        if (key == KeyBinds.FREE_LOOK_SECOND_PERSON) {
            return CameraType.THIRD_PERSON_FRONT;
        }
        if (key == KeyBinds.FREE_LOOK_THIRD_PERSON) {
            return CameraType.THIRD_PERSON_BACK;
        }
        return this.perspective.getValue();
    }

    private void startFreeLooking(Minecraft client, CameraType requested) {
        this.lastPerspective = client.options.getCameraType();
        this.freeLookPerspective = requested;
        this.perspectiveOverridden = false;
        active = true;
        if (client.player instanceof CameraOverriddenEntity camera) {
            // Start from the direction the body is facing, which also re-anchors the yaw limit.
            camera.freelook$setCameraYaw(client.player.getYRot());
            camera.freelook$setCameraPitch(client.player.getXRot());
        }
        applyPerspective(client, requested);
    }

    private void setFreeLookPerspective(Minecraft client, CameraType requested) {
        this.freeLookPerspective = requested;
        applyPerspective(client, requested);
    }

    /**
     * Switches to the requested perspective, sliding the camera out of or into the player's head when
     * that changes whether the camera is detached at all.
     *
     * <p>Only the head-to-detached move needs animating. Front and back third person sit at the same
     * distance, so swapping between them is instant, which is what the game itself does on F5.</p>
     */
    private void applyPerspective(Minecraft client, CameraType requested) {
        if (requested == this.lastPerspective && !this.perspectiveOverridden) {
            this.transitionTicks = 0;
            this.restoringPerspective = false;
            return;
        }
        boolean leavesHead = this.lastPerspective.isFirstPerson() != requested.isFirstPerson();
        this.perspectiveOverridden = true;
        if (leavesHead) {
            // Pick up wherever the previous slide had got to, so an interrupted slide does not jump.
            this.startFactor = this.transitionFactor();
            // A slide towards first person ends at the head, one towards third person at its distance.
            this.restoringPerspective = requested.isFirstPerson();
            this.transitionTicks = TRANSITION_TICKS;
        }
        client.options.setCameraType(requested);
    }

    private void stopFreeLooking(Minecraft client) {
        active = false;
        this.freeLookPerspective = null;
        this.currentHoldKey = null;
        if (this.lastPerspective == null || !this.perspectiveOverridden) {
            return;
        }
        if (this.lastPerspective.isFirstPerson() != client.options.getCameraType().isFirstPerson()) {
            // Slide back into the player's head first, then hand the perspective back, so the slide is
            // not cut off half way.
            this.startFactor = this.transitionFactor();
            this.restoringPerspective = true;
            this.transitionTicks = TRANSITION_TICKS;
            return;
        }
        // Same side of the head either way, so there is nothing to slide.
        this.perspectiveOverridden = false;
        client.options.setCameraType(this.lastPerspective);
    }

    /**
     * Called at the start of every client tick: advances the perspective animation and hands the
     * perspective back once the camera has returned to the player's head.
     */
    public void startTick() {
        if (this.transitionTicks <= 0) {
            return;
        }
        this.transitionTicks--;
        if (this.transitionTicks != 0) {
            return;
        }
        if (this.restoringPerspective) {
            // The camera is back at the player's head, so the perspective can be handed back.
            this.restoringPerspective = false;
            this.perspectiveOverridden = false;
            if (this.lastPerspective != null) {
                MC.getMc().options.setCameraType(this.lastPerspective);
            }
        }
    }

    /** How far along the perspective animation is, 0 at the head and 1 at the third person distance. */
    public float transitionFactor() {
        if (this.transitionTicks <= 0) {
            return 1.0f;
        }
        // The camera is only drawn while the tick counter is still above zero, so the last tick it is
        // drawn on has to be the end of the curve. Dividing by TRANSITION_TICKS instead would leave the
        // camera short of its target and snap on the frame the perspective is handed back.
        float progress = (float) (TRANSITION_TICKS - this.transitionTicks) / (TRANSITION_TICKS - 1);
        // Ease in and out, so the camera does not start and stop abruptly.
        float eased = progress * progress * (3.0f - 2.0f * progress);
        // Sliding out ends at the third person distance, sliding back in ends at the head.
        return Mth.lerp(eased, this.startFactor, this.restoringPerspective ? 0.0f : 1.0f);
    }

    /** Whether the perspective animation is still running. */
    public boolean isTransitioning() {
        return this.transitionTicks > 0;
    }

    /** Re-checks the blocked server list, called when joining a server. */
    public void onServerJoined() {
        ServerData server = MC.getMc().getCurrentServer();
        blocked = false;
        if (server == null || server.ip == null) {
            return;
        }
        String address = server.ip.toLowerCase(Locale.ROOT);
        for (String blockedServer : this.getDisabledServers()) {
            if (!blockedServer.isEmpty() && address.contains(blockedServer.toLowerCase(Locale.ROOT))) {
                blocked = true;
                return;
            }
        }
    }

    /** Clears the blocked flag, called when leaving a server. */
    public void onServerLeft() {
        blocked = false;
    }

    private List<String> getDisabledServers() {
        List<String> servers = new ArrayList<>();
        for (String entry : this.disabledServers.getValue().split(",")) {
            String trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                servers.add(trimmed);
            }
        }
        return servers;
    }
}