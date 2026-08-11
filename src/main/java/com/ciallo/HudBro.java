package com.ciallo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ciallo.command.CommandManager;
import com.ciallo.config.HudConfig;
import com.ciallo.gui.HudDragManager;
import com.ciallo.gui.HudSettingsScreen;
import com.ciallo.module.ModuleManager;
import com.ciallo.module.client.HudEditor;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.util.MC;
import com.ciallo.module.hud.ArmorHud;
import com.ciallo.module.hud.BrandHud;
import com.ciallo.module.hud.CoordsHud;
import com.ciallo.module.hud.FPS;
import com.ciallo.module.hud.IPHud;
import com.ciallo.module.hud.InventoryViewer;
import com.ciallo.module.hud.KeystrokesHud;
import com.ciallo.module.hud.ComboHud;
import com.ciallo.module.hud.CpsHud;
import com.ciallo.module.hud.DamageHud;
import com.ciallo.module.hud.InGameTime;
import com.ciallo.module.hud.MovementHud;
import com.ciallo.module.hud.Ping;
import com.ciallo.module.hud.PlayerModel;
import com.ciallo.module.hud.PotionEffectsHud;
import com.ciallo.module.hud.ReachHud;
import com.ciallo.module.hud.SpeedHud;
import com.ciallo.module.hud.TimeHud;
import com.ciallo.module.hud.TPSHud;
import com.ciallo.module.hud.TntHud;
import com.ciallo.module.hud.TotemHud;

public class HudBro implements ClientModInitializer {
    public static final String MOD_ID = "hudbro";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static AbstractHudModule pendingSettingsModule = null;

    public static void openSettingsScreen(AbstractHudModule hud) {
        pendingSettingsModule = hud;
    }

    @Override
    public void onInitializeClient() {
        try {
            Class.forName("net.minecraft.client.gui.screens.Screen");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Failed to pre-load Screen class", e);
        }

        ModuleManager manager = ModuleManager.INSTANCE;

        manager.register(new HudEditor());
        manager.register(new TotemHud());
        manager.register(new CoordsHud());
        manager.register(new ArmorHud());
        manager.register(new FPS());
        manager.register(new InventoryViewer());
        manager.register(new Ping());
        manager.register(new PlayerModel());
        manager.register(new TPSHud());
        manager.register(new IPHud());
        manager.register(new TimeHud());
        manager.register(new SpeedHud());
        manager.register(new BrandHud());
        manager.register(new KeystrokesHud());
        manager.register(new PotionEffectsHud());
        manager.register(new MovementHud());
        manager.register(new InGameTime());
        manager.register(new CpsHud());
        manager.register(new DamageHud());
        manager.register(new ComboHud());
        manager.register(new ReachHud());
        manager.register(new TntHud());

        HudConfig.load();

        CommandManager.registerCommands();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player == MC.getMc().player && world.isClientSide()) {
                ReachHud.onAttack(player, entity);
                if (entity != null) {
                    ComboHud.onHit();
                }
            }
            return InteractionResult.PASS;
        });

        HudRenderCallback.EVENT.register((context, deltaTracker) -> {
            if (pendingSettingsModule != null) {
                Minecraft.getInstance().setScreen(new HudSettingsScreen(pendingSettingsModule));
                pendingSettingsModule = null;
            }
            for (AbstractHudModule hud : manager.getHudModules()) {
                if (hud.isEnabled()) {
                    hud.render(context, deltaTracker.getGameTimeDeltaTicks());
                }
            }
            HudDragManager.getInstance().renderHoverHighlight(context);
            HudDragManager.getInstance().update();
            HudDragManager.getInstance().renderAlignmentLines(context);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(HudConfig::save));
        LOGGER.info("HudBro initialized!");
    }
}

