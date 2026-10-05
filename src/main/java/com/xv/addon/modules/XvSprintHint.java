package com.xv.addon.modules;

import com.xv.addon.XvAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

/** เตือนเมื่อค่าความหิวต่ำ (QoL เล็ก ๆ) */
public class XvSprintHint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("hunger-threshold")
        .description("Warn when food level is at or below this value.")
        .defaultValue(6)
        .range(1, 20)
        .sliderRange(1, 20)
        .build());

    private boolean warned = false;

    public XvSprintHint() {
        super(XvAddon.CATEGORY, "hunger-warning", "Warns you in chat when you are getting hungry.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        int food = mc.player.getHungerManager().getFoodLevel();
        if (food <= threshold.get()) {
            if (!warned) {
                warning("Hunger is low (%d/20).", food);
                warned = true;
            }
        } else {
            warned = false;
        }
    }
}
