package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.item.AstraliumArmorItem;
import com.hirensharma.astralium.item.AstraliumAxeItem;
import com.hirensharma.astralium.item.AstraliumHoeItem;
import com.hirensharma.astralium.item.AstraliumPickaxeItem;
import com.hirensharma.astralium.item.AstraliumShovelItem;
import com.hirensharma.astralium.item.AstraliumSwordItem;
import com.hirensharma.astralium.item.ModArmorMaterials;
import com.hirensharma.astralium.item.ModToolTiers;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AstraliumMod.MOD_ID);
    public static final RegistryObject<Item> ASTRALIUM_ORE = ITEMS.register("astralium_ore", () -> new BlockItem(ModBlocks.ASTRALIUM_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> ASTRALIUM_BLOCK = ITEMS.register("astralium_block", () -> new BlockItem(ModBlocks.ASTRALIUM_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> RAW_ASTRALIUM_BLOCK = ITEMS.register("raw_astralium_block", () -> new BlockItem(ModBlocks.RAW_ASTRALIUM_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> RAW_ASTRALIUM = ITEMS.register("raw_astralium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ASTRALIUM_INGOT = ITEMS.register("astralium_ingot", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ASTRALIUM_UPGRADE_TEMPLATE = ITEMS.register("astralium_upgrade_template", ModItems::createUpgradeTemplate);
    public static final RegistryObject<Item> ASTRALIUM_SWORD = ITEMS.register("astralium_sword", () -> new AstraliumSwordItem(ModToolTiers.ASTRALIUM, 4, -2.3F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_PICKAXE = ITEMS.register("astralium_pickaxe", () -> new AstraliumPickaxeItem(ModToolTiers.ASTRALIUM, 2, -2.7F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_AXE = ITEMS.register("astralium_axe", () -> new AstraliumAxeItem(ModToolTiers.ASTRALIUM, 6.0F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_SHOVEL = ITEMS.register("astralium_shovel", () -> new AstraliumShovelItem(ModToolTiers.ASTRALIUM, 2.0F, -2.9F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_HOE = ITEMS.register("astralium_hoe", () -> new AstraliumHoeItem(ModToolTiers.ASTRALIUM, -3, 0.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_HELMET = ITEMS.register("astralium_helmet", () -> new AstraliumArmorItem(ModArmorMaterials.ASTRALIUM, ArmorItem.Type.HELMET, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_CHESTPLATE = ITEMS.register("astralium_chestplate", () -> new AstraliumArmorItem(ModArmorMaterials.ASTRALIUM, ArmorItem.Type.CHESTPLATE, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_LEGGINGS = ITEMS.register("astralium_leggings", () -> new AstraliumArmorItem(ModArmorMaterials.ASTRALIUM, ArmorItem.Type.LEGGINGS, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ASTRALIUM_BOOTS = ITEMS.register("astralium_boots", () -> new AstraliumArmorItem(ModArmorMaterials.ASTRALIUM, ArmorItem.Type.BOOTS, new Item.Properties().fireResistant()));
    private ModItems() {}

    private static SmithingTemplateItem createUpgradeTemplate() {
        return new SmithingTemplateItem(
            Component.translatable("item.astralium.astralium_upgrade_template.applies_to"),
            Component.translatable("item.astralium.astralium_upgrade_template.ingredients"),
            Component.translatable("item.astralium.astralium_upgrade_template.title"),
            Component.translatable("item.astralium.astralium_upgrade_template.base_slot_description"),
            Component.translatable("item.astralium.astralium_upgrade_template.additions_slot_description"),
            List.of(
                new ResourceLocation("item/empty_slot_helmet"), new ResourceLocation("item/empty_slot_chestplate"),
                new ResourceLocation("item/empty_slot_leggings"), new ResourceLocation("item/empty_slot_boots"),
                new ResourceLocation("item/empty_slot_sword"), new ResourceLocation("item/empty_slot_pickaxe"),
                new ResourceLocation("item/empty_slot_axe"), new ResourceLocation("item/empty_slot_hoe"),
                new ResourceLocation("item/empty_slot_shovel")),
            List.of(new ResourceLocation("item/empty_slot_ingot")));
    }
}
