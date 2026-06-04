# Changelog

## 1.1.1
- Modded biomes now display a clean prettified name when the mod ships no translation key (e.g. `biome.mymod.crystal_forest` → "Crystal Forest"), and modded translations are honored when present.
- Modded biomes outside the curated palette now pick **water color** instead of grass color when the name indicates a water biome (`ocean`, `river`, `beach`, `shore`, `sea`), so modded oceans/beaches no longer come out as generic green.
- Modded structures can supply a translation key (`structure.<namespace>.<path>`) which will be honored; structures without one keep the prettified-id fallback.
- Prettifier now handles nested resource paths (`/`) in addition to `_`.

## 1.1.0
- Added a **biome display** on its own line (toggleable): below the coordinates when the HUD is at the top of the screen, above them when it's at the bottom.
- The biome name is **colored to fit the biome** via a curated palette (oceans blue/teal, beaches sandy, deserts tan, jungles vivid green, swamps murky, and so on); modded biomes fall back to their grass color. This can be toggled off for plain white.
- Added a **structure display** (toggleable) that shows the structure you're standing in, such as Stronghold or Ancient City. Singleplayer only — multiplayer servers don't send structure data to the client.
- The biome line is hidden in the End, which is effectively a single biome.

## 1.0.1
- Added a **Text Size** option that sets the on-screen size of the coordinates independently of the GUI Scale setting — the text stays the same size no matter the player's GUI Scale.

## 1.0.0
- Initial release: dimension-colored coordinates on the HUD (Overworld/Nether/End), with the equivalent cross-dimension coordinates.
- Configurable screen corner, horizontal/vertical padding, and toggles for the main and alternate coordinate lines, via a Cloth Config screen accessible through ModMenu.
