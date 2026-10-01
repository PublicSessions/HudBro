package com.ciallo.mixin;

import com.ciallo.module.render.FreeLook;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * FreeLook: while enabled the mouse rotation is fed to the module instead of turning the player.
 */
@Mixin(MouseHandler.class)
public class MixinMouseHandlerFreeLook {

    @Redirect(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void hudbro$freeLookTurn(LocalPlayer player, double yaw, double pitch) {
        FreeLook module = FreeLook.INSTANCE;
        if (module != null && module.isEnabled()) {
            module.onMouseDelta(yaw, pitch);
            return;
        }
        player.turn(yaw, pitch);
    }
}
