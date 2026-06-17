package com.coles.recipeeditor.client;

import com.coles.recipeeditor.gui.CREScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = "cre", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class CREClientEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (CREKeyBindings.OPEN_GUI.consumeClick()) {
            if (mc.screen == null && mc.player != null) {
                mc.setScreen(new CREScreen());
            }
        }
    }
}
