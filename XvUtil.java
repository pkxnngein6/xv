package com.xv.addon;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class XvUtil {
    /** หา spawner ทั้งหมดใน chunk ที่โหลดอยู่รอบตัวผู้เล่น */
    public static Set<BlockPos> scanSpawners() {
        MinecraftClient mc = MinecraftClient.getInstance();
        Set<BlockPos> found = new HashSet<>();
        if (mc.player == null || mc.world == null) return found;

        int r = mc.options.getViewDistance().getValue();
        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;

        for (int cx = pcx - r; cx <= pcx + r; cx++) {
            for (int cz = pcz - r; cz <= pcz + r; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof MobSpawnerBlockEntity) found.add(be.getPos().toImmutable());
                }
            }
        }
        return found;
    }
}
