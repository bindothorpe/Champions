package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.ExplosiveItem;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public class ExplosiveBomb extends Skill {

    private Map<UUID, GameItem> explosiveBombs = new HashMap<>();
    private final List<Double> damage = Arrays.asList(2.0, 3.0, 4.0);

    public ExplosiveBomb(DomainController dc) {
        super(dc, SkillId.EXPLOSIVE_BOMB, SkillType.AXE, ClassType.BRUTE, "Explosive Bomb", Arrays.asList(5.0, 4.5, 4.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(!event.isAxe())
            return;

        if (explosiveBombs.containsKey(player.getUniqueId()) && (!explosiveBombs.get(player.getUniqueId()).getItem().isDead())) {
            GameItem bomb = explosiveBombs.get(player.getUniqueId());
            dc.despawnItem(bomb.getId());
            explosiveBombs.remove(player.getUniqueId());
            return;
        }

        if (!activate(player.getUniqueId(), event))
            return;

        GameItem bomb = new ExplosiveItem(dc, player, damage.get(getSkillLevel(player.getUniqueId()) - 1));
        dc.spawnGameItem(bomb, player.getEyeLocation(), player.getLocation().getDirection(), 1.5);
        explosiveBombs.put(player.getUniqueId(), bomb);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return new ArrayList<>();
    }

    public void removeBomb(UUID uuid) {
        explosiveBombs.remove(uuid);
    }
}
