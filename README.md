# Astralium

> **Crystallised starlight for the outer End.**

Astralium is a polished Forge mod about discovering a rare celestial metal among the End's distant islands. Its progression begins with difficult exploration and finishes at the smithing table: upgrade netherite equipment, build momentum through precise play, and use the Shifting Excavation pickaxe to shape terrain without sacrificing control.

## Highlights

- **Outer End discovery** — rare Astralium Ore hides in small End Stone veins away from the central island.
- **Meaningful progression** — smelt the material, discover an ancient upgrade template, then improve existing netherite equipment at a smithing table.
- **Shifting Excavation** — Astralium Pickaxes and Shovels each store their own 1×1, 3×1, 1×3, or 3×3 mode. Press `V` while holding either tool to cycle modes.
- **Starlit Soil** — the Astralium Hoe creates Astral Farmland, where compatible crops receive extra normal growth opportunities (about 2× by default).
- **Astral Momentum** — sword and axe actions reward a steady rhythm with a temporary, transparent five-level system instead of permanent stat creep.
- **Voidbound armour** — a full Astralium set creates a personal virtual floor in configured void dimensions; protected Astralium drops rest there too.
- **Original art direction** — restrained textures built around deep purple, violet, silver-white, and pale blue so Astralium sits naturally beside vanilla assets.

## Versions

- Minecraft: 1.20.1
- Forge: 47.2.0
- Java: 17
- Mod ID: `astralium`
- Package: `com.hirensharma.astralium`
- Licence: MIT for source code; original artwork is covered separately by [ASSET_LICENSE.md](ASSET_LICENSE.md). Minecraft and Forge belong to their respective owners and are not included under either licence.

## Development

- Build: `./gradlew build`
- Run client: `./gradlew runClient`
- Run server: `./gradlew runServer`
- Data generation task is configured as `./gradlew runData`; the checked-in resources are already present.
- Built JAR path: `build/libs/astralium-1.2.3.jar`

## Installation

1. Install Minecraft Forge `47.2.0` for Minecraft `1.20.1`.
2. Place `astralium-1.2.3.jar` in your Minecraft instance's `mods` folder.
3. Launch Minecraft with Java 17. For multiplayer, install the same JAR on the dedicated server and every client.

## Progression

1. Reach the outer End islands.
2. Mine rare Astralium Ore with netherite/diamond-tier mining capability.
3. Smelt Astralium Ore or Raw Astralium into Astralium Ingots.
4. Find exactly one Astralium Upgrade Template in an End Ship treasure chest.
5. Duplicate the template with seven diamonds and one Astralium Ingot.
6. Upgrade netherite gear in the smithing table.

## End Ship Template Placement

Astralium uses a narrow End City ship-piece hook: one existing vanilla ship chest receives the Astralium template loot table and the other chest remains vanilla. End City chests outside ships are untouched.

## Recipes

- Astralium Ore -> Astralium Ingot by furnace or blast furnace.
- Raw Astralium -> Astralium Ingot by furnace or blast furnace.
- 9 Astralium Ingots <-> Block of Astralium.
- 9 Raw Astralium <-> Block of Raw Astralium.
- Template duplication pattern: `DDD / DTD / DID`, where `D` is diamond, `T` is the template, and `I` is an Astralium Ingot. Output: 2 templates.

## Equipment Stats

- Tool durability: 2650, about 30% above netherite.
- Tool speed: 10.5.
- Tool tier level: 5.
- Enchantability: 22.
- Sword: 9 base attack damage before enchantments and momentum.
- Pickaxe: 7 base attack damage.
- Axe: 11.5 base attack damage.
- Armour durability multiplier: 48, about 32% above netherite.
- Armour points: helmet 4, chestplate 9, leggings 7, boots 4, for 24 total.
- Toughness: 3.5.
- Knockback resistance: 0.15 per piece.
- Repair material: Astralium Ingot.

## Astral Momentum

Astral Momentum has five levels. All systems share the same percentages: Level I 10%, II 20%, III 30%, IV 40%, V 50%. The default inactivity reset is 50 ticks.

- Rising Rhythm: Astralium Axe mining speed on axe-mineable blocks.
- Battle Rhythm: Astralium Axe attack-speed recovery after charged successful hits.
- Astral Edge: Astralium Sword percentage damage bonus on charged successful hit chains.

Momentum is temporary, server-side, independent per chain, and does not appear as an enchantment.

## Shifting Excavation

Astralium Pickaxes and Astralium Shovels store their selected modes per item stack. Default keybind: `V`, under the Astralium controls category. Pickaxes affect only pickaxe-mineable blocks; shovels affect only shovel-mineable blocks.

Modes: Normal 1x1, Horizontal 3x1, Vertical 1x3, Expanded 3x3. The shared `MiningPatternCalculator` is used by both server area mining and client highlight rendering. Disabled modes are skipped automatically.

For walls, the pattern is parallel to the targeted wall. For floors and ceilings, it uses X/Z orientation from the player camera direction. The area is always one block deep.

## Starlit Soil

The Astralium Hoe creates Astral Farmland when it tills ordinary soil, and can upgrade vanilla farmland while preserving its moisture value. Astral Farmland follows vanilla farmland hydration, survival, trampling, and dirt-drop behaviour. Compatible crops directly above it receive extra ordinary random-tick opportunities; the configurable default multiplier is `2.0`.

## Armour

With the complete set equipped, a virtual floor catches the player at the configured void height in allowed dimensions. It is not block placement. The complete set prevents fall damage and its visible fall damage animation. Removing the set before landing does not rescue the player. Astralium item entities use the same configured floor.

The full set also grants one server-authoritative double jump. It preserves normal horizontal momentum and resets only after a genuine ground or virtual-floor landing.

## Testing

Run `./gradlew clean build` before release. Manual in-game testing remains required for void-floor boundary behavior, End Ship chest generation, Smithing UI text, and visual texture checks.
