package com.ciallo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import com.ciallo.module.hud.AbstractHudModule;
import com.ciallo.module.ModuleManager;
import com.ciallo.gui.HudSettingsScreen;
import com.ciallo.HudBro;

import java.util.concurrent.CompletableFuture;

public class CommandManager {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hudbro")
                .then(ClientCommandManager.argument("module", StringArgumentType.string())
                    .suggests(HudCommandSuggestionProvider.INSTANCE)
                    .executes(context -> {
                        String moduleName = StringArgumentType.getString(context, "module");
                        var module = ModuleManager.INSTANCE.getModuleByName(moduleName);
                        if (module == null) {
                            sendFeedback("Module not found: " + moduleName);
                            return 0;
                        }
                        module.toggle();
                        sendFeedback(module.getName() + " toggled: " + (module.isEnabled() ? "ON" : "OFF"));
                        return 1;
                    })
                    .then(ClientCommandManager.literal("setting")
                            .executes(context -> {
                                String moduleName = StringArgumentType.getString(context, "module");
                                var module = ModuleManager.INSTANCE.getModuleByName(moduleName);
                                if (module == null) {
                                    sendFeedback("Module not found: " + moduleName);
                                    return 0;
                                }
                                if (module instanceof AbstractHudModule hud) {
                                    HudBro.openSettingsScreen(hud);
                                    sendFeedback("Opened settings for " + hud.getName());
                                } else {
                                    sendFeedback(module.getName() + " is not a HUD module");
                                }
                                return 1;
                            })
                    )
                )
            );
        });
    }

    private static void sendFeedback(String message) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal(message), false);
        }
    }

    private static class HudCommandSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        public static final HudCommandSuggestionProvider INSTANCE = new HudCommandSuggestionProvider();

        private HudCommandSuggestionProvider() {}

        @Override
        public CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            for (AbstractHudModule hud : ModuleManager.INSTANCE.getHudModules()) {
                builder.suggest(hud.getName());
            }
            return builder.buildFuture();
        }
    }
}

