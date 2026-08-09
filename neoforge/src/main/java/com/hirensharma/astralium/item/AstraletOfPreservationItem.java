package com.hirensharma.astralium.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AstraletOfPreservationItem extends Item {
    public AstraletOfPreservationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.astralet.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.astralet.ability").withStyle(ChatFormatting.AQUA));
    }
}
