package com.johnseth97.info.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfoConfigTest {

    private static YamlConfiguration cfg(Object... pairs) {
        YamlConfiguration c = new YamlConfiguration();
        for (int i = 0; i < pairs.length; i += 2) {
            c.set((String) pairs[i], pairs[i + 1]);
        }
        return c;
    }

    @Test
    void defaults_whenConfigIsEmpty() {
        InfoConfig c = new InfoConfig(new YamlConfiguration());
        assertTrue(c.enabledByDefault);
        assertEquals(5L, c.updateIntervalTicks);
        assertEquals(8.0, c.maxDistance);
        assertTrue(c.preferEntities);
        assertTrue(c.showMaterialName);
        assertTrue(c.showNamespacedKey);
        assertTrue(c.showCoordinates);
        assertFalse(c.showBiome);
        assertFalse(c.showLightLevel);
        assertTrue(c.showEntityHealth);
    }

    @Test
    void customValues_areReadCorrectly() {
        InfoConfig c = new InfoConfig(cfg(
            "enabled-by-default",    false,
            "update-interval-ticks", 20L,
            "max-distance",          16.0,
            "prefer-entities",       false,
            "show.biome",            true,
            "show.light-level",      true,
            "show.entity-health",    false
        ));
        assertFalse(c.enabledByDefault);
        assertEquals(20L, c.updateIntervalTicks);
        assertEquals(16.0, c.maxDistance);
        assertFalse(c.preferEntities);
        assertTrue(c.showBiome);
        assertTrue(c.showLightLevel);
        assertFalse(c.showEntityHealth);
    }

    @Test
    void maxDistance_isClampedToUpperBound() {
        assertEquals(32.0, new InfoConfig(cfg("max-distance", 999.0)).maxDistance);
    }

    @Test
    void maxDistance_isClampedToLowerBound() {
        assertEquals(1.0, new InfoConfig(cfg("max-distance", 0.0)).maxDistance);
    }

    @Test
    void maxDistance_atBoundaryValues_isUnchanged() {
        assertEquals(1.0,  new InfoConfig(cfg("max-distance", 1.0)).maxDistance);
        assertEquals(32.0, new InfoConfig(cfg("max-distance", 32.0)).maxDistance);
    }

    @Test
    void updateIntervalTicks_zeroIsClampedToOne() {
        assertEquals(1L, new InfoConfig(cfg("update-interval-ticks", 0L)).updateIntervalTicks);
    }

    @Test
    void updateIntervalTicks_negativeIsClampedToOne() {
        assertEquals(1L, new InfoConfig(cfg("update-interval-ticks", -10L)).updateIntervalTicks);
    }

    @Test
    void updateIntervalTicks_validValueIsUnchanged() {
        assertEquals(10L, new InfoConfig(cfg("update-interval-ticks", 10L)).updateIntervalTicks);
    }
}
