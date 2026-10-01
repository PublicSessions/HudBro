package com.ciallo.mixin;

import net.minecraft.client.particle.TotemParticle;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TotemParticle: applies the configured velocity multipliers and colour to totem pop particles.
 *
 * <p>Implemented with argument modification plus a redirect on the vanilla {@code setColor} calls,
 * because {@code @Shadow} cannot resolve members declared in {@code TotemParticle}'s super classes.
 * The alpha channel of the configured colours is not applied (the particle alpha setter is
 * {@code protected} in {@code SingleQuadParticle}).</p>
 */
@Mixin(net.minecraft.client.particle.TotemParticle.class)
public abstract class MixinTotemParticle {

    private static com.ciallo.module.render.TotemParticle hudbro$module() {
        com.ciallo.module.render.TotemParticle module = com.ciallo.module.render.TotemParticle.INSTANCE;
        return module != null && module.isEnabled() ? module : null;
    }

    // Constructor arguments: (ClientLevel, x, y, z, velocityX, velocityY, velocityZ, SpriteSet)
    // Handlers that run before super() must be static.
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private static double hudbro$velocityX(double velocityX) {
        com.ciallo.module.render.TotemParticle module = hudbro$module();
        return module == null ? velocityX : velocityX * module.getVelocityXZ();
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 4)
    private static double hudbro$velocityY(double velocityY) {
        com.ciallo.module.render.TotemParticle module = hudbro$module();
        return module == null ? velocityY : velocityY * module.getVelocityY();
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 5)
    private static double hudbro$velocityZ(double velocityZ) {
        com.ciallo.module.render.TotemParticle module = hudbro$module();
        return module == null ? velocityZ : velocityZ * module.getVelocityXZ();
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/TotemParticle;setColor(FFF)V")
    )
    private static void hudbro$color(TotemParticle instance, float red, float green, float blue) {
        com.ciallo.module.render.TotemParticle module = hudbro$module();
        if (module == null) {
            instance.setColor(red, green, blue);
            return;
        }
        int color = module.getParticleColor();
        instance.setColor(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f);
    }
}
