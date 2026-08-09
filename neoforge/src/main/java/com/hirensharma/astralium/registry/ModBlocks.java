package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.RuntimeMappings;
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
    public static final RegistryObject<Block> ASTRALIUM_ORE = BLOCKS.register("astralium_ore", () -> new Block(sound(requires(strength(mapColor(propertiesOf(), purple()), 3.0F, 3.0F)), amethystSound())));
    public static final RegistryObject<Block> ASTRALIUM_BLOCK = BLOCKS.register("astralium_block", () -> new Block(sound(requires(strength(mapColor(propertiesOf(), purple()), 5.0F, 6.0F)), netheriteBlockSound())));
    public static final RegistryObject<Block> RAW_ASTRALIUM_BLOCK = BLOCKS.register("raw_astralium_block", () -> new Block(sound(requires(strength(mapColor(propertiesOf(), terracottaPurple()), 5.0F, 6.0F)), deepslateSound())));
    public static final RegistryObject<Block> ASTRAL_FARMLAND = BLOCKS.register("astral_farmland", () -> new AstralFarmlandBlock(mapColor(randomTicks(propertiesCopy(net.minecraft.world.level.block.Blocks.FARMLAND)), purple())));
    public static final RegistryObject<Block> VOID_FLOOR = BLOCKS.register("void_floor", () -> new VoidFloorBlock(randomTicks(noOcclusion(noLootTable(strength(propertiesOf(), -1.0F, 3600000.0F))))));
    private ModBlocks() {}

    private static BlockBehaviour.Properties propertiesOf() {
        return (BlockBehaviour.Properties) RuntimeMappings.staticCall(BlockBehaviour.Properties.class, "of", "m_284310_");
    }

    private static MapColor purple() {
        return (MapColor) RuntimeMappings.staticField(MapColor.class, "COLOR_PURPLE", "f_283889_");
    }

    private static MapColor terracottaPurple() {
        return (MapColor) RuntimeMappings.staticField(MapColor.class, "TERRACOTTA_PURPLE", "f_283892_");
    }

    private static SoundType amethystSound() {
        return (SoundType) RuntimeMappings.staticField(SoundType.class, "AMETHYST", "f_154654_");
    }

    private static SoundType netheriteBlockSound() {
        return (SoundType) RuntimeMappings.staticField(SoundType.class, "NETHERITE_BLOCK", "f_56725_");
    }

    private static SoundType deepslateSound() {
        return (SoundType) RuntimeMappings.staticField(SoundType.class, "DEEPSLATE", "f_154677_");
    }

    private static BlockBehaviour.Properties propertiesCopy(BlockBehaviour block) {
        return (BlockBehaviour.Properties) RuntimeMappings.staticCall(BlockBehaviour.Properties.class, "copy", "m_60926_", block);
    }

    private static BlockBehaviour.Properties mapColor(BlockBehaviour.Properties properties, MapColor color) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "mapColor", "m_284180_", color);
    }

    private static BlockBehaviour.Properties strength(BlockBehaviour.Properties properties, float destroyTime, float explosionResistance) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "strength", "m_60913_", destroyTime, explosionResistance);
    }

    private static BlockBehaviour.Properties requires(BlockBehaviour.Properties properties) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "requiresCorrectToolForDrops", "m_60999_");
    }

    private static BlockBehaviour.Properties sound(BlockBehaviour.Properties properties, SoundType sound) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "sound", "m_60918_", sound);
    }

    private static BlockBehaviour.Properties randomTicks(BlockBehaviour.Properties properties) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "randomTicks", "m_60977_");
    }

    private static BlockBehaviour.Properties noLootTable(BlockBehaviour.Properties properties) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "noLootTable", "m_60966_");
    }

    private static BlockBehaviour.Properties noOcclusion(BlockBehaviour.Properties properties) {
        return (BlockBehaviour.Properties) RuntimeMappings.call(properties, "noOcclusion", "m_60955_");
    }
}
