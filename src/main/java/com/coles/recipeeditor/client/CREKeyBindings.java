package com.coles.recipeeditor.client;

import com.coles.recipeeditor.gui.CREScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = "cre", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class CREKeyBindings {

    public static final KeyMapping OPEN_GUI = new KeyMapping(
        "key.cre.open_gui",
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_F8,
        "key.categories.cre"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI);
    }
}
