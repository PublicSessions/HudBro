package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: cancels selected particle types before they are created.
 */
@Mixin(net.minecraft.client.particle.ParticleEngine.class)
public class MixinParticleEngineNoRender {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void hudbro$cancelParticle(ParticleOptions options, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfoReturnable<Particle> cir) {
        NoRender module = NoRender.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        ParticleType<?> type = options.getType();
        if ((module.isElderGuardian() && type == ParticleTypes.ELDER_GUARDIAN)
                || (module.isExplosions() && (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER))
                || (module.isCampFire() && (type == ParticleTypes.CAMPFIRE_COSY_SMOKE || type == ParticleTypes.CAMPFIRE_SIGNAL_SMOKE))
                || (module.isFireworks() && type == ParticleTypes.FIREWORK)
                || (module.isEffect() && (type == ParticleTypes.EFFECT || type == ParticleTypes.INSTANT_EFFECT
                        || type == ParticleTypes.ENTITY_EFFECT || type == ParticleTypes.WITCH))) {
            cir.setReturnValue(null);
        }
    }
}
