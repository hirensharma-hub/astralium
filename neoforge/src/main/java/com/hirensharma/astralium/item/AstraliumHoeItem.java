package com.hirensharma.astralium.item;

import com.hirensharma.astralium.AstraliumConfig;
import com.hirensharma.astralium.ModCriteriaTriggers;
import com.hirensharma.astralium.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;

public class AstraliumHoeItem extends HoeItem {
    public AstraliumHoeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!AstraliumConfig.STARLIT_SOIL_ENABLED.get()) return super.useOn(context);

        Level level = context.getLevel();
        BlockPos position = context.getClickedPos();
        BlockState current = level.getBlockState(position);
        if (context.getClickedFace() == Direction.DOWN) return InteractionResult.PASS;

        BlockState target = null;
        if (current.is(Blocks.FARMLAND) && AstraliumConfig.STARLIT_SOIL_ALLOW_FARMLAND_UPGRADE.get()) {
            target = com.hirensharma.astralium.RuntimeMappings.setValue(ModBlocks.ASTRAL_FARMLAND.get().defaultBlockState(), net.minecraft.world.level.block.FarmBlock.MOISTURE, com.hirensharma.astralium.RuntimeMappings.getValue(current, net.minecraft.world.level.block.FarmBlock.MOISTURE));
        } else if (level.getBlockState(position.above()).isAir()) {
            BlockState vanillaResult = current.getToolModifiedState(context, ToolActions.HOE_TILL, false);
            if (vanillaResult != null && vanillaResult.is(Blocks.FARMLAND)) target = ModBlocks.ASTRAL_FARMLAND.get().defaultBlockState();
        }
        if (target == null) return super.useOn(context);

        Player player = context.getPlayer();
        level.playSound(player, position, (net.minecraft.sounds.SoundEvent) com.hirensharma.astralium.RuntimeMappings.staticField(SoundEvents.class, "AMETHYST_BLOCK_CHIME", "f_144243_"), SoundSource.BLOCKS, 0.45F, 1.45F);
        if (!level.isClientSide) {
            level.setBlock(position, target, 11);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, position.getX() + 0.5D, position.getY() + 0.85D, position.getZ() + 0.5D, 4, 0.18D, 0.08D, 0.18D, 0.005D);
            }
            if (player != null) {
                ItemStack stack = context.getItemInHand();
                if (!player.isCreative() || AstraliumConfig.STARLIT_SOIL_CREATIVE_DAMAGES_HOE.get()) {
                    stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
                }
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) ModCriteriaTriggers.STARLIT_SOIL.trigger(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.hoe.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.hoe.description").withStyle(ChatFormatting.GRAY));
        tooltip.add(com.hirensharma.astralium.RuntimeMappings.translatable("tooltip.astralium.hoe.growth").withStyle(ChatFormatting.GRAY));
    }
}
