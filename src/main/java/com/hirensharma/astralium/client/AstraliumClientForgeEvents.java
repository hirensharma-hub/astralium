package com.hirensharma.astralium.client;

import com.hirensharma.astralium.AstraliumMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AstraliumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AstraliumClientForgeEvents {
    private AstraliumClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        AstraliumClient.ClientForgeEvents.onClientTick(event);
    }

    @SubscribeEvent
    public static void onRenderHighlight(RenderHighlightEvent.Block event) {
        AstraliumClient.ClientForgeEvents.onRenderHighlight(event);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        AstraliumClient.ClientForgeEvents.onRenderLevelStage(event);
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        AstraliumClient.ClientForgeEvents.onInteraction(event);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        AstraliumClient.ClientForgeEvents.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        AstraliumClient.ClientForgeEvents.onRightClickItem(event);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        com.hirensharma.astralium.ExcavationConfigSnapshot.resetClient();
        com.hirensharma.astralium.VoidFloorConfigSnapshot.resetClient();
    }

}
