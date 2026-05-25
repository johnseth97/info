package com.johnseth97.info.command;

import com.johnseth97.info.InfoPlugin;
import com.johnseth97.info.service.InfoHudService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class InfoCommand implements CommandExecutor, TabCompleter {

    private final InfoPlugin plugin;
    private final InfoHudService hudService;

    public InfoCommand(InfoPlugin plugin, InfoHudService hudService) {
        this.plugin = plugin;
        this.hudService = hudService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can toggle the HUD.");
                    return true;
                }
                if (!player.hasPermission("info.toggle")) {
                    player.sendMessage("You don't have permission to do that.");
                    return true;
                }
                boolean nowEnabled = hudService.toggle(player);
                player.sendMessage("[Info] HUD " + (nowEnabled ? "enabled" : "disabled") + ".");
            }
            case "status" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can check HUD status.");
                    return true;
                }
                boolean enabled = hudService.isEnabled(player);
                sender.sendMessage("[Info] HUD is " + (enabled ? "enabled" : "disabled") + "."
                        + " Distance: " + plugin.getInfoConfig().maxDistance
                        + " | Interval: " + plugin.getInfoConfig().updateIntervalTicks + " ticks");
            }
            case "reload" -> {
                if (!sender.hasPermission("info.reload")) {
                    sender.sendMessage("You don't have permission to do that.");
                    return true;
                }
                plugin.reloadInfoConfig();
                sender.sendMessage("[Info] Config reloaded.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("toggle", "status", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("/info toggle  - Enable/disable your HUD");
        sender.sendMessage("/info status  - Show current HUD status");
        sender.sendMessage("/info reload  - Reload config (admin)");
    }
}
