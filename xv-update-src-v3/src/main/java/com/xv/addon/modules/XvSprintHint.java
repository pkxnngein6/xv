package com.xv.addon.modules;

import com.xv.addon.XvAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class XvSprintHint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("hunger-threshold")
        .description("Warn when food level is at or below this value (20 = always warn, good for testing).")
        .defaultValue(6)
        .range(1, 20)
        .sliderRange(1, 20)
        .build());

    private final Setting<Integer> repeatSeconds = sgGeneral.add(new IntSetting.Builder()
        .name("repeat-seconds")
        .description("Repeat the warning every N seconds while you stay hungry.")
        .defaultValue(10)
        .range(1, 120)
        .sliderRange(1, 60)
        .build());

    private int timer = 0;

    public XvSprintHint() {
        super(XvAddon.CATEGORY, "hunger-warning", "Warns you in chat when you are getting hungry.");
    }

    @Override
    public void onActivate() {
        timer = 0; // ถ้าหิวอยู่แล้ว เตือนทันทีตอนเปิด
        if (mc.player != null) {
            info("Hunger warning on. Now: (highlight)%d(default)/20, warns at <= (highlight)%d(default).",
                mc.player.getHungerManager().getFoodLevel(), threshold.get());
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        int food = mc.player.getHungerManager().getFoodLevel();
        if (food <= threshold.get()) {
            if (timer <= 0) {
                warning("Hunger is low (%d/20).", food);
                timer = repeatSeconds.get() * 20;
            } else {
                timer--;
            }
        } else {
            timer = 0;
        }
    }
}
