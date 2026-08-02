package com.hirensharma.astralium.item;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModArmorMaterials implements ArmorMaterial {
    ASTRALIUM(AstraliumMod.MOD_ID + ":astralium", 48, new int[]{4, 7, 9, 4}, 22, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5F, 0.15F);

    private final String name;
    private final int durabilityMultiplier;
    private final int[] defense;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private static final int[] BASE_DURABILITY = new int[]{13, 15, 16, 11};

    ModArmorMaterials(String name, int durabilityMultiplier, int[] defense, int enchantmentValue, SoundEvent equipSound, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    public int getDurabilityForType(ArmorItem.Type type) { return BASE_DURABILITY[type.getSlot().getIndex()] * durabilityMultiplier; }
    public int getDefenseForType(ArmorItem.Type type) { return defense[type.getSlot().getIndex()]; }
    public int getEnchantmentValue() { return enchantmentValue; }
    public SoundEvent getEquipSound() { return equipSound; }
    public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.ASTRALIUM_INGOT.get()); }
    public String getName() { return name; }
    public float getToughness() { return toughness; }
    public float getKnockbackResistance() { return knockbackResistance; }
}
