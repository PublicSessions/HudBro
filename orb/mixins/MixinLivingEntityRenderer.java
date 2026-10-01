/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package dev.lumn.asm.mixins;

import dev.lumn.api.utils.Wrapper;
import dev.lumn.api.utils.math.MathUtil;
import dev.lumn.api.utils.render.ModelPlayer;
import dev.lumn.core.impl.RotationManager;
import dev.lumn.mod.modules.impl.client.ClientSetting;
import dev.lumn.mod.modules.impl.render.Chams;
import dev.lumn.mod.modules.impl.render.NoRender;
import dev.lumn.mod.modules.impl.render.ParrotPet;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={LivingEntityRenderer.class})
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique
    private LivingEntity lastEntity;
    @Unique
    private float originalYaw;
    @Unique
    private float originalHeadYaw;
    @Unique
    private float originalBodyYaw;
    @Unique
    private float originalPitch;
    @Unique
    private float originalPrevYaw;
    @Unique
    private float originalPrevHeadYaw;
    @Unique
    private float originalPrevBodyYaw;

    @Inject(method={"render*"}, at={@At(value="HEAD")}, cancellable = true)
    public void onRenderPre(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams module = Chams.INSTANCE;
        
        // 玩家自定义渲染
        if (livingEntity instanceof PlayerEntity && module.isOn() && module.Players.getValue() && module.playerCustom.getValue()) {
            ci.cancel();
            
            PlayerEntity player = (PlayerEntity) livingEntity;
            
            // 创建 ModelPlayer 实例
            ModelPlayer modelPlayer = new ModelPlayer(player);
            
            // 设置动画角度
            modelPlayer.animateModel(player, player.limbAnimator.getPos(), player.limbAnimator.getSpeed(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));
            modelPlayer.setAngles(player, player.limbAnimator.getPos(), player.limbAnimator.getSpeed(), player.age, player.headYaw - player.bodyYaw, player.getPitch());
            
            // 应用缩放
            float scale = module.playerScale.getValueFloat();
            matrixStack.push();
            matrixStack.scale(scale, scale, scale);
            
            // 渲染填充和线条
            modelPlayer.render(matrixStack, module.playerFill, module.playerLine);
            
            matrixStack.pop();
            
            return;
        }
        
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            this.originalYaw = livingEntity.getYaw();
            this.originalHeadYaw = ((LivingEntity)livingEntity).headYaw;
            this.originalBodyYaw = ((LivingEntity)livingEntity).bodyYaw;
            this.originalPitch = livingEntity.getPitch();
            this.originalPrevYaw = ((LivingEntity)livingEntity).prevYaw;
            this.originalPrevHeadYaw = ((LivingEntity)livingEntity).prevHeadYaw;
            this.originalPrevBodyYaw = ((LivingEntity)livingEntity).prevBodyYaw;
            livingEntity.setYaw(RotationManager.getRenderYawOffset());
            ((LivingEntity)livingEntity).headYaw = RotationManager.getRotationYawHead();
            ((LivingEntity)livingEntity).bodyYaw = RotationManager.getRenderYawOffset();
            livingEntity.setPitch(RotationManager.getRenderPitch());
            ((LivingEntity)livingEntity).prevYaw = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).prevHeadYaw = RotationManager.getPrevRotationYawHead();
            ((LivingEntity)livingEntity).prevBodyYaw = RotationManager.getPrevRenderYawOffset();
            ((LivingEntity)livingEntity).prevPitch = RotationManager.getPrevRenderPitch();
        }
        this.lastEntity = livingEntity;
    }

    @Inject(method={"render*"}, at={@At(value="TAIL")})
    public void onRenderPost(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null && livingEntity == MinecraftClient.getInstance().player && ClientSetting.INSTANCE.rotations.getValue()) {
            livingEntity.setYaw(this.originalYaw);
            ((LivingEntity)livingEntity).headYaw = this.originalHeadYaw;
            ((LivingEntity)livingEntity).bodyYaw = this.originalBodyYaw;
            livingEntity.setPitch(this.originalPitch);
            ((LivingEntity)livingEntity).prevYaw = this.originalPrevYaw;
            ((LivingEntity)livingEntity).prevHeadYaw = this.originalPrevHeadYaw;
            ((LivingEntity)livingEntity).prevBodyYaw = this.originalPrevBodyYaw;
            ((LivingEntity)livingEntity).prevPitch = this.originalPitch;
        }
    }

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", shift = At.Shift.AFTER))
    private void onModelRender(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (livingEntity instanceof PlayerEntity && ParrotPet.INSTANCE != null && ParrotPet.INSTANCE.isOn()) {
            ParrotPet.INSTANCE.renderInPlayerContext(matrixStack, vertexConsumerProvider, i, (PlayerEntity) livingEntity, f, g, livingEntity.getYaw(), livingEntity.getPitch());
        }
    }

    @ModifyArgs(method={"render*"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderHook(Args args) {
        PlayerEntity pl;
        LivingEntity livingEntity;
        float alpha = -1.0f;
        
        // Chams 统一透明度控制（将百分比转换为0.0-1.0）
        if (Chams.INSTANCE.isOn() && Chams.INSTANCE.throughWall.getValue()) {
            // 检查是否为启用的实体类型
            boolean shouldApply = false;
            if (this.lastEntity instanceof PlayerEntity && Chams.INSTANCE.Players.getValue()) {
                shouldApply = true;
            } else if (this.lastEntity instanceof MobEntity && Chams.INSTANCE.Mobs.getValue()) {
                shouldApply = true;
            } else if (this.lastEntity instanceof AnimalEntity && Chams.INSTANCE.Animals.getValue()) {
                shouldApply = true;
            } else if ((this.lastEntity instanceof VillagerEntity || this.lastEntity instanceof WanderingTraderEntity) && Chams.INSTANCE.Villagers.getValue()) {
                shouldApply = true;
            }
            
            if (shouldApply) {
                float vanillaAlpha = Chams.INSTANCE.vanillaAlpha.getValueInt() / 100.0f;
                if (vanillaAlpha < 1.0f) {
                    alpha = vanillaAlpha;
                }
            }
        }
        
        // NoRender antiPlayerCollision 透明度
        if (alpha == -1.0f && NoRender.INSTANCE.isOn() && NoRender.INSTANCE.antiPlayerCollision.getValue() && this.lastEntity != Wrapper.mc.player && (livingEntity = this.lastEntity) instanceof PlayerEntity && !(pl = (PlayerEntity)livingEntity).isInvisible()) {
            alpha = MathUtil.clamp((float)(Wrapper.mc.player.squaredDistanceTo(this.lastEntity.getPos()) / 3.0) + 0.2f, 0.0f, 1.0f);
        }
        
        if (alpha != -1.0f) {
            args.set(4, (Object)this.applyOpacity(0x26FFFFFF, alpha));
        }
    }

    @Unique
    int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity)).getRGB();
    }
}

