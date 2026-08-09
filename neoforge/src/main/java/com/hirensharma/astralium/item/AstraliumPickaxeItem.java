package com.hirensharma.astralium.item;

import com.hirensharma.astralium.MiningMode;
import com.hirensharma.astralium.ExcavationTool;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AstraliumPickaxeItem extends PickaxeItem {
    private static final String MODE_TAG = "AstraliumMiningMode";

    public AstraliumPickaxeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    public static MiningMode getMode(ItemStack stack, ExcavationTool tool) {
        MiningMode mode = !stack.hasTag() ? MiningMode.NORMAL : MiningMode.byName(stack.getTag().getString(MODE_TAG));
        MiningMode corrected = tool.isModeEnabled(mode) ? mode : firstEnabledMode(tool);
        if (corrected != mode || !stack.hasTag() || !stack.getTag().contains(MODE_TAG)) {
            setMode(stack, corrected);
        }
        return corrected;
    }

    public static MiningMode getMode(ItemStack stack) { return getMode(stack, ExcavationTool.PICKAXE); }

    public static void setMode(ItemStack stack, MiningMode mode) {
        stack.getOrCreateTag().putString(MODE_TAG, mode.id());
    }

    public static MiningMode cycleMode(ItemStack stack, ExcavationTool tool) {
        MiningMode next = getMode(stack, tool).nextEnabled(tool);
        setMode(stack, next);
        return next;
    }

    public static MiningMode getPreviewMode(ItemStack stack, ExcavationTool tool) {
        MiningMode mode = !stack.hasTag() ? MiningMode.NORMAL : MiningMode.byName(stack.getTag().getString(MODE_TAG));
        if (tool.isPreviewModeEnabled(mode)) return mode;
        for (MiningMode candidate : MiningMode.values()) {
            if (tool.isPreviewModeEnabled(candidate)) return candidate;
        }
        return MiningMode.NORMAL;
    }

    private static MiningMode firstEnabledMode(ExcavationTool tool) {
        for (MiningMode mode : MiningMode.values()) {
            if (tool.isModeEnabled(mode)) return mode;
        }
        return MiningMode.NORMAL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        MiningMode mode = level != null && level.isClientSide
            ? getPreviewMode(stack, ExcavationTool.PICKAXE)
            : getMode(stack);
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.pickaxe.flavor").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.pickaxe.mode", mode.displayName()).withStyle(ChatFormatting.AQUA));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.pickaxe.cycle").withStyle(ChatFormatting.GRAY));
    }
}
