package com.hirensharma.astralium.item;

import com.hirensharma.astralium.AstraliumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class AstraliumArmorItem extends ArmorItem {
    private static final ResourceLocation LAYER_1 = new ResourceLocation(
        AstraliumMod.MOD_ID, "textures/models/armor/astralium_layer_1.png"
    );
    private static final ResourceLocation LAYER_2 = new ResourceLocation(
        AstraliumMod.MOD_ID, "textures/models/armor/astralium_layer_2.png"
    );

    public AstraliumArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, net.minecraft.world.entity.EquipmentSlot slot, String type) {
        return (slot == net.minecraft.world.entity.EquipmentSlot.LEGS ? LAYER_2 : LAYER_1).toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.armor.set_bonus").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.armor.fall").withStyle(ChatFormatting.GRAY));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.armor.double_jump").withStyle(ChatFormatting.GRAY));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.armor.void_floor").withStyle(ChatFormatting.GRAY));
    }
}
