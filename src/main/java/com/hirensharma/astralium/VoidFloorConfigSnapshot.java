package com.hirensharma.astralium;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record VoidFloorConfigSnapshot(
    boolean enabled,
    List<String> dimensions,
    int floorOffset,
    double resetMargin,
    boolean protectAstraliumItems
) {
    private static final VoidFloorConfigSnapshot DISABLED = new VoidFloorConfigSnapshot(false, List.of(), 0, 1.0D, false);
    private static volatile VoidFloorConfigSnapshot client = DISABLED;

    public VoidFloorConfigSnapshot {
        dimensions = List.copyOf(dimensions);
    }

    public static VoidFloorConfigSnapshot fromServerConfig() {
        return new VoidFloorConfigSnapshot(
            AstraliumConfig.VOID_FLOOR_ENABLED.get(),
            AstraliumConfig.VOID_FLOOR_DIMENSIONS.get().stream().limit(256).map(String::valueOf).toList(),
            AstraliumConfig.VOID_FLOOR_OFFSET.get(),
            AstraliumConfig.VOID_FLOOR_RESET_MARGIN.get(),
            AstraliumConfig.VOID_FLOOR_PROTECT_ASTRALIUM_ITEMS.get());
    }

    public static VoidFloorConfigSnapshot client() {
        return client;
    }

    public static void applyClient(VoidFloorConfigSnapshot snapshot) {
        client = snapshot;
    }

    public static void resetClient() {
        client = DISABLED;
    }

    public boolean enabledFor(Level level) {
        if (!enabled) return false;
        String dimension = level.dimension().location().toString();
        return dimensions.stream().anyMatch(configured -> ResourceLocation.tryParse(configured) != null && dimension.equals(configured));
    }
}
