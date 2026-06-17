package com.coles.recipeeditor.command;

import com.coles.recipeeditor.CREMod;
import com.coles.recipeeditor.kubejs.KubeJSScriptManager;
import com.coles.recipeeditor.recipe.CRERecipeEntry;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Set;

public class CRECommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("CRE")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        src.sendSuccess(() -> Component.literal("[CRE] Saving and reloading recipes..."), true);
                        KubeJSScriptManager.writeScripts();
                        RecipeStateManager.getInstance().saveToDisk();
                        MinecraftServer server = src.getServer();
                        server.reloadResources(server.getPackRepository().getSelectedIds()).thenRun(() -> {
                            src.sendSuccess(() -> Component.literal("[CRE] Reload complete! Recipe changes are now live."), true);
                        });
                        return 1;
                    })
                )
                .then(Commands.literal("list")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        RecipeStateManager state = RecipeStateManager.getInstance();
                        Set<String> disabled = state.getDisabledRecipes();
                        List<CRERecipeEntry> custom = state.getCustomRecipes();

                        src.sendSuccess(() -> Component.literal("§6[CRE] ===== Recipe List ====="), false);

                        if (disabled.isEmpty()) {
                            src.sendSuccess(() -> Component.literal("§7No disabled recipes."), false);
                        } else {
                            src.sendSuccess(() -> Component.literal("§c--- Disabled Recipes (" + disabled.size() + ") ---"), false);
                            for (String id : disabled) {
                                src.sendSuccess(() -> Component.literal("§c  - " + id), false);
                            }
                        }

                        if (custom.isEmpty()) {
                            src.sendSuccess(() -> Component.literal("§7No custom CRE recipes."), false);
                        } else {
                            src.sendSuccess(() -> Component.literal("§a--- Custom CRE Recipes (" + custom.size() + ") ---"), false);
                            for (CRERecipeEntry entry : custom) {
                                String status = entry.isEnabled() ? "§aENABLED" : "§cDISABLED";
                                src.sendSuccess(() -> Component.literal("  " + status + " §f" + entry.getId() + " §7(" + entry.getType() + ")"), false);
                            }
                        }

                        src.sendSuccess(() -> Component.literal("§6[CRE] ======================"), false);
                        return 1;
                    })
                )
                .then(Commands.literal("save")
                    .executes(ctx -> {
                        KubeJSScriptManager.writeScripts();
                        RecipeStateManager.getInstance().saveToDisk();
                        ctx.getSource().sendSuccess(() -> Component.literal("[CRE] Changes saved. Run /CRE reload to apply them."), true);
                        return 1;
                    })
                )
                .then(Commands.literal("help")
                    .executes(ctx -> {
                        CommandSourceStack src = ctx.getSource();
                        src.sendSuccess(() -> Component.literal("§6[CRE] Cole's Recipe Editor Commands:"), false);
                        src.sendSuccess(() -> Component.literal("§e/CRE reload §7- Save changes and reload all recipes"), false);
                        src.sendSuccess(() -> Component.literal("§e/CRE list §7- List all disabled and custom recipes"), false);
                        src.sendSuccess(() -> Component.literal("§e/CRE save §7- Save changes without reloading"), false);
                        src.sendSuccess(() -> Component.literal("§e/CRE help §7- Show this help message"), false);
                        src.sendSuccess(() -> Component.literal("§7Press F8 in-game to open the Recipe Editor GUI."), false);
                        return 1;
                    })
                )
        );
    }
}
