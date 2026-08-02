package com.hirensharma.astralium.client;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.ArmorAbilityHandler;
import com.hirensharma.astralium.ExcavationTool;
import com.hirensharma.astralium.MiningMode;
import com.hirensharma.astralium.MiningPatternCalculator;
import com.hirensharma.astralium.PickaxeAreaMiningHandler;
import com.hirensharma.astralium.item.AstraliumPickaxeItem;
import com.hirensharma.astralium.network.CycleMiningModePacket;
import com.hirensharma.astralium.network.DoubleJumpPacket;
import com.hirensharma.astralium.registry.ModNetworking;
import com.hirensharma.astralium.registry.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AstraliumMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AstraliumClient {
    public static final KeyMapping CYCLE_PICKAXE_MODE = new KeyMapping("key.astralium.cycle_pickaxe_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.categories.astralium");

    private AstraliumClient() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(CYCLE_PICKAXE_MODE);
    }

    public static void applyApprovedDoubleJump() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        Vec3 motion = minecraft.player.getDeltaMovement();
        minecraft.player.setDeltaMovement(
            motion.x,
            ArmorAbilityHandler.normalJumpVelocity(),
            motion.z);
        minecraft.player.hasImpulse = true;
        minecraft.player.fallDistance = 0.0F;
    }

    public static void applyMiningMode(int slot, ExcavationTool expectedTool, MiningMode mode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        ItemStack stack = minecraft.player.getInventory().getItem(slot);
        if (ExcavationTool.from(stack) == expectedTool) AstraliumPickaxeItem.setMode(stack, mode);
    }

    public static final class ClientForgeEvents {
        private static boolean jumpWasDown;
        private static boolean wasGrounded;

        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                jumpWasDown = false;
                wasGrounded = false;
                return;
            }
            boolean jumpDown = minecraft.options.keyJump.isDown();
            if (minecraft.screen == null && jumpDown && !jumpWasDown && !wasGrounded) requestDoubleJump(minecraft);
            if (minecraft.screen == null && CYCLE_PICKAXE_MODE.consumeClick()) requestModeCycle(minecraft);
            jumpWasDown = jumpDown;
            wasGrounded = minecraft.player.onGround()
                || com.hirensharma.astralium.VoidFloorHandler.isSupported(minecraft.player);
        }

        private static void requestModeCycle(Minecraft minecraft) {
            if (minecraft.player != null) {
                ExcavationTool tool = ExcavationTool.from(minecraft.player.getMainHandItem());
                if (tool != null) {
                    ModNetworking.CHANNEL.sendToServer(new CycleMiningModePacket(minecraft.player.getInventory().selected, tool));
                }
            }
        }

        private static void requestDoubleJump(Minecraft minecraft) {
            if (!minecraft.player.onGround()
                && !minecraft.player.getAbilities().flying
                && !minecraft.player.isFallFlying()
                && com.hirensharma.astralium.ArmorAbilityHandler.hasFullSet(minecraft.player)) {
                ModNetworking.CHANNEL.sendToServer(new DoubleJumpPacket());
            }
        }

        public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null || minecraft.screen != null || minecraft.hitResult == null) return;

            if (!event.isUseItem()) return;

            ItemStack stack = minecraft.player.getItemInHand(event.getHand());
            if (!(stack.getItem() instanceof BlockItem)) return;
            boolean targetingFloorTile = minecraft.hitResult instanceof BlockHitResult blockHit
                && minecraft.level.getBlockState(blockHit.getBlockPos()).is(ModBlocks.VOID_FLOOR.get());
            if (minecraft.hitResult.getType() != HitResult.Type.MISS && !targetingFloorTile) return;
            BlockHitResult floorHit = com.hirensharma.astralium.VoidFloorHandler.floorPlacementHit(minecraft.player);
            if (floorHit == null
                || minecraft.gameMode == null
                || touchesPlayer(floorHit.getBlockPos(), minecraft.player)) {
                event.setSwingHand(false);
                event.setCanceled(true);
                return;
            }

            InteractionResult result = minecraft.gameMode.useItemOn(minecraft.player, event.getHand(), floorHit);
            if (result.shouldSwing()) minecraft.player.swing(event.getHand());
            event.setSwingHand(false);
            event.setCanceled(true);
        }

        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (!event.getLevel().isClientSide
                || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.VOID_FLOOR.get())
                || !shouldCancelVoidFloorPlacement(event.getEntity(), event.getItemStack())) return;
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }

        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!event.getLevel().isClientSide
                || minecraft.hitResult == null
                || minecraft.hitResult.getType() != HitResult.Type.MISS
                || !shouldCancelVoidFloorPlacement(event.getEntity(), event.getItemStack())) return;
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }

        private static boolean shouldCancelVoidFloorPlacement(net.minecraft.world.entity.player.Player player, ItemStack stack) {
            if (!com.hirensharma.astralium.VoidFloorHandler.canUseFloor(player)
                || !(stack.getItem() instanceof BlockItem)) return false;
            BlockPos target = com.hirensharma.astralium.VoidFloorHandler.floorPlacementTarget(player);
            return target != null
                && touchesPlayer(target, player);
        }

        private static boolean touchesPlayer(BlockPos target, net.minecraft.world.entity.player.Player player) {
            return com.hirensharma.astralium.VoidFloorHandler.blocksPlacement(player, target);
        }

        @SubscribeEvent
        public static void onRenderHighlight(RenderHighlightEvent.Block event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.screen != null) return;
            ItemStack stack = minecraft.player.getMainHandItem();
            ExcavationTool excavationTool = ExcavationTool.from(stack);
            if (excavationTool == null || !PickaxeAreaMiningHandler.isEnabledFor(minecraft.player, excavationTool)) return;
            HitResult hit = minecraft.hitResult;
            if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;
            MiningMode mode = AstraliumPickaxeItem.getPreviewMode(stack, excavationTool);
            if (mode == MiningMode.NORMAL) return;
            List<BlockPos> positions = MiningPatternCalculator.calculate(blockHit.getBlockPos(), blockHit.getDirection(), minecraft.player.getDirection(), mode);
            PoseStack pose = event.getPoseStack();
            Vec3 camera = event.getCamera().getPosition();
            RenderSystem.lineWidth(com.hirensharma.astralium.AstraliumConfig.HIGHLIGHT_THICKNESS.get().floatValue());
            VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
            event.setCanceled(true);
            float[] color = highlightColor();
            for (BlockPos pos : positions) {
                if (!minecraft.level.isLoaded(pos)) continue;
                if (!pos.equals(blockHit.getBlockPos())
                    && !PickaxeAreaMiningHandler.canMineExtra(minecraft.player, pos, stack, excavationTool)) continue;
                VoxelShape shape = minecraft.level.getBlockState(pos).getShape(minecraft.level, pos);
                if (shape.isEmpty()) continue;
                AABB box = shape.bounds().move(pos).inflate(0.002D).move(-camera.x, -camera.y, -camera.z);
                LevelRenderer.renderLineBox(pose, lines, box, color[0], color[1], color[2], 0.92F);
            }
            RenderSystem.lineWidth(1.0F);
        }

        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) return;
            if (!(minecraft.player.getMainHandItem().getItem() instanceof BlockItem)) return;

            BlockPos target = com.hirensharma.astralium.VoidFloorHandler.floorPlacementTarget(minecraft.player);
            if (target == null) return;
            AABB targetBox = new AABB(target);
            if (com.hirensharma.astralium.VoidFloorHandler.blocksPlacement(minecraft.player, target)) return;

            PoseStack pose = event.getPoseStack();
            Vec3 camera = event.getCamera().getPosition();
            double surfaceY = target.getY() + 0.002D;
            AABB box = new AABB(
                target.getX() - 0.002D,
                surfaceY,
                target.getZ() - 0.002D,
                target.getX() + 1.002D,
                surfaceY,
                target.getZ() + 1.002D).move(-camera.x, -camera.y, -camera.z);
            VertexConsumer lines = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
            RenderSystem.lineWidth(1.0F);
            LevelRenderer.renderLineBox(pose, lines, box, 0.0F, 0.0F, 0.0F, 0.4F);
        }

        private static float[] highlightColor() {
            String value = com.hirensharma.astralium.AstraliumConfig.HIGHLIGHT_COLOR.get();
            try {
                int color = Integer.parseInt(value.startsWith("#") ? value.substring(1) : value, 16);
                return new float[] {((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F};
            } catch (NumberFormatException ignored) {
                return new float[] {0.56F, 0.85F, 1.0F};
            }
        }
    }
}
