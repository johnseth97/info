package com.johnseth97.info;

import com.johnseth97.info.command.InfoCommand;
import com.johnseth97.info.config.InfoConfig;
import com.johnseth97.info.service.InfoHudService;
import org.bukkit.plugin.java.JavaPlugin;

public class InfoPlugin extends JavaPlugin {

    private InfoConfig infoConfig;
    private InfoHudService hudService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        infoConfig = new InfoConfig(getConfig());

        hudService = new InfoHudService(this, infoConfig);
        hudService.start();

        InfoCommand cmd = new InfoCommand(this, hudService);
        getCommand("info").setExecutor(cmd);
        getCommand("info").setTabCompleter(cmd);

        getLogger().info("Info plugin enabled. Max distance: " + infoConfig.maxDistance
                + ", interval: " + infoConfig.updateIntervalTicks + " ticks.");
    }

    @Override
    public void onDisable() {
        if (hudService != null) hudService.stop();
        getLogger().info("Info plugin disabled.");
    }

    public InfoConfig getInfoConfig() {
        return infoConfig;
    }

    public void reloadInfoConfig() {
        reloadConfig();
        infoConfig = new InfoConfig(getConfig());
        hudService.reload(infoConfig);
    }
}
