package com.hirensharma.astralium;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class AstralFarmlandSupport {
    private static final TagKey<Block> COMPATIBLE_CROPS = TagKey.create(Registries.BLOCK, new ResourceLocation(AstraliumMod.MOD_ID, "astral_farmland_crops"));
    private static final TagKey<Block> VALID_FARMLAND = TagKey.create(Registries.BLOCK, new ResourceLocation(AstraliumMod.MOD_ID, "valid_crop_farmland"));

    private AstralFarmlandSupport() {}

    public static boolean canSurviveOnAstralFarmland(BlockState crop, LevelReader level, BlockPos position) {
        return crop.is(COMPATIBLE_CROPS) && level.getBlockState(position.below()).is(VALID_FARMLAND);
    }
}
