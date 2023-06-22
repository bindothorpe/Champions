package com.bindothorpe.champions.domain.skill.skills.global;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Rally extends Skill {
    public Rally(DomainController dc) {
        super(dc, SkillId.RALLY, SkillType.SWORD, ClassType.GLOBAL, "Rally", List.of(3.0), 1, 2);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        boolean success = activate(event.getPlayer().getUniqueId(), event);

        if (!success) {
            return;
        }

        List<Entity> nearby = event.getPlayer().getNearbyEntities(5, 5, 5);

        for (Entity entity : nearby) {
            if(dc.getTeamFromEntity(entity) != null)
                continue;

            dc.addEntityToTeam(entity, dc.getTeamFromEntity(event.getPlayer()));
            entity.setGlowing(true);

            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.setGlowing(false);
                    dc.removeEntityFromTeam(entity);
                }
            }.runTaskLater(dc.getPlugin(), 20 * 5);
        }
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent))
            return false;

        PlayerRightClickEvent e = (PlayerRightClickEvent) event;

        if(dc.getTeamFromEntity(e.getPlayer()) == null) {
            System.out.println("No team");
            return false;
        }

        if(!e.isSword())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(ComponentUtil.rightClick())
                .append(Component.text("to stun your self")));

        return lore;
    }
}
