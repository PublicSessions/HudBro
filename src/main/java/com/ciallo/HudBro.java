package com.ciallo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ciallo.command.CommandManager;
import com.ciallo.config.HudConfig;
import com.ciallo.gui.HudDragManager;
import com.ciallo.gui.HudSettingsScreen;
import com.ciallo.module.ModuleManager;
import com.ciallo.module.client.HudEditor;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.module.hud.ArmorHud;
import com.ciallo.module.hud.BrandHud;
import com.ciallo.module.hud.CoordsHud;
import com.ciallo.module.hud.FPS;
import com.ciallo.module.hud.IPHud;
import com.ciallo.module.hud.InventoryViewer;
import com.ciallo.module.hud.KeystrokesHud;
import com.ciallo.module.hud.Ping;
import com.ciallo.module.hud.PlayerModel;
import com.ciallo.module.hud.PotionEffectsHud;
import com.ciallo.module.hud.SpeedHud;
import com.ciallo.module.hud.TimeHud;
import com.ciallo.module.hud.TPSHud;
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

        HudConfig.load();

        CommandManager.registerCommands();

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

