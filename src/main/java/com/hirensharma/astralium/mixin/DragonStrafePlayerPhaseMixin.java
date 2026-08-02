package com.hirensharma.astralium.mixin;

import com.hirensharma.astralium.VoidFloorHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonStrafePlayerPhaseMixin {
    @Shadow
    private LivingEntity attackTarget;

    @Inject(method = "doServerTick", at = @At("HEAD"), cancellable = true)
    private void astralium$stopStrafingSupportedPlayer(CallbackInfo callbackInfo) {
        if (attackTarget instanceof Player player && VoidFloorHandler.isSupported(player)) {
            attackTarget = null;
            ((AbstractDragonPhaseInstanceAccessor) (Object) this)
                .astralium$getDragon()
                .getPhaseManager()
                .setPhase(EnderDragonPhase.HOLDING_PATTERN);
            callbackInfo.cancel();
        }
    }
}
