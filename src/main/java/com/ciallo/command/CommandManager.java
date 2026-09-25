package com.ciallo.command;

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
import com.ciallo.gui.HudEditorScreen;
import com.ciallo.gui.HudMainScreen;
import com.ciallo.HudBro;

import java.util.concurrent.CompletableFuture;

/**
 * HudBro commands:
 * <ul>
 *   <li>{@code /hudbro} - main screen</li>
 *   <li>{@code /hudbro editor} - HUD editor</li>
 *   <li>{@code /hudbro settings} - main screen</li>
 *   <li>{@code /hudbro <module>} - toggle a module (tab completable)</li>
 *   <li>{@code /hudbro <module> setting} - open that module's settings</li>
 * </ul>
 */
public class CommandManager {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hudbro")
                .executes(context -> {
                    Minecraft.getInstance().setScreen(new HudMainScreen());
                    return 1;
                })
                .then(ClientCommandManager.literal("editor")
                    .executes(context -> {
                        Minecraft.getInstance().setScreen(new HudEditorScreen());
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("settings")
                    .executes(context -> {
                        Minecraft.getInstance().setScreen(new HudMainScreen());
                        return 1;
                    })
                )
                .then(ClientCommandManager.argument("module", StringArgumentType.word())
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
                                    HudBro.openSettingsScreen(module);
                                    sendFeedback("Opened settings for " + module.getName());
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

    /** Suggests module names for the {@code /hudbro <module>} argument. */
    private static class HudCommandSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        public static final HudCommandSuggestionProvider INSTANCE = new HudCommandSuggestionProvider();

        private HudCommandSuggestionProvider() {
        }

        @Override
        public CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            String remaining = builder.getRemainingLowerCase();
            for (com.ciallo.module.Module module : ModuleManager.INSTANCE.getModules()) {
                String name = module.getName();
                String lower = name.toLowerCase();
                if (remaining.isEmpty() || lower.startsWith(remaining) || lower.contains(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        }
    }
}
