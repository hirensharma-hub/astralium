package com.hirensharma.astralium;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public final class AstraliumConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.BooleanValue MOMENTUM_ENABLED;
    public static final ForgeConfigSpec.IntValue MOMENTUM_MAX_LEVEL;
    public static final ForgeConfigSpec.IntValue MOMENTUM_PERCENT_PER_LEVEL;
    public static final ForgeConfigSpec.IntValue MOMENTUM_RESET_TICKS;
    public static final ForgeConfigSpec.BooleanValue MOMENTUM_LEVEL_FIVE_EFFECT;
    public static final ForgeConfigSpec.BooleanValue MOMENTUM_MESSAGES;
    public static final ForgeConfigSpec.BooleanValue EXCAVATION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PICKAXE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PICKAXE_MODE_NORMAL;
    public static final ForgeConfigSpec.BooleanValue PICKAXE_MODE_HORIZONTAL;
    public static final ForgeConfigSpec.BooleanValue PICKAXE_MODE_VERTICAL;
    public static final ForgeConfigSpec.BooleanValue PICKAXE_MODE_EXPANDED;
    public static final ForgeConfigSpec.BooleanValue SHOVEL_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SHOVEL_MODE_NORMAL;
    public static final ForgeConfigSpec.BooleanValue SHOVEL_MODE_HORIZONTAL;
    public static final ForgeConfigSpec.BooleanValue SHOVEL_MODE_VERTICAL;
    public static final ForgeConfigSpec.BooleanValue SHOVEL_MODE_EXPANDED;
    public static final ForgeConfigSpec.BooleanValue PROTECT_BLOCK_ENTITIES;
    public static final ForgeConfigSpec.BooleanValue CREATIVE_AREA_MINING;
    public static final ForgeConfigSpec.BooleanValue STARLIT_SOIL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue STARLIT_SOIL_GROWTH_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue STARLIT_SOIL_ALLOW_FARMLAND_UPGRADE;
    public static final ForgeConfigSpec.BooleanValue STARLIT_SOIL_CREATIVE_DAMAGES_HOE;
    public static final ForgeConfigSpec.BooleanValue VOID_FLOOR_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VOID_FLOOR_DIMENSIONS;
    public static final ForgeConfigSpec.IntValue VOID_FLOOR_OFFSET;
    public static final ForgeConfigSpec.DoubleValue VOID_FLOOR_RESET_MARGIN;
    public static final ForgeConfigSpec.BooleanValue VOID_FLOOR_PROTECT_ASTRALIUM_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<String> HIGHLIGHT_COLOR;
    public static final ForgeConfigSpec.DoubleValue HIGHLIGHT_THICKNESS;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.comment("Consecutive Astralium sword and axe actions build separate temporary Momentum chains.");
        common.push("astral_momentum");
        MOMENTUM_ENABLED = common.comment("Enables all Astral Momentum chains. Disabling this clears active chains and axe modifiers.").define("enabled", true);
        MOMENTUM_MAX_LEVEL = common.defineInRange("maxLevel", 5, 1, 5);
        MOMENTUM_PERCENT_PER_LEVEL = common.comment("Each Momentum level grants a fixed 10% bonus.").defineInRange("percentPerLevel", 10, 10, 10);
        MOMENTUM_RESET_TICKS = common.defineInRange("resetTicks", 50, 5, 20 * 60);
        MOMENTUM_LEVEL_FIVE_EFFECT = common.define("levelFiveEffect", true);
        MOMENTUM_MESSAGES = common.define("actionBarMessages", true);
        common.pop();

        common.comment("Server-authoritative area mining. These rules are synchronized to clients for accurate previews.");
        common.push("shifting_excavation");
        EXCAVATION_ENABLED = common.comment("Master switch. When false, mode cycling, previews, speed adjustment and extra breaking are disabled.").define("enabled", true);
        PROTECT_BLOCK_ENTITIES = common.comment("Prevents surrounding block entities from being included in excavation.").define("protectSurroundingBlockEntities", true);
        CREATIVE_AREA_MINING = common.comment("Allows expanded excavation while the player is in Creative mode.").define("creativeAreaMining", false);
        common.push("pickaxe");
        PICKAXE_ENABLED = common.define("enabled", true);
        PICKAXE_MODE_NORMAL = common.define("normalModeEnabled", true);
        PICKAXE_MODE_HORIZONTAL = common.define("horizontalModeEnabled", true);
        PICKAXE_MODE_VERTICAL = common.define("verticalModeEnabled", true);
        PICKAXE_MODE_EXPANDED = common.define("expandedModeEnabled", true);
        common.pop();
        common.push("shovel");
        SHOVEL_ENABLED = common.define("enabled", true);
        SHOVEL_MODE_NORMAL = common.define("normalModeEnabled", true);
        SHOVEL_MODE_HORIZONTAL = common.define("horizontalModeEnabled", true);
        SHOVEL_MODE_VERTICAL = common.define("verticalModeEnabled", true);
        SHOVEL_MODE_EXPANDED = common.define("expandedModeEnabled", true);
        common.pop();
        common.pop();

        common.push("starlit_soil");
        STARLIT_SOIL_ENABLED = common.define("enabled", true);
        STARLIT_SOIL_GROWTH_MULTIPLIER = common.defineInRange("growthMultiplier", 2.0D, 1.0D, 3.0D);
        STARLIT_SOIL_ALLOW_FARMLAND_UPGRADE = common.define("allowVanillaFarmlandUpgrade", true);
        STARLIT_SOIL_CREATIVE_DAMAGES_HOE = common.define("creativeTillingDamagesHoe", false);
        common.pop();

        common.comment("A complete Astralium armour set creates an invisible support floor in configured dimensions. Tagged Astralium items are also kept safely on that floor.");
        common.push("void_floor");
        VOID_FLOOR_ENABLED = common.comment("Enables the Astralium virtual floor ability.").define("enabled", true);
        VOID_FLOOR_DIMENSIONS = common.comment("Valid namespaced dimensions where the virtual floor exists.").defineListAllowEmpty("dimensionAllowlist", List.of("minecraft:the_end"), AstraliumConfig::isValidDimension);
        VOID_FLOOR_OFFSET = common.comment("Non-negative vertical offset from the dimension minimum build height.").defineInRange("floorOffset", 0, 0, 512);
        VOID_FLOOR_RESET_MARGIN = common.comment("Maximum reconnect recovery distance above the floor plane.").defineInRange("resetMargin", 1.0D, 0.5D, 8.0D);
        VOID_FLOOR_PROTECT_ASTRALIUM_ITEMS = common.comment("Keeps tagged Astralium item entities on the virtual floor and restores their original gravity state when support ends.").define("protectAstraliumItems", true);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        client.push("shifting_excavation");
        HIGHLIGHT_COLOR = client.define("highlightColor", "#8FD8FF", AstraliumConfig::isValidHexColor);
        HIGHLIGHT_THICKNESS = client.defineInRange("highlightThickness", 2.0D, 1.0D, 6.0D);
        client.pop();
        CLIENT_SPEC = client.build();
    }

    private AstraliumConfig() {}

    private static boolean isValidDimension(Object value) {
        return value instanceof String string && string.length() <= 256 && ResourceLocation.tryParse(string) != null;
    }

    private static boolean isValidHexColor(Object value) {
        return value instanceof String string && string.matches("^#?[0-9A-Fa-f]{6}$");
    }
}
