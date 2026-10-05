package com.xv.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.xv.addon.SusAnalyzer;
import com.xv.addon.XvAddon;
import com.xv.addon.XvUtil;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class XvCommand extends Command {
    public XvCommand() {
        super("xv", "Shows info about XV ADDON.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            info("XV ADDON v1.0.0");
            info("Commands: (highlight).xv status(default), (highlight).xv scan(default), (highlight).xv death(default), (highlight).xv sus(default)");
            return SINGLE_SUCCESS;
        });

        // .xv status  -> ดูความหิว / ที่ตายล่าสุด / จำนวน spawner ใกล้ตัว
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("status").executes(context -> {
            if (mc.player == null) {
                error("Join a world first.");
                return SINGLE_SUCCESS;
            }
            int food = mc.player.getHungerManager().getFoodLevel();
            info("Hunger: (highlight)%d(default)/20", food);
            info("Last death: (highlight)%s", XvAddon.lastDeath == null ? "none" : XvAddon.lastDeath);
            info("Spawners nearby: (highlight)%d", XvUtil.scanSpawners().size());
            return SINGLE_SUCCESS;
        }));

        // .xv scan  -> สแกนหา spawner ตอนนี้เลย (ไม่ต้องเปิดโมดูล)
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("scan").executes(context -> {
            if (mc.player == null) {
                error("Join a world first.");
                return SINGLE_SUCCESS;
            }
            Set<BlockPos> found = XvUtil.scanSpawners();
            if (found.isEmpty()) {
                info("No spawners in loaded chunks.");
                return SINGLE_SUCCESS;
            }
            int shown = 0;
            for (BlockPos pos : found) {
                double dist = Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos));
                info("Spawner at (highlight)%d, %d, %d(default) (%.0f blocks away)", pos.getX(), pos.getY(), pos.getZ(), dist);
                if (++shown >= 10) break;
            }
            if (found.size() > shown) info("...and %d more.", found.size() - shown);
            return SINGLE_SUCCESS;
        }));

        // .xv sus  -> ประเมินความ SUS ของ spawner รอบตัวตอนนี้เลย (ไม่ต้องเปิดโมดูล)
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("sus").executes(context -> {
            if (mc.player == null) {
                error("Join a world first.");
                return SINGLE_SUCCESS;
            }
            Set<BlockPos> found = XvUtil.scanSpawners();
            if (found.isEmpty()) {
                info("No spawners in loaded chunks.");
                return SINGLE_SUCCESS;
            }
            List<SusAnalyzer.Result> list = new ArrayList<>();
            for (BlockPos pos : found) list.add(SusAnalyzer.analyze(pos, found, 6));
            list.sort((a, b) -> Integer.compare(b.chance, a.chance));

            int shown = 0;
            for (SusAnalyzer.Result r : list) {
                double dist = Math.sqrt(mc.player.getBlockPos().getSquaredDistance(r.pos));
                info("(highlight)%d%%(default) %s at (highlight)%d, %d, %d(default) (%.0f blocks)",
                    r.chance, r.label(), r.pos.getX(), r.pos.getY(), r.pos.getZ(), dist);
                info("  Why: %s", r.reasonText());
                if (++shown >= 8) break;
            }
            return SINGLE_SUCCESS;
        }));

        // .xv death  -> พิกัดที่ตายล่าสุด
        builder.then(LiteralArgumentBuilder.<CommandSource>literal("death").executes(context -> {
            if (XvAddon.lastDeath == null) info("You haven't died yet (or death-coords was off).");
            else info("Last death: (highlight)%s", XvAddon.lastDeath);
            return SINGLE_SUCCESS;
        }));
    }
}
