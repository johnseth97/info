package com.johnseth97.info.service;

import com.johnseth97.info.config.InfoConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class InfoHudService {

  private final Plugin plugin;
  private final TargetInfoService targetInfoService;
  private final Map<UUID, Boolean> enabledOverrides = new HashMap<>();

  private BukkitTask task;
  private InfoConfig config;

  public InfoHudService(
    Plugin plugin,
    InfoConfig config,
    TargetInfoService targetInfoService
  ) {
    this.plugin = plugin;
    this.config = config;
    this.targetInfoService = targetInfoService;
  }

  public void start() {
    if (task != null) task.cancel();

    task = Bukkit.getScheduler().runTaskTimer(
      plugin,
      this::tick,
      0L,
      Math.max(1L, config.updateIntervalTicks)
    );
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

    boolean nowEnabled = !isEnabled(player);
    enabledOverrides.put(id, nowEnabled);

    if (!nowEnabled) {
      player.sendActionBar(Component.empty());
    }

    return nowEnabled;
  }

  public boolean isEnabled(Player player) {
    Boolean override = enabledOverrides.get(player.getUniqueId());
    return override != null ? override : config.enabledByDefault;
  }

  private void tick() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!player.hasPermission("visualid.use")) {
        player.sendActionBar(Component.empty());
        continue;
      }

      if (!isEnabled(player)) {
        player.sendActionBar(Component.empty());
        continue;
      }

      Component component = targetInfoService.getTargetComponent(
        player,
        config
      );
      if (component != null) {
        player.sendActionBar(component);
      } else {
        player.sendActionBar(Component.empty());
      }
    }
  }
}
