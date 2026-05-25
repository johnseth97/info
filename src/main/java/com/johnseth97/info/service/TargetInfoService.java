package com.johnseth97.info.service;

import com.johnseth97.info.config.InfoConfig;
import com.johnseth97.info.util.NameUtil;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Set;

public class TargetInfoService {

    /**
     * Returns a formatted actionbar string for the block/entity the player is looking at,
     * or null if nothing relevant is in range.
     */
    public String getTargetText(Player player, InfoConfig cfg) {
        RayTraceResult entityResult = null;
        RayTraceResult blockResult  = null;

        if (cfg.preferEntities) {
            entityResult = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    cfg.maxDistance,
                    e -> !e.equals(player)
            );
        }

        blockResult = player.rayTraceBlocks(cfg.maxDistance, FluidCollisionMode.NEVER);

        // Prefer entity when it is closer (or when cfg says so), fall back to block
        if (cfg.preferEntities && entityResult != null && entityResult.getHitEntity() != null) {
            double eDist = entityResult.getHitPosition().distanceSquared(player.getEyeLocation().toVector());
            double bDist = blockResult != null && blockResult.getHitBlock() != null
                    ? blockResult.getHitPosition().distanceSquared(player.getEyeLocation().toVector())
                    : Double.MAX_VALUE;
            if (eDist <= bDist) {
                return formatEntity(entityResult.getHitEntity(), cfg);
            }
        }

        if (blockResult != null && blockResult.getHitBlock() != null) {
            Block block = blockResult.getHitBlock();
            if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR) {
                return formatBlock(block, cfg);
            }
        }

        return null;
    }

    private String formatBlock(Block block, InfoConfig cfg) {
        StringBuilder sb = new StringBuilder();

        if (cfg.showMaterialName) {
            sb.append(NameUtil.pretty(block.getType().name()));
        }

        if (cfg.showNamespacedKey) {
            appendSeparator(sb);
            sb.append(block.getType().getKey().toString());
        }

        if (cfg.showCoordinates) {
            appendSeparator(sb);
            sb.append(block.getX()).append(", ").append(block.getY()).append(", ").append(block.getZ());
        }

        if (cfg.showBiome) {
            appendSeparator(sb);
            Biome biome = block.getBiome();
            sb.append(NameUtil.pretty(biome.getKey().getKey().toUpperCase()));
        }

        if (cfg.showLightLevel) {
            appendSeparator(sb);
            sb.append("Light: ").append(block.getLightLevel());
        }

        return sb.toString();
    }

    private String formatEntity(Entity entity, InfoConfig cfg) {
        StringBuilder sb = new StringBuilder();

        String name = entity.customName() != null
                ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(entity.customName())
                : NameUtil.pretty(entity.getType().name());

        if (cfg.showMaterialName) {
            sb.append(name);
        }

        if (cfg.showNamespacedKey) {
            appendSeparator(sb);
            sb.append(entity.getType().getKey().toString());
        }

        if (cfg.showEntityHealth && entity instanceof LivingEntity living) {
            appendSeparator(sb);
            sb.append("Health: ")
              .append((int) Math.ceil(living.getHealth()))
              .append("/")
              .append((int) living.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        }

        return sb.toString();
    }

    private void appendSeparator(StringBuilder sb) {
        if (!sb.isEmpty()) sb.append(" · ");
    }
}
