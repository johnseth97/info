package com.johnseth97.visualid.command;

import com.johnseth97.visualid.VisualIdPlugin;
import com.johnseth97.visualid.service.TargetInfoService;
import com.johnseth97.visualid.service.VisualIdHudService;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Handles the /visual-id command and its subcommands.
 */
public class VisualIdCommand extends Command implements CommandExecutor, TabCompleter {

    // Re-send the one-shot action bar a few times to keep it visible.
    private static final int ONE_SHOT_MULTIPLIER = 3;

    // Maps short section names (used in the command) to config.yml paths.
    private static final Map<String, String> SECTIONS = Map.of(
            "name",        "show.material-name",
            "key",         "show.namespaced-key",
            "coordinates", "show.coordinates",
            "biome",       "show.biome",
            "light",       "show.light-level",
            "health",      "show.entity-health"
    );

    private final VisualIdPlugin plugin;
    private final VisualIdHudService hudService;
    private final TargetInfoService targetInfoService;

    public VisualIdCommand(String name, VisualIdPlugin plugin, VisualIdHudService hudService, TargetInfoService targetInfoService) {
        super(name);
        setDescription("Visual Identification HUD.");
        setUsage("/" + name + " [toggle|status|reload|config]");
        setPermission("visualid.use");
        this.plugin = plugin;
        this.hudService = hudService;
        this.targetInfoService = targetInfoService;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            handleOneShot(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> handleToggle(sender);
            case "status" -> handleStatus(sender, label);
            case "reload" -> handleReload(sender);
            case "config" -> handleConfig(sender, label, args);
            default       -> sendHelp(sender, label);
        }
        return true;
    }

    // ── Subcommands ────────────────────────────────────────────────────────────

    private void handleOneShot(CommandSender sender, String label) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }
        if (!player.hasPermission("visualid.use")) {
            player.sendMessage("You don't have permission to do that.");
            return;
        }
        Component result = targetInfoService.getTargetComponent(player, plugin.getInfoConfig());
        if (result == null) {
            player.sendMessage("[VisualID] Not looking at anything.");
            return;
        }
        // Send immediately, then refresh for a few intervals to extend visibility.
        player.sendActionBar(result);
        long interval = plugin.getInfoConfig().updateIntervalTicks;
        for (int i = 1; i < ONE_SHOT_MULTIPLIER; i++) {
            long delay = interval * i;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.sendActionBar(result), delay);
        }
    }

    private void handleToggle(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can toggle the HUD.");
            return;
        }
        if (!player.hasPermission("visualid.toggle")) {
            player.sendMessage("You don't have permission to do that.");
            return;
        }
        hudService.toggle(player);
    }

    private void handleStatus(CommandSender sender, String label) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can check HUD status.");
            return;
        }
        var cfg = plugin.getInfoConfig();
        boolean on = hudService.isEnabled(player);
        sender.sendMessage("[VisualID] HUD: " + (on ? "enabled" : "disabled")
                + " | distance: " + cfg.maxDistance
                + " | interval: " + cfg.updateIntervalTicks + " ticks");
        sender.sendMessage("Sections — name:" + flag(cfg.showMaterialName)
                + " key:" + flag(cfg.showNamespacedKey)
                + " coords:" + flag(cfg.showCoordinates)
                + " biome:" + flag(cfg.showBiome)
                + " light:" + flag(cfg.showLightLevel)
                + " health:" + flag(cfg.showEntityHealth));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("visualid.reload")) {
            sender.sendMessage("You don't have permission to do that.");
            return;
        }
        plugin.reloadInfoConfig();
        sender.sendMessage("[VisualID] Config reloaded.");
    }

    private void handleConfig(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("visualid.reload")) {
            sender.sendMessage("You don't have permission to do that.");
            return;
        }
        if (args.length < 3) {
            sendConfigHelp(sender, label);
            return;
        }

        String section = args[1].toLowerCase();
        String action  = args[2].toLowerCase();

        String configPath = SECTIONS.get(section);
        if (configPath == null) {
            sender.sendMessage("[VisualID] Unknown section '" + section + "'. Options: " + String.join(", ", SECTIONS.keySet()));
            return;
        }

        boolean enable = switch (action) {
            case "enable", "on", "true"    -> true;
            case "disable", "off", "false" -> false;
            default -> {
                sender.sendMessage("[VisualID] Use 'enable' or 'disable'.");
                yield plugin.getConfig().getBoolean(configPath);
            }
        };

        plugin.getConfig().set(configPath, enable);
        plugin.saveConfig();
        plugin.applyInfoConfig();
        sender.sendMessage("[VisualID] " + section + " " + (enable ? "enabled" : "disabled") + " and saved.");
    }

    // ── Help ───────────────────────────────────────────────────────────────────

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("/" + label + "                              - Show current target once");
        sender.sendMessage("/" + label + " toggle                       - Enable/disable your HUD");
        sender.sendMessage("/" + label + " status                       - Show HUD state and section flags");
        sender.sendMessage("/" + label + " config <section> enable|disable  - Toggle a display section");
        sender.sendMessage("/" + label + " reload                       - Reload config (admin)");
        sender.sendMessage("Sections: " + String.join(", ", SECTIONS.keySet()));
    }

    private void sendConfigHelp(CommandSender sender, String label) {
        sender.sendMessage("Usage: /" + label + " config <section> enable|disable");
        sender.sendMessage("Sections: " + String.join(", ", SECTIONS.keySet()));
    }

    // ── CommandExecutor / TabCompleter bridge (for PluginCommand wiring) ───────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return tabComplete(sender, label, args);
    }

    // ── Tab completion ─────────────────────────────────────────────────────────

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("toggle", "status", "reload", "config").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("config")) {
            return SECTIONS.keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .sorted()
                    .toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            return List.of("enable", "disable").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    // ── Util ───────────────────────────────────────────────────────────────────

    private String flag(boolean on) {
        return on ? "on" : "off";
    }
}
