package com.johnseth97.visualid.service;

import com.johnseth97.visualid.config.VisualIdConfig;
import com.johnseth97.visualid.util.NameUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

/**
 * Builds the HUD component for whatever the player is currently targeting.
 */
public class TargetInfoService {

    private static final Component SEP = Component.text("  ·  ", NamedTextColor.DARK_GRAY);

    public Component getTargetComponent(Player player, VisualIdConfig cfg) {
        RayTraceResult entityResult = null;
        RayTraceResult blockResult;

        if (cfg.preferEntities) {
            // Ray trace entities first when the config prefers them.
            entityResult = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    cfg.maxDistance,
                    e -> !e.equals(player)
            );
        }

        // Always ray trace blocks so we can compare distances.
        blockResult = player.rayTraceBlocks(cfg.maxDistance, FluidCollisionMode.NEVER);

        if (cfg.preferEntities && entityResult != null && entityResult.getHitEntity() != null) {
            // Choose whichever hit is closer to the player's eyes.
            double eDist = entityResult.getHitPosition().distanceSquared(player.getEyeLocation().toVector());
            double bDist = blockResult != null && blockResult.getHitBlock() != null
                    ? blockResult.getHitPosition().distanceSquared(player.getEyeLocation().toVector())
                    : Double.MAX_VALUE;
            if (eDist <= bDist) return formatEntity(entityResult.getHitEntity(), cfg);
        }

        if (blockResult != null && blockResult.getHitBlock() != null) {
            Block block = blockResult.getHitBlock();
            if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR) {
                return formatBlock(block, cfg);
            }
        }

        return null;
    }

    // ── Block ──────────────────────────────────────────────────────────────────

    private Component formatBlock(Block block, VisualIdConfig cfg) {
        Component out = Component.text(NameUtil.pretty(block.getType().name()), NamedTextColor.WHITE);

        if (cfg.showNamespacedKey) {
            out = out.append(SEP)
                     .append(Component.text(block.getType().getKey().toString(), NamedTextColor.GRAY));
        }
        if (cfg.showCoordinates) {
            out = out.append(SEP)
                     .append(Component.text(block.getX() + ", " + block.getY() + ", " + block.getZ(), NamedTextColor.GRAY));
        }
        if (cfg.showBiome) {
            String biome = NameUtil.pretty(block.getBiome().getKey().getKey().toUpperCase());
            out = out.append(SEP).append(Component.text(biome, NamedTextColor.DARK_AQUA));
        }
        if (cfg.showLightLevel) {
            out = out.append(SEP).append(Component.text("Light " + block.getLightLevel(), NamedTextColor.YELLOW));
        }
        out = out.append(SEP).append(modComponent(block.getType().getKey().namespace()));

        return out;
    }

    // ── Entity ─────────────────────────────────────────────────────────────────

    private Component formatEntity(Entity entity, VisualIdConfig cfg) {
        String name = entity.customName() != null
                ? PlainTextComponentSerializer.plainText().serialize(entity.customName())
                : NameUtil.pretty(entity.getType().name());

        Component out = Component.text(name, NamedTextColor.WHITE);

        if (cfg.showNamespacedKey) {
            out = out.append(SEP)
                     .append(Component.text(entity.getType().getKey().toString(), NamedTextColor.GRAY));
        }
        if (cfg.showEntityHealth && entity instanceof LivingEntity living) {
            double hp    = living.getHealth();
            double maxHp = living.getAttribute(Attribute.MAX_HEALTH).getValue();
            double ratio = hp / maxHp;
            NamedTextColor healthColor = ratio > 0.5 ? NamedTextColor.GREEN
                                       : ratio > 0.25 ? NamedTextColor.YELLOW
                                       : NamedTextColor.RED;
            out = out.append(SEP)
                     .append(Component.text("❤ " + (int) Math.ceil(hp) + "/" + (int) maxHp, healthColor));
        }
        out = out.append(SEP).append(modComponent(entity.getType().getKey().namespace()));

        return out;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Component modComponent(String namespace) {
        return Component.text(NameUtil.pretty(namespace))
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.ITALIC, true);
    }
}
