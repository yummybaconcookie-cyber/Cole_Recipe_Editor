package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import com.coles.recipeeditor.kubejs.KubeJSScriptManager;
import com.coles.recipeeditor.recipe.CRERecipeEntry;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CREMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CRENetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
            CRETogglePacket.TYPE,
            CRETogglePacket.STREAM_CODEC,
            CRENetworkHandler::handleToggleOnServer
        );

        registrar.playToServer(
            CREAddRecipePacket.TYPE,
            CREAddRecipePacket.STREAM_CODEC,
            CRENetworkHandler::handleAddRecipeOnServer
        );

        registrar.playToServer(
            CREDeleteRecipePacket.TYPE,
            CREDeleteRecipePacket.STREAM_CODEC,
            CRENetworkHandler::handleDeleteOnServer
        );

        registrar.playToServer(
            CRESavePacket.TYPE,
            CRESavePacket.STREAM_CODEC,
            CRENetworkHandler::handleSaveOnServer
        );

        registrar.playToServer(
            CREReloadPacket.TYPE,
            CREReloadPacket.STREAM_CODEC,
            CRENetworkHandler::handleReloadOnServer
        );
    }

    public static void sendToggleRecipe(String id, boolean enabled) {
        PacketDistributor.sendToServer(new CRETogglePacket(id, enabled));
    }

    public static void sendAddRecipe(CRERecipeEntry entry) {
        PacketDistributor.sendToServer(new CREAddRecipePacket(entry.toJson().toString()));
    }

    public static void sendDeleteRecipe(String id) {
        PacketDistributor.sendToServer(new CREDeleteRecipePacket(id));
    }

    public static void sendSaveRequest() {
        PacketDistributor.sendToServer(new CRESavePacket());
    }

    public static void sendReloadRequest() {
        PacketDistributor.sendToServer(new CREReloadPacket());
    }

    private static void handleToggleOnServer(CRETogglePacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ctx.sender().hasPermissions(2)) return;
            RecipeStateManager state = RecipeStateManager.getInstance();
            if (payload.enabled()) {
                state.enableRecipe(payload.recipeId());
            } else {
                state.disableRecipe(payload.recipeId());
            }
            CREMod.LOGGER.info("[CRE] {} recipe: {}", payload.enabled() ? "Enabled" : "Disabled", payload.recipeId());
        });
    }

    private static void handleAddRecipeOnServer(CREAddRecipePacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ctx.sender().hasPermissions(2)) return;
            try {
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(payload.entryJson()).getAsJsonObject();
                CRERecipeEntry entry = CRERecipeEntry.fromJson(obj);
                if (entry != null) {
                    RecipeStateManager.getInstance().addCustomRecipe(entry);
                    CREMod.LOGGER.info("[CRE] Added custom recipe: {}", entry.getId());
                }
            } catch (Exception e) {
                CREMod.LOGGER.error("[CRE] Failed to add recipe from packet: {}", e.getMessage());
            }
        });
    }

    private static void handleDeleteOnServer(CREDeleteRecipePacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ctx.sender().hasPermissions(2)) return;
            RecipeStateManager state = RecipeStateManager.getInstance();
            if (state.isDisabled(payload.recipeId())) {
                state.removeCustomRecipe(payload.recipeId());
                CREMod.LOGGER.info("[CRE] Deleted recipe: {}", payload.recipeId());
            }
        });
    }

    private static void handleSaveOnServer(CRESavePacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ctx.sender().hasPermissions(2)) return;
            KubeJSScriptManager.writeScripts();
            RecipeStateManager.getInstance().saveToDisk();
        });
    }

    private static void handleReloadOnServer(CREReloadPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!ctx.sender().hasPermissions(2)) return;
            KubeJSScriptManager.writeScripts();
            RecipeStateManager.getInstance().saveToDisk();
            net.minecraft.server.MinecraftServer server = ctx.sender().getServer();
            server.reloadResources(server.getPackRepository().getSelectedIds());
            CREMod.LOGGER.info("[CRE] Server reload triggered by {}", ctx.sender().getName().getString());
        });
    }
}
