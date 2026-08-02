package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.block.AstralFarmlandBlock;
import com.hirensharma.astralium.block.VoidFloorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AstraliumMod.MOD_ID);
    public static final RegistryObject<Block> ASTRALIUM_ORE = BLOCKS.register("astralium_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> ASTRALIUM_BLOCK = BLOCKS.register("astralium_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
    public static final RegistryObject<Block> RAW_ASTRALIUM_BLOCK = BLOCKS.register("raw_astralium_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final RegistryObject<Block> ASTRAL_FARMLAND = BLOCKS.register("astral_farmland", () -> new AstralFarmlandBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.FARMLAND).randomTicks().mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> VOID_FLOOR = BLOCKS.register("void_floor", () -> new VoidFloorBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noLootTable().noOcclusion().randomTicks()));
    private ModBlocks() {}
}
