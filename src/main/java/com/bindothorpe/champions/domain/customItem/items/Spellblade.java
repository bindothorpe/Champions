package com.bindothorpe.champions.domain.customItem.items;

import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.CustomItemType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Spellblade extends CustomItem {

    private static Set<UUID> active = new HashSet<>();
    private static Map<UUID, BukkitTask> taskMap = new HashMap<>();
    public Spellblade(CustomItemManager manager) {
        super(manager, CustomItemId.SPELLBLADE, Set.of(CustomItemType.ATTACK), "Spellblade", Material.GOLDEN_SWORD, 400, List.of(CustomItemId.LONG_SWORD, CustomItemId.LONG_SWORD));
    }

    @Override
    protected List<Component> getAdditionalLore() {
        List<Component> lore = super.getAdditionalLore();
        lore.add(ComponentUtil.passive()
                .append(Component.text("Every time you").color(NamedTextColor.GRAY)));
        lore.add(Component.text("cast a skill, your next").color(NamedTextColor.GRAY));
        lore.add(Component.text("attack within 3 seconds").color(NamedTextColor.GRAY));
        lore.add(Component.text("deals bonus damage").color(NamedTextColor.GRAY));
        lore.add(Component.text("(3 second cooldown)").color(NamedTextColor.GRAY));

        return lore;
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        Player player = event.getPlayer();
        if(active.contains(player.getUniqueId())) {
            return;
        }

        active.add(player.getUniqueId());
        taskMap.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                active.remove(player.getUniqueId());
                taskMap.remove(player.getUniqueId());
                cancel();
            }
        }.runTaskLater(manager.getDc().getPlugin(), 3 * 20L));
    }
}
