package com.hirensharma.astralium.item;

import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum ModToolTiers implements Tier {
    ASTRALIUM(2650, 10.5F, 5.0F, 5, 22);

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int level;
    private final int enchantmentValue;

    ModToolTiers(int uses, float speed, float attackDamageBonus, int level, int enchantmentValue) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.level = level;
        this.enchantmentValue = enchantmentValue;
    }

    public int getUses() { return uses; }
    public float getSpeed() { return speed; }
    public float getAttackDamageBonus() { return attackDamageBonus; }
    public int getLevel() { return level; }
    public int getEnchantmentValue() { return enchantmentValue; }
    public Ingredient getRepairIngredient() { return Ingredient.of(ModItems.ASTRALIUM_INGOT.get()); }
}
