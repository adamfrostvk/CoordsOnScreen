package com.zenil.coordsonscreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // The in-game screen is built with Cloth Config, which is optional. If it
        // isn't installed, offer no screen (ModMenu hides the Configure button)
        // rather than crash — settings are still editable via the config file.
        // Returning before referencing CoordsConfigScreen avoids classloading it
        // (and its Cloth imports) when Cloth is absent.
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return screen -> null;
        }
        return CoordsConfigScreen::create;
    }
}
