package com.zenil.coordsonscreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class CoordsOnScreen implements ClientModInitializer {

    public static final String MOD_ID = "coordsonscreen";

    @Override
    public void onInitializeClient() {
        CoordsConfig.load();
        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath(MOD_ID, "coords"),
            new CoordsHudElement()
        );
    }
}
