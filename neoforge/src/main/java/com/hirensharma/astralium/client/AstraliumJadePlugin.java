package com.hirensharma.astralium.client;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.registry.ModBlocks;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(AstraliumMod.MOD_ID)
public final class AstraliumJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.hideTarget(ModBlocks.VOID_FLOOR.get());
    }
}
