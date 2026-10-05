package com.xv.addon.hud;

import com.xv.addon.XvAddon;
import com.xv.addon.modules.SpawnerNotifier;
import com.xv.addon.modules.SusSpawnerFinder;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class XvHud extends HudElement {
    public static final HudElementInfo<XvHud> INFO =
        new HudElementInfo<>(XvAddon.HUD_GROUP, "xv-info", "Shows hunger, last death and spawners.", XvHud::new);

    private static final Color RED = new Color(255, 85, 85);
    private static final Color YELLOW = new Color(255, 215, 85);

    public XvHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        MinecraftClient mc = MinecraftClient.getInstance();
        List<String> lines = new ArrayList<>();
        List<Color> colors = new ArrayList<>();

        lines.add("XV ADDON");
        colors.add(YELLOW);

        if (mc.player != null) {
            int food = mc.player.getHungerManager().getFoodLevel();
            lines.add("Hunger: " + food + "/20");
            colors.add(food <= 6 ? RED : Color.WHITE);
        } else {
            lines.add("Hunger: -");
            colors.add(Color.WHITE);
        }

        String death = XvAddon.lastDeath;
        lines.add("Last death: " + (death == null ? "none" : death));
        colors.add(Color.WHITE);

        if (Modules.get().isActive(SpawnerNotifier.class)) {
            String nearest = XvAddon.nearestSpawner;
            lines.add("Spawners: " + XvAddon.spawnerCount + (nearest == null ? "" : "  nearest " + nearest));
        } else {
            lines.add("Spawners: (module off)");
        }
        colors.add(Color.WHITE);

        if (Modules.get().isActive(SusSpawnerFinder.class)) {
            String top = XvAddon.susTop;
            lines.add("Sus: " + XvAddon.susCount + (top == null ? "" : "  top " + top));
            colors.add(XvAddon.susCount > 0 ? RED : Color.WHITE);
        } else {
            lines.add("Sus: (module off)");
            colors.add(Color.WHITE);
        }

        double width = 0;
        for (String s : lines) width = Math.max(width, renderer.textWidth(s, true));
        double lineHeight = renderer.textHeight(true) + 2;
        setSize(width, lineHeight * lines.size());

        double yy = y;
        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, yy, colors.get(i), true);
            yy += lineHeight;
        }
    }
}
