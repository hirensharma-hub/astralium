package com.hirensharma.astralium;

public record ExcavationConfigSnapshot(
    boolean enabled,
    boolean pickaxeEnabled,
    int pickaxeModes,
    boolean shovelEnabled,
    int shovelModes,
    boolean protectBlockEntities,
    boolean creativeAreaMining
) {
    private static final ExcavationConfigSnapshot DISABLED = new ExcavationConfigSnapshot(
        false, false, 0, false, 0, true, false);
    private static volatile ExcavationConfigSnapshot client = DISABLED;

    public static ExcavationConfigSnapshot fromServerConfig() {
        return new ExcavationConfigSnapshot(
            AstraliumConfig.EXCAVATION_ENABLED.get(),
            AstraliumConfig.PICKAXE_ENABLED.get(),
            modeMask(ExcavationTool.PICKAXE),
            AstraliumConfig.SHOVEL_ENABLED.get(),
            modeMask(ExcavationTool.SHOVEL),
            AstraliumConfig.PROTECT_BLOCK_ENTITIES.get(),
            AstraliumConfig.CREATIVE_AREA_MINING.get());
    }

    public static ExcavationConfigSnapshot client() {
        return client;
    }

    public static void applyClient(ExcavationConfigSnapshot snapshot) {
        client = snapshot;
    }

    public static void resetClient() {
        client = DISABLED;
    }

    public boolean toolEnabled(ExcavationTool tool) {
        return enabled && switch (tool) {
            case PICKAXE -> pickaxeEnabled;
            case SHOVEL -> shovelEnabled;
        };
    }

    public boolean modeEnabled(ExcavationTool tool, MiningMode mode) {
        int modes = switch (tool) {
            case PICKAXE -> pickaxeModes;
            case SHOVEL -> shovelModes;
        };
        return (modes & (1 << mode.ordinal())) != 0;
    }

    private static int modeMask(ExcavationTool tool) {
        int mask = 0;
        for (MiningMode mode : MiningMode.values()) {
            if (tool.isModeEnabled(mode)) mask |= 1 << mode.ordinal();
        }
        return mask;
    }
}
