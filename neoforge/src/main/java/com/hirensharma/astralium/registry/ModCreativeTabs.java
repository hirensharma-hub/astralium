package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.RuntimeMappings;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(creativeModeTabRegistry(), AstraliumMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> ASTRALIUM = CREATIVE_TABS.register("astralium", ModCreativeTabs::createAstraliumTab);
    private static CreativeModeTab createAstraliumTab() {
        CreativeModeTab.Builder builder = new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0);
        RuntimeMappings.call(builder, "title", "m_257941_", RuntimeMappings.translatable("itemGroup.astralium"));
        RuntimeMappings.call(builder, "icon", "m_257737_", (Supplier<ItemStack>) () -> new ItemStack(ModItems.ASTRALIUM_INGOT.get()));
        RuntimeMappings.call(builder, "displayItems", "m_257501_", (CreativeModeTab.DisplayItemsGenerator) (parameters, output) -> {
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
            output.accept(ModItems.CONTAINED_STAR.get());
            output.accept(ModItems.ASTRALET_OF_PRESERVATION.get());
            output.accept(ModItems.ASTRALIUM_ORE.get());
            output.accept(ModItems.RAW_ASTRALIUM_BLOCK.get());
            output.accept(ModItems.ASTRALIUM_BLOCK.get());
            output.accept(ModItems.ASTRALIUM_UPGRADE_TEMPLATE.get());
        });
        return (CreativeModeTab) RuntimeMappings.call(builder, "build", "m_257652_");
    }
    private ModCreativeTabs() {}

    @SuppressWarnings("unchecked")
    private static ResourceKey<Registry<CreativeModeTab>> creativeModeTabRegistry() {
        try {
            Field field;
            try {
                field = Registries.class.getDeclaredField("CREATIVE_MODE_TAB");
            } catch (NoSuchFieldException ignored) {
                field = Registries.class.getDeclaredField("f_279569_");
            }
            field.setAccessible(true);
            return (ResourceKey<Registry<CreativeModeTab>>) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to locate the creative-mode-tab registry", exception);
        }
    }
}
