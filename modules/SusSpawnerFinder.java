package com.xv.addon.modules;

import com.xv.addon.SusAnalyzer;
import com.xv.addon.XvAddon;
import com.xv.addon.XvUtil;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SusSpawnerFinder extends Module {
    private static final Color SIDE = new Color(0, 0, 0, 120);
    private static final Color LINE = new Color(0, 0, 0, 255);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minChance = sgGeneral.add(new IntSetting.Builder()
        .name("min-chance")
        .description("Only notify and paint black when the sus chance is at least this percent.")
        .defaultValue(50)
        .range(1, 99)
        .sliderRange(1, 99)
        .build());

    private final Setting<Integer> analyzeRadius = sgGeneral.add(new IntSetting.Builder()
        .name("analyze-radius")
        .description("How many blocks around the spawner to inspect.")
        .defaultValue(6)
        .range(3, 10)
        .sliderRange(3, 10)
        .build());

    private final Setting<Boolean> chatNotify = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notify")
        .description("Send the sus percentage in chat.")
        .defaultValue(true)
        .build());

    private final Setting<Boolean> blackArea = sgGeneral.add(new BoolSetting.Builder()
        .name("black-area")
        .description("Paint the area around a sus spawner black.")
        .defaultValue(true)
        .build());

    private final Setting<Integer> areaRadius = sgGeneral.add(new IntSetting.Builder()
        .name("area-radius")
        .description("Size of the black area around the spawner (in blocks).")
        .defaultValue(3)
        .range(1, 8)
        .sliderRange(1, 8)
        .visible(blackArea::get)
        .build());

    private final Map<BlockPos, SusAnalyzer.Result> results = new HashMap<>();
    private final Map<BlockPos, Integer> lastReported = new HashMap<>();
    private int timer = 0;

    public SusSpawnerFinder() {
        super(XvAddon.CATEGORY, "sus-spawner-finder",
            "Estimates the chance a spawner is player-made (SUS) and paints the area black.");
    }

    @Override
    public void onActivate() {
        results.clear();
        lastReported.clear();
        timer = 40; // วิเคราะห์ทันทีตอนเปิด
        if (mc.player != null) info("Sus spawner finder on. Notifies at >= (highlight)%d%%(default).", minChance.get());
    }

    @Override
    public void onDeactivate() {
        results.clear();
        lastReported.clear();
        XvAddon.susCount = 0;
        XvAddon.susTop = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        if (++timer < 40) return; // ทุก 2 วินาที
        timer = 0;

        Set<BlockPos> found = XvUtil.scanSpawners();
        results.keySet().retainAll(found);

        int susCount = 0;
        int topChance = 0;
        BlockPos topPos = null;

        for (BlockPos pos : found) {
            SusAnalyzer.Result r = SusAnalyzer.analyze(pos, found, analyzeRadius.get());
            results.put(pos, r);

            if (r.chance >= minChance.get()) {
                susCount++;
                if (r.chance > topChance) {
                    topChance = r.chance;
                    topPos = pos;
                }

                Integer last = lastReported.get(pos);
                if (chatNotify.get() && (last == null || Math.abs(r.chance - last) >= 15)) {
                    lastReported.put(pos, r.chance);
                    warning("SUS spawner at (highlight)%d, %d, %d(default) - (highlight)%d%%(default) chance (%s)",
                        pos.getX(), pos.getY(), pos.getZ(), r.chance, r.label());
                    info("Why: %s", r.reasonText());
                }
            }
        }

        XvAddon.susCount = susCount;
        XvAddon.susTop = topPos == null ? null
            : topPos.getX() + ", " + topPos.getY() + ", " + topPos.getZ() + " (" + topChance + "%)";
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!blackArea.get()) return;
        double r = areaRadius.get();
        for (SusAnalyzer.Result res : results.values()) {
            if (res.chance < minChance.get()) continue;
            BlockPos p = res.pos;
            event.renderer.box(
                p.getX() - r, p.getY() - r, p.getZ() - r,
                p.getX() + 1 + r, p.getY() + 1 + r, p.getZ() + 1 + r,
                SIDE, LINE, ShapeMode.Both, 0);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        results.clear();
        lastReported.clear();
        XvAddon.susCount = 0;
        XvAddon.susTop = null;
    }
}
