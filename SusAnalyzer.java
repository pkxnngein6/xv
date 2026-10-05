package com.xv.addon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ประเมินว่า spawner ตัวนี้ "SUS" (น่าจะมีผู้เล่นวาง/สร้างฐานอยู่) กี่เปอร์เซ็นต์
 * เป็นระบบให้คะแนนตามกฎ (heuristic) ดูจากสิ่งที่อยู่รอบ ๆ spawner ไม่ใช่ AI/ML จริง ๆ
 */
public class SusAnalyzer {
    public static class Result {
        public final BlockPos pos;
        public final int chance;
        public final List<String> reasons;

        public Result(BlockPos pos, int chance, List<String> reasons) {
            this.pos = pos;
            this.chance = chance;
            this.reasons = reasons;
        }

        public String label() {
            if (chance >= 70) return "HIGH";
            if (chance >= 40) return "MEDIUM";
            return "LOW";
        }

        public String reasonText() {
            if (reasons.isEmpty()) return "no strong signs";
            return String.join(", ", reasons);
        }
    }

    public static Result analyze(BlockPos spawner, Set<BlockPos> allSpawners, int radius) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        List<String> reasons = new ArrayList<>();
        if (world == null) return new Result(spawner, 0, reasons);

        boolean nether = World.NETHER.equals(world.getRegistryKey());

        int mossy = 0, cobweb = 0, netherBricks = 0, portalFrame = 0, torches = 0, crafted = 0;

        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    m.set(spawner.getX() + dx, spawner.getY() + dy, spawner.getZ() + dz);
                    BlockState state = world.getBlockState(m);
                    if (state.isAir()) continue;
                    Block b = state.getBlock();

                    if (b == Blocks.MOSSY_COBBLESTONE) mossy++;
                    else if (b == Blocks.COBWEB) cobweb++;
                    else if (b == Blocks.NETHER_BRICKS) netherBricks++;
                    else if (b == Blocks.END_PORTAL_FRAME) portalFrame++;
                    else if (b == Blocks.TORCH || b == Blocks.WALL_TORCH
                        || b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH
                        || b == Blocks.LANTERN || b == Blocks.SOUL_LANTERN
                        || b == Blocks.CAMPFIRE || b == Blocks.SOUL_CAMPFIRE) torches++;
                    else if (b == Blocks.CRAFTING_TABLE || b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE
                        || b == Blocks.SMOKER || b == Blocks.ANVIL || b == Blocks.ENCHANTING_TABLE
                        || b == Blocks.BEACON || b == Blocks.GLASS || b == Blocks.HOPPER
                        || b == Blocks.IRON_BLOCK) crafted++;
                }
            }
        }

        // block entity ที่มักเป็นของผู้เล่น (หีบ ถัง เตา ฯลฯ) ใกล้ spawner
        int storage = 0;
        WorldChunk chunk = world.getWorldChunk(spawner);
        if (chunk != null) {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (!be.getPos().isWithinDistance(spawner, 16)) continue;
                if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity
                    || be instanceof HopperBlockEntity || be instanceof ShulkerBoxBlockEntity
                    || be instanceof AbstractFurnaceBlockEntity || be instanceof EnderChestBlockEntity
                    || be instanceof BeaconBlockEntity || be instanceof SignBlockEntity) {
                    storage++;
                }
            }
        }

        // spawner ตัวอื่นที่อยู่ใกล้ ๆ (ฟาร์ม/กอง spawner)
        int near8 = 0, near16 = 0;
        for (BlockPos other : allSpawners) {
            if (other.equals(spawner)) continue;
            if (other.isWithinDistance(spawner, 8)) near8++;
            else if (other.isWithinDistance(spawner, 16)) near16++;
        }

        int light = world.getLightLevel(LightType.BLOCK, spawner);

        int score = 25; // ค่าเริ่มต้น

        // --- หลักฐานว่าเป็นธรรมชาติ (ลดคะแนน) ---
        boolean natural = false;
        if (mossy >= 6) {
            score -= 35;
            natural = true;
            reasons.add("dungeon-like mossy cobblestone (" + mossy + ")");
        }
        if (cobweb >= 3) {
            score -= 30;
            natural = true;
            reasons.add("cobwebs, mineshaft-like (" + cobweb + ")");
        }
        if (nether && netherBricks >= 12) {
            score -= 35;
            natural = true;
            reasons.add("nether fortress bricks (" + netherBricks + ")");
        }
        if (portalFrame > 0) {
            score -= 40;
            natural = true;
            reasons.add("end portal frame, stronghold-like");
        }

        // --- หลักฐานว่าผู้เล่นทำ (เพิ่มคะแนน) ---
        if (torches >= 1) {
            score += torches >= 4 ? 40 : 25;
            reasons.add("torches/lanterns nearby (" + torches + ")");
        }
        if (!nether && light >= 8) {
            score += 10;
            reasons.add("bright at spawner (light " + light + ")");
        }
        if (crafted >= 1) {
            score += 15;
            reasons.add("player-crafted blocks (" + crafted + ")");
        }
        if (storage >= 3) {
            score += storage >= 8 ? 30 : 20;
            reasons.add("chests/hoppers/furnaces nearby (" + storage + ")");
        }
        if (near8 >= 1) {
            score += 25;
            reasons.add("other spawners within 8 blocks (" + near8 + ")");
        } else if (near16 >= 1) {
            score += 10;
            reasons.add("other spawner within 16 blocks");
        }
        if (!natural) {
            score += 20;
            reasons.add("no natural structure around");
        }

        int chance = Math.max(3, Math.min(97, score));
        return new Result(spawner, chance, reasons);
    }
}
