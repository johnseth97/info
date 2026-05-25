package com.johnseth97.visualid.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Immutable snapshot of config.yml values with basic validation/clamping.
 */
public class VisualIdConfig {

    public final boolean enabledByDefault;
    public final long updateIntervalTicks;
    public final double maxDistance;
    public final boolean preferEntities;

    public final boolean showMaterialName;
    public final boolean showNamespacedKey;
    public final boolean showCoordinates;
    public final boolean showBiome;
    public final boolean showLightLevel;
    public final boolean showEntityHealth;

    public VisualIdConfig(FileConfiguration cfg) {
        this.enabledByDefault   = cfg.getBoolean("enabled-by-default", true);
        this.updateIntervalTicks = Math.max(1, cfg.getLong("update-interval-ticks", 5));
        this.maxDistance        = Math.min(32.0, Math.max(1.0, cfg.getDouble("max-distance", 8.0)));
        this.preferEntities     = cfg.getBoolean("prefer-entities", true);

        this.showMaterialName   = cfg.getBoolean("show.material-name", true);
        this.showNamespacedKey  = cfg.getBoolean("show.namespaced-key", true);
        this.showCoordinates    = cfg.getBoolean("show.coordinates", true);
        this.showBiome          = cfg.getBoolean("show.biome", false);
        this.showLightLevel     = cfg.getBoolean("show.light-level", false);
        this.showEntityHealth   = cfg.getBoolean("show.entity-health", true);
    }
}
