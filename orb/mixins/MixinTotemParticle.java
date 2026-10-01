/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.SpriteProvider
 *  net.minecraft.client.particle.TotemParticle
 *  net.minecraft.client.world.ClientWorld
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.lumn.asm.mixins;

import dev.lumn.lumn;
import dev.lumn.asm.accessors.IParticle;
import dev.lumn.api.events.impl.TotemParticleEvent;

import java.awt.Color;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TotemParticle.class})
public abstract class MixinTotemParticle {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void hookInit(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider, CallbackInfo ci) {
        TotemParticleEvent event = TotemParticleEvent.get(velocityX, velocityY, velocityZ);
        lumn.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ((IParticle) this).setVelocityX(event.velocityX);
            ((IParticle) this).setVelocityY(event.velocityY);
            ((IParticle) this).setVelocityZ(event.velocityZ);
            Color color = event.color;
            if (color != null) {
                ((IParticle) this).hookSetColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
                ((IParticle) this).hookSetAlpha((float)color.getAlpha() / 255.0f);
            }
        }
    }
}

