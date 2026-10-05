package com.xv.addon;

import com.xv.addon.commands.XvCommand;
import com.xv.addon.hud.XvHud;
import com.xv.addon.modules.DeathCoords;
import com.xv.addon.modules.SpawnerNotifier;
import com.xv.addon.modules.SusSpawnerFinder;
import com.xv.addon.modules.XvSprintHint;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XvAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("XV ADDON");
    public static final Category CATEGORY = new Category("XV ADDON");
    public static final HudGroup HUD_GROUP = new HudGroup("XV ADDON");

    // ข้อมูลที่โมดูลเขียน แล้ว HUD เอาไปแสดง
    public static volatile String lastDeath = null;
    public static volatile int spawnerCount = 0;
    public static volatile String nearestSpawner = null;
    public static volatile int susCount = 0;
    public static volatile String susTop = null;

    @Override
    public void onInitialize() {
        LOG.info("Initializing XV ADDON");

        Modules.get().add(new DeathCoords());
        Modules.get().add(new XvSprintHint());
        Modules.get().add(new SpawnerNotifier());
        Modules.get().add(new SusSpawnerFinder());
        Commands.add(new XvCommand());
        Hud.get().register(XvHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.xv.addon";
    }
}
