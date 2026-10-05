package com.xv.addon.modules;

import com.xv.addon.XvAddon;
import com.xv.addon.XvUtil;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class SpawnerNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Shows how far the spawner is from you.")
        .defaultValue(true)
        .build());

    private final Set<BlockPos> notified = new HashSet<>();
    private int timer = 0;

    public SpawnerNotifier() {
        super(XvAddon.CATEGORY, "spawner-notifier", "Notifies you when a loaded chunk contains a spawner.");
    }

    @Override
    public void onActivate() {
        notified.clear();
        timer = 20; // สแกนทันทีตอนเปิด
        if (mc.player != null) info("Spawner scan started.");
    }

    @Override
    public void onDeactivate() {
        XvAddon.spawnerCount = 0;
        XvAddon.nearestSpawner = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (++timer < 20) return;
        timer = 0;

        Set<BlockPos> found = XvUtil.scanSpawners();

        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : found) {
            double d = mc.player.getBlockPos().getSquaredDistance(pos);
            if (d < best) { best = d; nearest = pos; }

            if (notified.add(pos)) {
                String dist = showDistance.get() ? String.format(" (%.0f blocks away)", Math.sqrt(d)) : "";
                info("Spawner found at (highlight)%d, %d, %d(default)%s.", pos.getX(), pos.getY(), pos.getZ(), dist);
            }
        }

        XvAddon.spawnerCount = found.size();
        XvAddon.nearestSpawner = nearest == null ? null
            : nearest.getX() + ", " + nearest.getY() + ", " + nearest.getZ()
              + String.format(" (%.0fm)", Math.sqrt(best));
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        notified.clear();
        XvAddon.spawnerCount = 0;
        XvAddon.nearestSpawner = null;
    }
}
