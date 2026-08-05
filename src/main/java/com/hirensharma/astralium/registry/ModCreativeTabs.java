package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AstraliumMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> ASTRALIUM = CREATIVE_TABS.register("astralium", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.astralium"))
        .icon(() -> new ItemStack(ModItems.ASTRALIUM_INGOT.get()))
        .displayItems((parameters, output) -> {
            output.accept(ModItems.ASTRALIUM_SWORD.get());
            output.accept(ModItems.ASTRALIUM_PICKAXE.get());
            output.accept(ModItems.ASTRALIUM_AXE.get());
            output.accept(ModItems.ASTRALIUM_SHOVEL.get());
            output.accept(ModItems.ASTRALIUM_HOE.get());
            output.accept(ModItems.ASTRALIUM_HELMET.get());
            output.accept(ModItems.ASTRALIUM_CHESTPLATE.get());
            output.accept(ModItems.ASTRALIUM_LEGGINGS.get());
            output.accept(ModItems.ASTRALIUM_BOOTS.get());
            output.accept(ModItems.RAW_ASTRALIUM.get());
            output.accept(ModItems.ASTRALIUM_INGOT.get());
            output.accept(ModItems.UNSTABLE_STAR.get());
            output.accept(ModItems.ASTRALIUM_ORE.get());
            output.accept(ModItems.RAW_ASTRALIUM_BLOCK.get());
            output.accept(ModItems.ASTRALIUM_BLOCK.get());
            output.accept(ModItems.ASTRALIUM_UPGRADE_TEMPLATE.get());
        }).build());
    private ModCreativeTabs() {}
}
