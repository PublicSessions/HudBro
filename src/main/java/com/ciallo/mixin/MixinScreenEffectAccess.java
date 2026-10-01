package com.ciallo.mixin;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor for the private static {@code ScreenEffectRenderer#getViewBlockingState} so the
 * NoRender block overlay toggle can fall back to the vanilla implementation.
 */
@Mixin(ScreenEffectRenderer.class)
public interface MixinScreenEffectAccess {

    @Invoker("getViewBlockingState")
    static BlockState hudbro$getViewBlockingState(Player player) {
        throw new AssertionError();
    }
}
