package com.johnseth97.info.service;

import com.johnseth97.info.config.InfoConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InfoHudService {

    private final Plugin plugin;
    private final TargetInfoService targetInfoService = new TargetInfoService();

    /** UUIDs of players who have the HUD disabled (flipped when enabledByDefault=true). */
    private final Set<UUID> disabledPlayers = new HashSet<>();

    private BukkitTask task;
    private InfoConfig config;

    public InfoHudService(Plugin plugin, InfoConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void start() {
        if (task != null) task.cancel();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, config.updateIntervalTicks);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void reload(InfoConfig newConfig) {
        this.config = newConfig;
        start();
    }

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (config.enabledByDefault) {
            if (disabledPlayers.contains(id)) {
                disabledPlayers.remove(id);
                return true;  // now enabled
            } else {
                disabledPlayers.add(id);
                return false; // now disabled
            }
        } else {
            if (disabledPlayers.contains(id)) {
                disabledPlayers.remove(id);
                return false; // now disabled (remove from "enabled" set)
            } else {
                disabledPlayers.add(id);
                return true;  // now enabled
            }
        }
    }

    public boolean isEnabled(Player player) {
        boolean inSet = disabledPlayers.contains(player.getUniqueId());
        return config.enabledByDefault != inSet;
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("info.use")) continue;
            if (!isEnabled(player)) continue;

            String text = targetInfoService.getTargetText(player, config);
            if (text != null && !text.isBlank()) {
                player.sendActionBar(Component.text(text));
            }
        }
    }
}
