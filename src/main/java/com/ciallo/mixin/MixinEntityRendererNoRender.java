package com.ciallo.mixin;

import com.ciallo.module.render.NoRender;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NoRender: removes the drop shadow of item entities.
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRendererNoRender {

    @Inject(
            method = "getShadowRadius(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private void hudbro$noItemShadow(EntityRenderState state, CallbackInfoReturnable<Float> cir) {
        NoRender module = NoRender.INSTANCE;
        if (module != null && module.isCastShadow() && state.entityType == EntityType.ITEM) {
            cir.setReturnValue(0.0f);
        }
    }
}
