package com.xv.addon.modules;

import com.xv.addon.XvAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class DeathCoords extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> showDimension = sgGeneral.add(new BoolSetting.Builder()
        .name("show-dimension")
        .description("Also prints the dimension you died in.")
        .defaultValue(true)
        .build());

    private boolean wasDead = false;

    public DeathCoords() {
        super(XvAddon.CATEGORY, "death-coords", "Prints your coordinates in chat when you die.");
    }

    @Override
    public void onActivate() {
        wasDead = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        boolean dead = mc.player.getHealth() <= 0;
        if (dead && !wasDead) {
            int x = mc.player.getBlockX(), y = mc.player.getBlockY(), z = mc.player.getBlockZ();
            String dim = mc.world.getRegistryKey().getValue().toString();
            XvAddon.lastDeath = x + ", " + y + ", " + z + (showDimension.get() ? " (" + dim + ")" : "");
            info("You died at (highlight)%d, %d, %d(default) in %s.", x, y, z, dim);
        }
        wasDead = dead;
    }
}
