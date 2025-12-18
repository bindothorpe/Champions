package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.interact.blocking.PlayerStopBlockingEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.util.MobilityUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Claw extends Skill implements ReloadableData {

    private final Map<UUID, UUID> attachedMap = new HashMap<>();
    private final Map<UUID, Vector> offsetMap = new HashMap<>();
    private final Map<UUID, Vector> startingDirectionMap = new HashMap<>();

    public Claw(DomainController dc) {
        super(dc, "Claw", SkillId.CLAW, SkillType.SWORD, ClassType.ASSASSIN);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        attachedMap.remove(uuid);
        offsetMap.remove(uuid);
        startingDirectionMap.remove(uuid);
    }

    @EventHandler
    public void onRightClickEntity(PlayerRightClickEvent event) {
        Player player = event.getPlayer();
        if(!isUser(player.getUniqueId())) return;

        if(event.getClickedEntity() == null) return;

        if(!activate(player.getUniqueId(), event)) return;

        attachedMap.put(player.getUniqueId(), event.getClickedEntity().getUniqueId());
        offsetMap.put(player.getUniqueId(), MobilityUtil.directionTo(event.getClickedEntity().getLocation(), player.getLocation()).multiply(MobilityUtil.distanceTo(event.getClickedEntity().getLocation(), player.getLocation())));
        startingDirectionMap.put(player.getUniqueId(), player.getLocation().getDirection());
    }

    @EventHandler
    public void onStopBlocking(PlayerStopBlockingEvent event) {
        if(!attachedMap.containsKey(event.getPlayer().getUniqueId())) return;

        attachedMap.remove(event.getPlayer().getUniqueId());
        offsetMap.remove(event.getPlayer().getUniqueId());
        startingDirectionMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.isTick()) return;

        for(UUID uuid : attachedMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null || !player.isOnline()) continue;

            if(!(Bukkit.getEntity(attachedMap.get(uuid)) instanceof LivingEntity entity)) continue;


            Location location = entity.getLocation().clone().add(offsetMap.get(uuid));
            location.setDirection(startingDirectionMap.get(uuid));
            player.teleport(location);
            player.setFallDistance(0);
        }

    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
