package com.xv.addon.hud;

import com.xv.addon.XvAddon;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class XvHud extends HudElement {
    public static final HudElementInfo<XvHud> INFO =
        new HudElementInfo<>(XvAddon.HUD_GROUP, "xv-watermark", "Shows the XV ADDON watermark.", XvHud::new);

    private static final String TEXT = "XV ADDON";

    public XvHud() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        setSize(renderer.textWidth(TEXT, true), renderer.textHeight(true));
        renderer.text(TEXT, x, y, Color.WHITE, true);
    }
}
