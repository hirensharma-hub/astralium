package com.hirensharma.astralium;

import net.minecraft.network.chat.Component;

public enum MiningMode {
    NORMAL("normal", 1, 1),
    HORIZONTAL("horizontal", 3, 1),
    VERTICAL("vertical", 1, 3),
    EXPANDED("expanded", 3, 3);

    private final String id;
    private final int width;
    private final int height;

    MiningMode(String id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public String id() { return id; }
    public int width() { return width; }
    public int height() { return height; }
    public Component displayName() { return Component.translatable("astralium.mode." + id); }

    public MiningMode nextEnabled(ExcavationTool tool) {
        MiningMode[] modes = values();
        int index = ordinal();
        for (int step = 1; step <= modes.length; step++) {
            MiningMode candidate = modes[(index + step) % modes.length];
            if (tool.isModeEnabled(candidate)) return candidate;
        }
        return NORMAL;
    }

    public MiningMode nextPreviewEnabled(ExcavationTool tool) {
        MiningMode[] modes = values();
        int index = ordinal();
        for (int step = 1; step <= modes.length; step++) {
            MiningMode candidate = modes[(index + step) % modes.length];
            if (tool.isPreviewModeEnabled(candidate)) return candidate;
        }
        return NORMAL;
    }

    public static MiningMode byName(String name) {
        for (MiningMode mode : values()) {
            if (mode.id.equals(name)) return mode;
        }
        return NORMAL;
    }
}
