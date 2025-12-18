package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Isolation extends Skill implements ReloadableData {

    private double RADIUS;
    private double DAMAGE;

    private final Particle.DustTransition dustOptions = new Particle.DustTransition(Color.RED, Color.BLACK, 1.0f);

    public Isolation(DomainController dc) {
        super(dc, "Isolation", SkillId.ISOLATION, SkillType.PASSIVE_B, ClassType.ASSASSIN);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(!(event.getDamager() instanceof Player attacker)) return;
        if(!isUser(attacker)) return;
        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;


        LivingEntity target = event.getDamagee();

        if(!isIsolated(target)) return;

        dc.getEntityStatusManager().addEntityStatus(attacker.getUniqueId(),
                new EntityStatus(
                        EntityStatusType.ATTACK_DAMAGE_DONE,
                        DAMAGE,
                        0.1,
                        false,
                        false,
                        this
                ));
    }

    private boolean isIsolated(@NotNull Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) return false;
        return isIsolated(livingEntity);
    }

    private boolean isIsolated(@NotNull LivingEntity livingEntity) {
        return livingEntity.getNearbyEntities(RADIUS, RADIUS, RADIUS).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> !entity.equals(livingEntity)) // Exclude the target itself
                .filter(entity -> !dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, entity)) // Check for ALLIES
                .filter(entity -> !entity.isDead())
                .findFirst()
                .isEmpty(); // True if no allies found = isolated
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.RAPID)) return;

        for(UUID uuid: getUsers()) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            for(Entity entity : player.getNearbyEntities(10, 10, 10)) {
                if(!isIsolated(entity)) continue;
                if(dc.getStatusEffectManager().hasStatusEffect(StatusEffectType.TRUE_INVISIBLE, entity.getUniqueId())) continue;
                playIsolationParticle(player, (LivingEntity) entity);
            }

        }
    }

    private void playIsolationParticle(Player viewer, LivingEntity isolatedEntity) {
        viewer.spawnParticle(
                Particle.DUST,
                isolatedEntity.getLocation().clone().add(0, isolatedEntity.getHeight() + 0.3, 0),
                1,
                dustOptions);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.passive()
                        .append(Component.text(String.format("Deal %.1f bonus damage to enemies that are ", DAMAGE)).color(NamedTextColor.GRAY))
                        .append(Component.text("isolated", NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.GRAY)),
                30));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Enemies are ", NamedTextColor.GRAY)
                        .append(Component.text("isolated", NamedTextColor.YELLOW))
                        .append(Component.text(" when there are no other enemies within ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.1f", RADIUS), NamedTextColor.GRAY))
                        .append(Component.text(" blocks of the target enemy, and will be marked with a red indicator above their head.")),
                30));
        return lore;
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("radius"));
            DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
