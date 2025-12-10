package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.ExplosiveItem;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public class ExplosiveBomb extends Skill implements ReloadableData {

    private Map<UUID, GameItem> explosiveBombs = new HashMap<>();
    private static double BASE_DAMAGE;
    private static double DAMAGE_INCREASE_PER_LEVEL;

    public ExplosiveBomb(DomainController dc) {
        super(dc, "Explosive Bomb", SkillId.EXPLOSIVE_BOMB, SkillType.AXE, ClassType.BRUTE);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(!event.isAxe())
            return;

        if (explosiveBombs.containsKey(player.getUniqueId()) && (!explosiveBombs.get(player.getUniqueId()).getItem().isDead())) {
            GameItem bomb = explosiveBombs.get(player.getUniqueId());
            dc.getGameItemManager().despawnItem(bomb.getId());
            explosiveBombs.remove(player.getUniqueId());
            return;
        }

        if (!activate(player.getUniqueId(), event))
            return;

        GameItem bomb = new ExplosiveItem(dc, player, calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player)));
        dc.getGameItemManager().spawnGameItem(bomb, player.getEyeLocation(), player.getLocation().getDirection(), 1.5);
        explosiveBombs.put(player.getUniqueId(), bomb);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();

        lore.add(ComponentUtil.active()
                .append(ComponentUtil.rightClick())
                .append(Component.text("to throw an").color(NamedTextColor.GRAY)));
        lore.add(Component.text("Explosive Bomb that sticks").color(NamedTextColor.GRAY));
        lore.add(Component.text("to walls and explodes,").color(NamedTextColor.GRAY));
        lore.add(Component.text("dealing ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                .append(Component.text(" damage,").color(NamedTextColor.GRAY)));
        lore.add(Component.text("if you ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.rightClick())
                .append(Component.text("again").color(NamedTextColor.GRAY)));
        return lore;
    }

    public void removeBomb(UUID uuid) {
        explosiveBombs.remove(uuid);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.cooldown_reduction_per_level");
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.base_damage");
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.brute.explosive_bomb.damage_increase_per_level");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
