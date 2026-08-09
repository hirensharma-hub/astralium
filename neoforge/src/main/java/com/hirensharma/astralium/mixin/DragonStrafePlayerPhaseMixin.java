package com.hirensharma.astralium.mixin;

import com.hirensharma.astralium.VoidFloorHandler;
import com.hirensharma.astralium.RuntimeMappings;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonStrafePlayerPhaseMixin {
    @Inject(method = "doServerTick", at = @At("HEAD"), cancellable = true)
    private void astralium$stopStrafingSupportedPlayer(CallbackInfo callbackInfo) {
        LivingEntity attackTarget = (LivingEntity) RuntimeMappings.instanceField(this, "attackTarget", "f_31353_");
        if (attackTarget instanceof Player player && VoidFloorHandler.isSupported(player)) {
            RuntimeMappings.setInstanceField(this, "attackTarget", "f_31353_", null);
            ((AbstractDragonPhaseInstanceAccessor) (Object) this)
                .astralium$getDragon()
                .getPhaseManager()
                .setPhase(EnderDragonPhase.HOLDING_PATTERN);
            callbackInfo.cancel();
        }
    }
}
