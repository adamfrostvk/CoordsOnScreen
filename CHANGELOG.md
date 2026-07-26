# Changelog

> **Builds are published per Minecraft version**, named `mc<mc_version>_<mod_version>` (e.g. `coordsonscreen-mc26.2_1.2.0.jar`). The version headings below describe the feature set; the same mod version may be released for more than one Minecraft version so popular modded MC versions stay supported.

## 1.2.0
*Supported Minecraft versions: 26.2, 26.1.2.*
- The mod now has its own config file handling, so **Cloth Config is no longer required**. Settings live in `config/coordsonscreen.json` and can be edited by hand; missing fields fall back to defaults and out-of-range values are clamped automatically.
- **Cloth Config and ModMenu are now optional** — they're only needed for the in-game settings screen. Without them, the mod still loads and is fully configurable via the file.
- If ModMenu is installed but Cloth Config isn't, the Configure button is simply hidden instead of erroring.
- **Minecraft 26.2 build** (`mc26.2_1.2.0`): rebuilt against Minecraft 26.2 with Fabric Loader 0.19.3, Fabric API 0.153.0+26.2, Cloth Config 26.2.155, and ModMenu 20.0.0-beta.4. No feature changes — same 1.2.0 feature set. The 26.2 build requires Minecraft 26.2 and does not load on 26.1.2; use the `mc26.1.2_1.2.0` build for 26.1.2.
- Build toolchain updated to the stable **Fabric Loom 1.17.13** (previously the 1.16 snapshot line) and **Gradle 9.6.1**. No effect on the shipped jar's behavior.
- Dev/build dependencies refreshed for the 26.2 target: **Fabric API 0.155.2+26.2** and **ModMenu 20.0.1** (first stable ModMenu for 26.2, up from 20.0.0-beta.4). No feature changes.

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
