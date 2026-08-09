package com.hirensharma.astralium.block;

import com.hirensharma.astralium.AstraliumConfig;
import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.Registries;

public class AstralFarmlandBlock extends FarmBlock {
    private static final TagKey<Block> COMPATIBLE_CROPS = com.hirensharma.astralium.RuntimeMappings.tagKey(com.hirensharma.astralium.RuntimeMappings.registryKey("BLOCK", "f_256747_"), new ResourceLocation(AstraliumMod.MOD_ID, "astral_farmland_crops"));

    public AstralFarmlandBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos position, RandomSource random) {
        super.randomTick(state, level, position, random);
        if (!AstraliumConfig.STARLIT_SOIL_ENABLED.get() || !level.getBlockState(position).is(ModBlocks.ASTRAL_FARMLAND.get())) return;

        BlockPos cropPosition = position.above();
        BlockState crop = level.getBlockState(cropPosition);
        if (!crop.is(COMPATIBLE_CROPS) || !crop.isRandomlyTicking() || !crop.canSurvive(level, cropPosition)) return;

        double extraTicks = AstraliumConfig.STARLIT_SOIL_GROWTH_MULTIPLIER.get() - 1.0D;
        int attempts = (int) Math.floor(extraTicks);
        if (random.nextDouble() < extraTicks - attempts) attempts++;
        for (int attempt = 0; attempt < attempts; attempt++) {
            crop = level.getBlockState(cropPosition);
            if (!crop.is(COMPATIBLE_CROPS) || !crop.isRandomlyTicking() || !crop.canSurvive(level, cropPosition)) break;
            crop.randomTick(level, cropPosition, random);
        }
    }
}
