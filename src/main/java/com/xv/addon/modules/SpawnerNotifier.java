package com.xv.addon.modules;

import com.xv.addon.XvAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

/** แจ้งเตือนในแชทเมื่อ chunk ที่โหลดมี spawner (ใช้ตรวจสอบเซิร์ฟของตัวเอง) */
public class SpawnerNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Shows how far the spawner is from you.")
        .defaultValue(true)
        .build());

    private final Set<BlockPos> notified = new HashSet<>();

    public SpawnerNotifier() {
        super(XvAddon.CATEGORY, "spawner-notifier", "Notifies you in chat when a loaded chunk contains a spawner.");
    }

    @Override
    public void onActivate() {
        notified.clear();
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        if (mc.player == null) return;

        for (BlockEntity be : event.chunk.getBlockEntities().values()) {
            if (!(be instanceof MobSpawnerBlockEntity)) continue;

            BlockPos pos = be.getPos().toImmutable();
            if (!notified.add(pos)) continue;

            String dist = showDistance.get()
                ? String.format(" (%.0f blocks away)", Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos)))
                : "";
            info("Spawner found at (highlight)%d, %d, %d(default)%s.", pos.getX(), pos.getY(), pos.getZ(), dist);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        notified.clear();
    }
}
