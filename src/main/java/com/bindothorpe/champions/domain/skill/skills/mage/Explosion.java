package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
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

public class Explosion extends Skill {

    private List<Double> travelSpeed = Arrays.asList(30.0, 38.0, 50.0);
    private List<Double> damage = Arrays.asList(7.0, 7.5, 8.0);

    private Map<UUID, ArmorStand> explosionOrbsMap;
    private Map<UUID, Vector> directionMap;

    public Explosion(DomainController dc) {
        super(dc, SkillId.EXPLOSION, SkillType.AXE, ClassType.MAGE, "Explosion", Arrays.asList(10D, 8D, 2D), 3, 1);
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
            Player player = Bukkit.getPlayer(uuid);

            ArmorStand explosionOrb = entry.getValue();
            Vector direction = directionMap.get(uuid);
            Vector add = direction.clone().multiply(travelSpeed.get(getSkillLevel(uuid) - 1) / 20);

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

            Set<UUID> uuids = explosionOrb.getEyeLocation().getNearbyEntities(0.25, 0.25, 0.25).stream().filter(entity -> entity instanceof LivingEntity).map(entity -> entity.getUniqueId()).collect(Collectors.toSet());
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
            entity.damage(damage.get(getSkillLevel(uuid) - 1));
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
                .append(ComponentUtil.skillLevelValues(skillLevel, travelSpeed, NamedTextColor.YELLOW)));
        lore.add(Component.text("blocks per second and").color(NamedTextColor.GRAY));
        lore.add(Component.text("deals ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, damage, NamedTextColor.YELLOW))
                .append(Component.text(" damage").color(NamedTextColor.RED)));
        lore.add(Component.text("on impact or recast, to all").color(NamedTextColor.GRAY));
        lore.add(Component.text("nearby enemies").color(NamedTextColor.GRAY));
        return lore;
    }
}
