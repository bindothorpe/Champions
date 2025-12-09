package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class Explosion extends Skill implements ReloadableData {
    private Map<UUID, ArmorStand> explosionOrbsMap;
    private Map<UUID, Vector> directionMap;

    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;
    private static double BASE_TRAVEL_SPEED;
    private static double TRAVEL_SPEED_INCREASE_PER_LEVEL;
    private static double BASE_COLLISION_RADIUS;
    private static double COLLISION_RADIUS_INCREASE_PER_LEVEL;

    public Explosion(DomainController dc) {
        super(dc, "Explosion", SkillId.EXPLOSION, SkillType.AXE, ClassType.MAGE);
        explosionOrbsMap = new HashMap<>();
        directionMap = new HashMap<UUID, Vector>();
    }


    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();


        if(explosionOrbsMap.containsKey(player.getUniqueId())) {
            onSecondRightClick(player.getUniqueId(), event);
            return;
        }

        boolean success = activate(player.getUniqueId(), event);

        if(!success)
            return;

        Vector dir = player.getLocation().getDirection().normalize();
        directionMap.put(player.getUniqueId(), dir);

        ArmorStand explosionOrb = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        explosionOrb.setGravity(false);
        explosionOrb.setInvulnerable(true);
        explosionOrb.setInvisible(true);
        explosionOrb.setTicksLived(1);
        explosionOrbsMap.put(player.getUniqueId(), explosionOrb);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK))
            return;

        for(Map.Entry<UUID, ArmorStand> entry: explosionOrbsMap.entrySet()) {
            UUID uuid = entry.getKey();

            ArmorStand explosionOrb = entry.getValue();
            Vector direction = directionMap.get(uuid);
            Vector add = direction.clone().multiply(calculateBasedOnLevel(BASE_TRAVEL_SPEED, TRAVEL_SPEED_INCREASE_PER_LEVEL, getSkillLevel(uuid)) / 20);

            explosionOrb.teleport(explosionOrb.getLocation().add(add));
            explosionOrb.getWorld().spawnParticle(Particle.FLAME, explosionOrb.getEyeLocation(), 1, 0, 0, 0, 0, null, true);
            explosionOrb.getWorld().spawnParticle(Particle.LARGE_SMOKE, explosionOrb.getEyeLocation(), 1, 0, 0, 0, 0, null, true);

            Block block = explosionOrb.getEyeLocation().getBlock();

            if(block.getType().isSolid()) {
                performExplosion(uuid, explosionOrb.getEyeLocation(), new HashSet<>(Arrays.asList(uuid, explosionOrb.getUniqueId())));                explosionOrb.remove();
                explosionOrbsMap.remove(uuid);
                directionMap.remove(uuid);
                return;
            }

            double radius = calculateBasedOnLevel(BASE_COLLISION_RADIUS, COLLISION_RADIUS_INCREASE_PER_LEVEL, getSkillLevel(uuid));
            Set<UUID> uuids = explosionOrb.getEyeLocation().getNearbyEntities(radius, radius, radius).stream().filter(entity -> entity instanceof LivingEntity).map(entity -> entity.getUniqueId()).collect(Collectors.toSet());
            uuids.remove(uuid);
            uuids.remove(explosionOrb.getUniqueId());


            if(uuids.size() > 0) {
                performExplosion(uuid, explosionOrb.getEyeLocation(), new HashSet<>(Arrays.asList(uuid, explosionOrb.getUniqueId())));
                explosionOrb.remove();
                explosionOrbsMap.remove(uuid);
                directionMap.remove(uuid);
                return;
            }

            if(explosionOrb.getTicksLived() > 20 * 3) {
                performExplosion(uuid, explosionOrb.getEyeLocation(), new HashSet<>(Arrays.asList(uuid, explosionOrb.getUniqueId())));
                explosionOrb.remove();
                explosionOrbsMap.remove(uuid);
                directionMap.remove(uuid);
            }
        }
    }

    private void onSecondRightClick(UUID uuid, Event e) {
        if(!canUseHook(uuid, e))
            return;

        ArmorStand explosionOrb = explosionOrbsMap.get(uuid);
        if(explosionOrb == null)
            return;

        performExplosion(uuid, explosionOrb.getEyeLocation(), new HashSet<>(Arrays.asList(uuid, explosionOrb.getUniqueId())));
        explosionOrb.remove();
        explosionOrbsMap.remove(uuid);
        directionMap.remove(uuid);

    }

    private void performExplosion(UUID uuid, Location location, Set<UUID> blacklist) {
        Set<UUID> uuids = location.getNearbyEntities(2.5, 2.5, 2.5).stream().filter(entity -> entity instanceof LivingEntity).map(entity -> entity.getUniqueId()).collect(Collectors.toSet());
        uuids.removeAll(blacklist);

        for(UUID id: uuids) {
            LivingEntity entity = (LivingEntity) Bukkit.getEntity(id);
            entity.damage(calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid)));
        }

        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 1, 0, 0, 0, 0, null, true);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event e) {
        if(!(e instanceof PlayerInteractEvent))
            return false;

        PlayerInteractEvent event = (PlayerInteractEvent) e;

        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND))
            return false;

        if(!event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_AXE"))
            return false;

        return super.canUseHook(uuid, e);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click ").color(NamedTextColor.YELLOW))
                .append(Component.text("to").color(NamedTextColor.GRAY)));
        lore.add(Component.text("launch an explosive orb").color(NamedTextColor.GRAY));
        lore.add(Component.text("that travels ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_TRAVEL_SPEED, TRAVEL_SPEED_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW)));
        lore.add(Component.text("blocks per second and").color(NamedTextColor.GRAY));
        lore.add(Component.text("deals ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                .append(Component.text(" damage").color(NamedTextColor.RED)));
        lore.add(Component.text("on impact or recast, to all").color(NamedTextColor.GRAY));
        lore.add(Component.text("nearby enemies").color(NamedTextColor.GRAY));
        return lore;
    }


    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.explosion.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.explosion.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.cooldown_reduction_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.damage_increase_per_level");
            BASE_TRAVEL_SPEED = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.base_travel_speed");
            TRAVEL_SPEED_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.travel_speed_increase_per_level");
            BASE_COLLISION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.base_collision_radius");
            COLLISION_RADIUS_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.explosion.collision_radius_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}
