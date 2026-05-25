package com.johnseth97.info.service;

import com.johnseth97.info.config.InfoConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfoHudServiceTest {

    @Mock Player player;

    private static InfoConfig configWith(boolean enabledByDefault) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("enabled-by-default", enabledByDefault);
        return new InfoConfig(cfg);
    }

    @BeforeEach
    void setUp() {
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    // ── enabledByDefault = true ────────────────────────────────────────────────

    @Test
    void isEnabled_trueByDefault_startsEnabled() {
        InfoHudService svc = new InfoHudService(null, configWith(true), null);
        assertTrue(svc.isEnabled(player));
    }

    @Test
    void toggle_trueByDefault_firstToggleDisables() {
        InfoHudService svc = new InfoHudService(null, configWith(true), null);
        assertFalse(svc.toggle(player));
        assertFalse(svc.isEnabled(player));
    }

    @Test
    void toggle_trueByDefault_secondToggleRestores() {
        InfoHudService svc = new InfoHudService(null, configWith(true), null);
        svc.toggle(player);
        assertTrue(svc.toggle(player));
        assertTrue(svc.isEnabled(player));
    }

    // ── enabledByDefault = false ───────────────────────────────────────────────

    @Test
    void isEnabled_falseByDefault_startsDisabled() {
        InfoHudService svc = new InfoHudService(null, configWith(false), null);
        assertFalse(svc.isEnabled(player));
    }

    @Test
    void toggle_falseByDefault_firstToggleEnables() {
        InfoHudService svc = new InfoHudService(null, configWith(false), null);
        assertTrue(svc.toggle(player));
        assertTrue(svc.isEnabled(player));
    }

    @Test
    void toggle_falseByDefault_secondToggleDisables() {
        InfoHudService svc = new InfoHudService(null, configWith(false), null);
        svc.toggle(player);
        assertFalse(svc.toggle(player));
        assertFalse(svc.isEnabled(player));
    }

    // ── Per-player isolation ───────────────────────────────────────────────────

    @Test
    void toggle_onlyAffectsToggledPlayer() {
        Player other = mock(Player.class);
        when(other.getUniqueId()).thenReturn(UUID.randomUUID());

        InfoHudService svc = new InfoHudService(null, configWith(true), null);
        svc.toggle(player); // disable player

        assertFalse(svc.isEnabled(player));
        assertTrue(svc.isEnabled(other), "unmodified player should remain enabled");
    }
}
