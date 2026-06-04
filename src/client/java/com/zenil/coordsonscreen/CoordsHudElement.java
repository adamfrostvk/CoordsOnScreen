package com.zenil.coordsonscreen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class CoordsHudElement implements HudElement {

    // Structure lookups touch the integrated server, so cache by position and
    // only recompute when the player moves to a new block (or changes dimension).
    private long cachedStructurePos = Long.MIN_VALUE;
    private ResourceKey<Level> cachedStructureDim = null;
    private Component cachedStructureLine = null;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        CoordsConfig config = CoordsConfig.get();

        ResourceKey<Level> dim = mc.level.dimension();
        BlockPos pos = mc.player.blockPosition();

        // Lines in top-to-bottom order assuming a TOP anchor: coordinates first,
        // then biome, then structure. For a BOTTOM anchor we reverse below so the
        // coordinates stay nearest the bottom edge.
        List<Component> lines = new ArrayList<>(3);
        if (config.showMainCoords) {
            Component coords = buildLine(dim, pos.getX(), pos.getY(), pos.getZ(), config);
            if (coords != null) lines.add(coords);
        }
        // The End is effectively a single biome, so there's nothing useful to show.
        if (config.showBiome && !dim.equals(Level.END)) {
            lines.add(buildBiomeLine(mc.level, pos, config));
        }
        if (config.showStructure) {
            Component structure = structureLine(mc, dim, pos);
            if (structure != null) lines.add(structure);
        }
        if (lines.isEmpty()) return;

        boolean bottom = config.position == CoordsConfig.HudPosition.BOTTOM_LEFT
                || config.position == CoordsConfig.HudPosition.BOTTOM_RIGHT;
        boolean right = config.position == CoordsConfig.HudPosition.TOP_RIGHT
                || config.position == CoordsConfig.HudPosition.BOTTOM_RIGHT;

        if (bottom) Collections.reverse(lines);

        Font font = mc.font;
        int lineH = font.lineHeight;
        int screenW = graphics.guiWidth();
        int screenH = graphics.guiHeight();
        int px = config.xPadding;
        int py = config.yPadding;

        // Counter-scale by the GUI scale so the chosen size is a fixed on-screen
        // size: physical height = lineH * guiScale * scale stays constant.
        int guiScale = Math.max(1, mc.getWindow().getGuiScale());
        float scale = config.fontSizePercent / (50f * guiScale);
        float scaledLineH = lineH * scale;
        float blockHeight = scaledLineH * lines.size();
        float blockTop = bottom ? (screenH - blockHeight - py) : py;

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            float scaledWidth = font.width(line) * scale;
            float x = right ? (screenW - scaledWidth - px) : px;
            float y = blockTop + i * scaledLineH;

            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(font, line, 0, 0, 0xFFFFFFFF, true);
            graphics.pose().popMatrix();
        }
    }

    private static Component buildBiomeLine(ClientLevel level, BlockPos pos, CoordsConfig config) {
        Holder<Biome> holder = level.getBiome(pos);
        Optional<ResourceKey<Biome>> keyOpt = holder.unwrapKey();

        // translatableWithFallback honors a (modded) biome's own translation
        // when present, and otherwise shows a clean prettified name rather than
        // the raw "biome.mymod.crystal_forest" key.
        MutableComponent name = keyOpt
            .map(key -> {
                Identifier id = key.identifier();
                String translationKey = "biome." + id.getNamespace() + "." + id.getPath().replace('/', '.');
                return Component.translatableWithFallback(translationKey, prettify(id.getPath()));
            })
            .orElseGet(() -> Component.literal("Unknown Biome"));

        if (!config.useBiomeColor) {
            return name; // plain white (default color passed to graphics.text)
        }

        // Prefer the curated vanilla palette; for modded biomes pick water or
        // grass color based on the biome's name so oceans/rivers/beaches don't
        // come out as generic green. Always lift dark colors to a readable
        // brightness.
        Integer curated = keyOpt.map(BiomeColors::curated).orElse(null);
        int raw;
        if (curated != null) {
            raw = curated;
        } else {
            Biome biome = holder.value();
            String path = keyOpt.map(k -> k.identifier().getPath()).orElse("");
            boolean watery = path.contains("ocean")
                || path.contains("river")
                || path.contains("beach")
                || path.contains("shore")
                || path.contains("sea");
            raw = (watery
                ? biome.getWaterColor()
                : biome.getGrassColor(pos.getX(), pos.getZ())) & 0xFFFFFF;
        }
        return name.withColor(ensureReadable(raw));
    }

    /**
     * Returns the name of the structure the player is standing inside, or
     * {@code null}. Only works in singleplayer: structure data is not sent to
     * clients on multiplayer servers, so there is nothing to read there.
     */
    private Component structureLine(Minecraft mc, ResourceKey<Level> dim, BlockPos pos) {
        long key = pos.asLong();
        if (key == cachedStructurePos && dim.equals(cachedStructureDim)) {
            return cachedStructureLine;
        }
        cachedStructurePos = key;
        cachedStructureDim = dim;
        cachedStructureLine = computeStructureLine(mc, dim, pos);
        return cachedStructureLine;
    }

    private static Component computeStructureLine(Minecraft mc, ResourceKey<Level> dim, BlockPos pos) {
        if (!mc.hasSingleplayerServer()) return null;
        try {
            IntegratedServer server = mc.getSingleplayerServer();
            if (server == null) return null;
            ServerLevel serverLevel = server.getLevel(dim);
            if (serverLevel == null) return null;

            StructureManager structureManager = serverLevel.structureManager();
            Map<Structure, ?> structures = structureManager.getAllStructuresAt(pos);
            if (structures.isEmpty()) return null;

            Registry<Structure> registry = server.registryAccess().lookup(Registries.STRUCTURE).orElseThrow();
            for (Structure structure : structures.keySet()) {
                StructureStart start = structureManager.getStructureWithPieceAt(pos, structure);
                if (start != null && start.isValid()) {
                    Identifier id = registry.getKey(structure);
                    MutableComponent line;
                    if (id == null) {
                        line = Component.literal("Structure");
                    } else {
                        // translatableWithFallback lets mods supply a nicer name
                        // via a translation key, while modded/vanilla structures
                        // without one fall back to a prettified id path.
                        String tkey = "structure." + id.getNamespace() + "." + id.getPath().replace('/', '.');
                        line = Component.translatableWithFallback(tkey, prettify(id.getPath()));
                    }
                    return line.withStyle(ChatFormatting.GOLD);
                }
            }
            return null;
        } catch (Exception e) {
            // Reading server state from the render thread can rarely race; never
            // let that crash the HUD.
            return null;
        }
    }

    private static String prettify(String path) {
        // Treat both '_' (snake_case) and '/' (nested resource paths) as word
        // separators so modded ids like "ancient/lost_city" become "Ancient
        // Lost City" rather than the raw path.
        String[] parts = path.split("[_/]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.isEmpty() ? path : sb.toString();
    }

    // Lift very dark colors to a minimum brightness so the text stays legible on
    // the HUD while keeping the original hue.
    private static int ensureReadable(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        if (max == 0) return 0xAAAAAA;
        int target = 170;
        if (max < target) {
            float f = target / (float) max;
            r = Math.min(255, Math.round(r * f));
            g = Math.min(255, Math.round(g * f));
            b = Math.min(255, Math.round(b * f));
        }
        return (r << 16) | (g << 8) | b;
    }

    private static MutableComponent buildLine(
            ResourceKey<Level> dim, int x, int y, int z, CoordsConfig config) {

        if (dim.equals(Level.OVERWORLD)) {
            MutableComponent main = Component.empty()
                .append(Component.literal("Overworld ").withStyle(ChatFormatting.GREEN))
                .append(coordText(x, y, z));
            if (!config.showAltCoords) return main;
            return main
                .append(Component.literal(" ["))
                .append(Component.literal("Nether ").withStyle(ChatFormatting.RED))
                .append(coordText(x / 8, y, z / 8))
                .append(Component.literal("]"));

        } else if (dim.equals(Level.NETHER)) {
            MutableComponent main = Component.empty()
                .append(Component.literal("Nether ").withStyle(ChatFormatting.RED))
                .append(coordText(x, y, z));
            if (!config.showAltCoords) return main;
            return main
                .append(Component.literal(" ["))
                .append(Component.literal("Overworld ").withStyle(ChatFormatting.GREEN))
                .append(coordText(x * 8, y, z * 8))
                .append(Component.literal("]"));

        } else if (dim.equals(Level.END)) {
            return Component.empty()
                .append(Component.literal("End ").withStyle(ChatFormatting.DARK_PURPLE))
                .append(coordText(x, y, z));
        }

        return Component.empty().append(coordText(x, y, z));
    }

    private static Component coordText(int x, int y, int z) {
        return Component.literal("X: %d, Y: %d, Z: %d".formatted(x, y, z));
    }
}
