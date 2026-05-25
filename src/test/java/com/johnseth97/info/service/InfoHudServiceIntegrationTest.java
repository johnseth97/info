package com.johnseth97.info.service;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import com.johnseth97.info.config.InfoConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class InfoHudServiceIntegrationTest {

    ServerMock server;
    MockPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private InfoConfig defaultConfig() {
        return new InfoConfig(new YamlConfiguration());
    }

    @Test
    void start_schedulesRepeatingTaskWithoutError() {
        InfoHudService svc = new InfoHudService(plugin, defaultConfig(), new TargetInfoService());
        assertDoesNotThrow(svc::start);
        server.getScheduler().performTicks(10);
    }

    @Test
    void stop_beforeStart_doesNotThrow() {
        InfoHudService svc = new InfoHudService(plugin, defaultConfig(), new TargetInfoService());
        assertDoesNotThrow(svc::stop);
    }

    @Test
    void stop_afterStart_cancelsTask() {
        InfoHudService svc = new InfoHudService(plugin, defaultConfig(), new TargetInfoService());
        svc.start();
        assertDoesNotThrow(svc::stop);
        server.getScheduler().performTicks(10); // no task should fire
    }

    @Test
    void reload_replacesScheduledTask() {
        InfoHudService svc = new InfoHudService(plugin, defaultConfig(), new TargetInfoService());
        svc.start();

        YamlConfiguration newCfg = new YamlConfiguration();
        newCfg.set("update-interval-ticks", 10L);
        assertDoesNotThrow(() -> svc.reload(new InfoConfig(newCfg)));

        server.getScheduler().performTicks(15);
        svc.stop();
    }
}
