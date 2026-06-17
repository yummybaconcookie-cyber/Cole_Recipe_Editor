package com.coles.recipeeditor;

import com.coles.recipeeditor.command.CRECommands;
import com.coles.recipeeditor.kubejs.KubeJSScriptManager;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("cre")
public class CREMod {
    public static final String MOD_ID = "cre";
    public static final String MOD_NAME = "Cole's Recipe Editor";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public CREMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        LOGGER.info("[CRE] Cole's Recipe Editor loading...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[CRE] Common setup complete.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("[CRE] Client setup complete.");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CRECommands.register(event.getDispatcher());
    }

    private void onServerStarting(ServerStartingEvent event) {
        RecipeStateManager.getInstance().loadFromDisk();
        LOGGER.info("[CRE] Recipe state loaded.");
    }
}
