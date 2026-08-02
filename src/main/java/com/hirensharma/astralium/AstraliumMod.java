package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModBlocks;
import com.hirensharma.astralium.registry.ModCreativeTabs;
import com.hirensharma.astralium.registry.ModItems;
import com.hirensharma.astralium.registry.ModNetworking;
import com.hirensharma.astralium.network.CycleMiningModePacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(AstraliumMod.MOD_ID)
public class AstraliumMod {
    public static final String MOD_ID = "astralium";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AstraliumMod() {
        ModCriteriaTriggers.register();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_TABS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AstraliumConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AstraliumConfig.CLIENT_SPEC);
        modBus.addListener(ModNetworking::register);
        modBus.addListener(this::onConfigReload);
        MinecraftForge.EVENT_BUS.register(new AstraliumEvents());
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON
            || !MOD_ID.equals(event.getConfig().getModId())) return;
        net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        server.execute(() -> {
            AstralMomentumHandler.clearAll(server);
            PickaxeAreaMiningHandler.clearAll();
            VoidFloorHandler.onConfigReload(server);
            CycleMiningModePacket.resetWarnings();
            CycleMiningModePacket.validateConfiguration();
            for (net.minecraft.server.level.ServerPlayer player : server.getPlayerList().getPlayers()) {
                ModNetworking.sendExcavationConfig(player);
            }
        });
    }
}
