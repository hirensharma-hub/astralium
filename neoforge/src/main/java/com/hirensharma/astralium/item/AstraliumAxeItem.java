package com.hirensharma.astralium.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AstraliumAxeItem extends AxeItem {
    public AstraliumAxeItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.axe.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.axe.ability").withStyle(ChatFormatting.AQUA));
    }
}
