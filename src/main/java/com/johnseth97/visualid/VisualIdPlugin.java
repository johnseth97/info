package com.johnseth97.visualid;

import com.johnseth97.visualid.command.VisualIdCommand;
import com.johnseth97.visualid.config.VisualIdConfig;
import com.johnseth97.visualid.service.TargetInfoService;
import com.johnseth97.visualid.service.VisualIdHudService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

/**
 * Main plugin entry point that wires config, services, and command registration.
 */
public class VisualIdPlugin extends JavaPlugin {

    private VisualIdConfig infoConfig;
    private VisualIdHudService hudService;
    private TargetInfoService targetInfoService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        infoConfig = new VisualIdConfig(getConfig());

        targetInfoService = new TargetInfoService();
        hudService = new VisualIdHudService(this, infoConfig, targetInfoService);
        hudService.start();

        String keyword = getConfig().getString("command", "visual-id");
        VisualIdCommand cmd = new VisualIdCommand(keyword, this, hudService, targetInfoService);

        // Always wire the plugin.yml-declared "visual-id" command — this ensures
        // Brigadier pushes it to the client so the command works correctly.
        getCommand("visual-id").setExecutor(cmd);
        getCommand("visual-id").setTabCompleter(cmd);

        // If the admin configured a different keyword, register that alias too.
        if (!keyword.equalsIgnoreCase("visual-id")) {
            registerDynamic(keyword, cmd);
            getLogger().info("Also registered alias '/" + keyword + "'.");
        }

        getLogger().info("Ready. Distance: " + infoConfig.maxDistance
                + ", interval: " + infoConfig.updateIntervalTicks + " ticks.");
    }

    @Override
    public void onDisable() {
        if (hudService != null) hudService.stop();
    }

    public VisualIdConfig getInfoConfig() {
        return infoConfig;
    }

    public void applyInfoConfig() {
        // Rebuild the config snapshot and restart the HUD with the new settings.
        infoConfig = new VisualIdConfig(getConfig());
        hudService.reload(infoConfig);
    }

    public void reloadInfoConfig() {
        reloadConfig();
        applyInfoConfig();
    }

    private void registerDynamic(String name, VisualIdCommand cmd) {
        // Bukkit doesn't expose CommandMap directly, so we use reflection for aliases.
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
            commandMap.register(getName().toLowerCase(), cmd);
        } catch (Exception e) {
            getLogger().severe("Failed to register alias '/" + name + "': " + e.getMessage());
        }
    }
}
