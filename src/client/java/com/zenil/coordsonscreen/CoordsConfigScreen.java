package com.zenil.coordsonscreen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class CoordsConfigScreen {

    private CoordsConfigScreen() {}

    public static Screen create(Screen parent) {
        CoordsConfig config = CoordsConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.coordsonscreen.title"))
            .setSavingRunnable(CoordsConfig::save);

        ConfigEntryBuilder entry = builder.entryBuilder();
        ConfigCategory display = builder.getOrCreateCategory(
            Component.translatable("config.coordsonscreen.category.display"));

        display.addEntry(entry
            .startEnumSelector(
                Component.translatable("config.coordsonscreen.position"),
                CoordsConfig.HudPosition.class,
                config.position)
            .setDefaultValue(CoordsConfig.HudPosition.TOP_LEFT)
            .setTooltip(Component.translatable("config.coordsonscreen.position.tooltip"))
            .setEnumNameProvider(pos -> Component.translatable(
                "config.coordsonscreen.position." + pos.name().toLowerCase()))
            .setSaveConsumer(v -> config.position = v)
            .build());

        display.addEntry(entry
            .startIntSlider(
                Component.translatable("config.coordsonscreen.x_padding"),
                config.xPadding, 0, 200)
            .setDefaultValue(5)
            .setTooltip(Component.translatable("config.coordsonscreen.x_padding.tooltip"))
            .setSaveConsumer(v -> config.xPadding = v)
            .build());

        display.addEntry(entry
            .startIntSlider(
                Component.translatable("config.coordsonscreen.y_padding"),
                config.yPadding, 0, 200)
            .setDefaultValue(5)
            .setTooltip(Component.translatable("config.coordsonscreen.y_padding.tooltip"))
            .setSaveConsumer(v -> config.yPadding = v)
            .build());

        display.addEntry(entry
            .startIntSlider(
                Component.translatable("config.coordsonscreen.font_size"),
                config.fontSizePercent, 50, 300)
            .setDefaultValue(100)
            .setTextGetter(v -> Component.literal(v + "%"))
            .setTooltip(Component.translatable("config.coordsonscreen.font_size.tooltip"))
            .setSaveConsumer(v -> config.fontSizePercent = v)
            .build());

        display.addEntry(entry
            .startBooleanToggle(
                Component.translatable("config.coordsonscreen.show_main_coords"),
                config.showMainCoords)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.coordsonscreen.show_main_coords.tooltip"))
            .setSaveConsumer(v -> config.showMainCoords = v)
            .build());

        display.addEntry(entry
            .startBooleanToggle(
                Component.translatable("config.coordsonscreen.show_alt_coords"),
                config.showAltCoords)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.coordsonscreen.show_alt_coords.tooltip"))
            .setSaveConsumer(v -> config.showAltCoords = v)
            .build());

        display.addEntry(entry
            .startBooleanToggle(
                Component.translatable("config.coordsonscreen.show_biome"),
                config.showBiome)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.coordsonscreen.show_biome.tooltip"))
            .setSaveConsumer(v -> config.showBiome = v)
            .build());

        display.addEntry(entry
            .startBooleanToggle(
                Component.translatable("config.coordsonscreen.biome_color"),
                config.useBiomeColor)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.coordsonscreen.biome_color.tooltip"))
            .setSaveConsumer(v -> config.useBiomeColor = v)
            .build());

        display.addEntry(entry
            .startBooleanToggle(
                Component.translatable("config.coordsonscreen.show_structure"),
                config.showStructure)
            .setDefaultValue(true)
            .setTooltip(Component.translatable("config.coordsonscreen.show_structure.tooltip"))
            .setSaveConsumer(v -> config.showStructure = v)
            .build());

        return builder.build();
    }
}
