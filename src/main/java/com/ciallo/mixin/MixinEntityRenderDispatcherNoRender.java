package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: stops selected entity types from being rendered at all.
 */
@Mixin(net.minecraft.client.renderer.entity.EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcherNoRender {

    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void hudbro$skipEntity(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        NoRender module = NoRender.INSTANCE;
        if (module == null || !module.isEnabled()) {
            return;
        }
        if ((module.isPotions() && entity instanceof AbstractThrownPotion)
                || (module.isXp() && entity instanceof ThrownExperienceBottle)
                || (module.isArrows() && entity instanceof AbstractArrow)
                || (module.isEggs() && entity instanceof ThrownEgg)
                || (module.isItems() && entity instanceof ItemEntity)) {
            cir.setReturnValue(false);
        }
    }
}
