package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import com.hirensharma.astralium.registry.ModBlocks;
import com.hirensharma.astralium.registry.ModNetworking;
import com.hirensharma.astralium.network.CycleMiningModePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public class AstraliumEvents {
    private static final String VOID_FLOOR_JUMP_TAG = "AstraliumVoidFloorJump";

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) return;
        if (event.phase == TickEvent.Phase.START) {
            if (!VoidFloorHandler.restoreSupportedLogout(player, true)) {
                VoidFloorHandler.rescueLoginPlayer(player);
            }
            return;
        }
        AstralMomentumHandler.tick(player);
        VoidFloorHandler.tickPlayer(player);
        if (VoidFloorHandler.isSupported(player)) {
            player.getPersistentData().putBoolean(VOID_FLOOR_JUMP_TAG, true);
        } else if (player.onGround()) {
            player.getPersistentData().remove(VOID_FLOOR_JUMP_TAG);
        }
        ArmorAbilityHandler.tick(player);
    }

    @SubscribeEvent
    public void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || event.isVanillaCritical()
            || !player.getPersistentData().getBoolean(VOID_FLOOR_JUMP_TAG)
            || player.onGround()
            || player.isSprinting()
            || player.isSwimming()) return;
        event.setDamageModifier(1.5F);
        event.setResult(net.minecraftforge.eventbus.api.Event.Result.ALLOW);
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (player instanceof ServerPlayer serverPlayer
            && stack.is(ModItems.ASTRALIUM_AXE.get())
            && event.getState().is(BlockTags.MINEABLE_WITH_AXE)) {
            event.setNewSpeed(event.getNewSpeed() * AstralMomentumHandler.bonusMultiplier(serverPlayer, AstralMomentumHandler.Chain.AXE_MINING));
        }
        PickaxeAreaMiningHandler.adjustBreakSpeed(event);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (ExcavationTool.from(event.getItemStack()) != null) {
            PickaxeAreaMiningHandler.rememberTarget(player, event.getPos(), event.getFace());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(ModBlocks.VOID_FLOOR.get())) {
            if (!event.getPlayer().isCreative() && !event.getPlayer().hasPermissions(2)) event.setCanceled(true);
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.is(ModItems.ASTRALIUM_AXE.get()) && event.getState().is(BlockTags.MINEABLE_WITH_AXE) && !player.isCreative()) {
            AstralMomentumHandler.advance(player, AstralMomentumHandler.Chain.AXE_MINING);
        }
        PickaxeAreaMiningHandler.afterCentralBreak(event);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)
            || event.getSource().getEntity() != player
            || !(event.getEntity() instanceof LivingEntity)) return;
        if (player.getAttackStrengthScale(0.5F) < 0.9F) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.is(ModItems.ASTRALIUM_SWORD.get())) {
            float multiplier = AstralMomentumHandler.bonusMultiplier(player, AstralMomentumHandler.Chain.SWORD_COMBAT);
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    @SubscribeEvent
    public void onLivingKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.handleKnockback(player);
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ArmorAbilityHandler.hasFullSet(player)) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
            && ArmorAbilityHandler.hasFullSet(player)
            && event.getSource().is(DamageTypes.FALL)) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
            return;
        }
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)
            || event.getSource().getEntity() != player
            || event.getAmount() <= 0.0F) return;
        if (player.getAttackStrengthScale(0.5F) < 0.9F) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.is(ModItems.ASTRALIUM_SWORD.get())) AstralMomentumHandler.queueSwordAdvance(player);
        if (stack.is(ModItems.ASTRALIUM_AXE.get())) AstralMomentumHandler.advance(player, AstralMomentumHandler.Chain.AXE_COMBAT);
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getOriginal() instanceof ServerPlayer original && event.isWasDeath()) {
                AstraletPreservationHandler.restoreOnClone(original, player);
            }
            VoidFloorHandler.clearSupportedLogoutMarker(event.getOriginal());
            VoidFloorHandler.clearSupportedLogoutMarker(player);
            AstralMomentumHandler.clear(player);
            ArmorAbilityHandler.clear(player);
            CycleMiningModePacket.clear(player);
            VoidFloorHandler.clearPlayer(player);
            PickaxeAreaMiningHandler.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.clearSupportedLogoutMarker(player);
            ArmorAbilityHandler.clear(player);
            CycleMiningModePacket.clear(player);
            VoidFloorHandler.clearPlayer(player);
            PickaxeAreaMiningHandler.clearPlayer(player.getUUID());
            AstralMomentumHandler.clear(player);
            ModNetworking.sendExcavationConfig(player);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.clearSupportedLogoutMarker(player);
            ArmorAbilityHandler.clear(player);
            CycleMiningModePacket.clear(player);
            VoidFloorHandler.clearPlayer(player);
            PickaxeAreaMiningHandler.clearPlayer(player.getUUID());
            AstralMomentumHandler.clear(player);
            ModNetworking.sendExcavationConfig(player);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.armLoginRestore(player);
            VoidFloorHandler.restoreSupportedLogout(player, true);
            ModNetworking.sendExcavationConfig(player);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.prepareForDisconnect(player);
            AstralMomentumHandler.clear(player);
            ArmorAbilityHandler.clear(player);
            CycleMiningModePacket.clear(player);
            PickaxeAreaMiningHandler.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AstraletPreservationHandler.captureOnDeath(player);
            VoidFloorHandler.clearSupportedLogoutMarker(player);
            AstralMomentumHandler.clear(player);
            ArmorAbilityHandler.clear(player);
            CycleMiningModePacket.clear(player);
            VoidFloorHandler.clearPlayer(player);
            PickaxeAreaMiningHandler.clearPlayer(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
            && AstraletPreservationHandler.shouldSuppressDrops(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VoidFloorHandler.handleTeleport(player, event.getTarget());
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            VoidFloorHandler.clearDimension(level);
            PickaxeAreaMiningHandler.clearAll();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            VoidFloorHandler.prepareForDisconnect(player);
        }
        ArmorAbilityHandler.clearAll();
        CycleMiningModePacket.clearAll();
        VoidFloorHandler.clearAll(event.getServer());
        PickaxeAreaMiningHandler.clearAll();
        AstralMomentumHandler.clearAll(event.getServer());
    }
}
