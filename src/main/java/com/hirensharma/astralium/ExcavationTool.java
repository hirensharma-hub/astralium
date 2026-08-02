package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public enum ExcavationTool {
    PICKAXE("pickaxe", BlockTags.MINEABLE_WITH_PICKAXE),
    SHOVEL("shovel", BlockTags.MINEABLE_WITH_SHOVEL);

    private final String translationKey;
    private final TagKey<Block> mineableTag;

    ExcavationTool(String translationKey, TagKey<Block> mineableTag) {
        this.translationKey = translationKey;
        this.mineableTag = mineableTag;
    }

    public String translationKey() { return translationKey; }
    public TagKey<Block> mineableTag() { return mineableTag; }

    public boolean isEnabled() {
        return switch (this) {
            case PICKAXE -> AstraliumConfig.PICKAXE_ENABLED.get();
            case SHOVEL -> AstraliumConfig.SHOVEL_ENABLED.get();
        };
    }

    public boolean isPreviewEnabled() {
        return ExcavationConfigSnapshot.client().toolEnabled(this);
    }

    public boolean isModeEnabled(MiningMode mode) {
        return switch (this) {
            case PICKAXE -> switch (mode) {
                case NORMAL -> AstraliumConfig.PICKAXE_MODE_NORMAL.get();
                case HORIZONTAL -> AstraliumConfig.PICKAXE_MODE_HORIZONTAL.get();
                case VERTICAL -> AstraliumConfig.PICKAXE_MODE_VERTICAL.get();
                case EXPANDED -> AstraliumConfig.PICKAXE_MODE_EXPANDED.get();
            };
            case SHOVEL -> switch (mode) {
                case NORMAL -> AstraliumConfig.SHOVEL_MODE_NORMAL.get();
                case HORIZONTAL -> AstraliumConfig.SHOVEL_MODE_HORIZONTAL.get();
                case VERTICAL -> AstraliumConfig.SHOVEL_MODE_VERTICAL.get();
                case EXPANDED -> AstraliumConfig.SHOVEL_MODE_EXPANDED.get();
            };
        };
    }

    public boolean isPreviewModeEnabled(MiningMode mode) {
        return ExcavationConfigSnapshot.client().modeEnabled(this, mode);
    }

    public boolean hasAnyEnabledMode() {
        for (MiningMode mode : MiningMode.values()) {
            if (isModeEnabled(mode)) return true;
        }
        return false;
    }

    public static ExcavationTool from(ItemStack stack) {
        if (stack.is(ModItems.ASTRALIUM_PICKAXE.get())) return PICKAXE;
        if (stack.is(ModItems.ASTRALIUM_SHOVEL.get())) return SHOVEL;
        return null;
    }
}
