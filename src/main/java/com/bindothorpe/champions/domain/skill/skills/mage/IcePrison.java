package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.items.IceOrbItem;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

public class IcePrison extends Skill implements ReloadableData {

    private static double BASE_DURATION;
    private static double DURATION_INCREASE_PER_LEVEL;
    private static double BASE_ORB_DURATION;
    private static double ORB_DURATION_INCREASE_PER_LEVEL;
    private static double BASE_LAUNCH_STRENGTH;
    private static double LAUNCH_STRENGTH_INCREASE_PER_LEVEL;
    private static double RADIUS;

    public IcePrison(DomainController dc) {
        super(dc, "Ice Prison", SkillId.ICE_PRISON, SkillType.AXE, ClassType.MAGE);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if (!activate(player.getUniqueId(), event)) return;

        dc.getGameItemManager().spawnGameItem(
                new IceOrbItem(dc, calculateBasedOnLevel(BASE_ORB_DURATION, ORB_DURATION_INCREASE_PER_LEVEL, getSkillLevel(player)), calculateBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, getSkillLevel(player)), player, RADIUS),
                player.getEyeLocation(),
                player.getLocation().getDirection(),
                calculateBasedOnLevel(BASE_LAUNCH_STRENGTH, LAUNCH_STRENGTH_INCREASE_PER_LEVEL, getSkillLevel(player)));
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent playerRightClickEvent))
            return false;

        if (!playerRightClickEvent.isAxe())
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> description = new ArrayList<>();

        description.add(Component.text("Active: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click").color(NamedTextColor.YELLOW))
                .append(Component.text(" to throw").color(NamedTextColor.GRAY)));
        description.add(Component.text("an ice orb, that creates").color(NamedTextColor.GRAY));
        description.add(Component.text("an ice prison that stays").color(NamedTextColor.GRAY));
        description.add(Component.text("for ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                .append(Component.text(" seconds").color(NamedTextColor.GRAY)));

        return description;
    }

    @Override
    public void onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.ice_prison.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.mage.ice_prison.level_up_cost");
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.base_cooldown");
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.cooldown_reduction_per_level");
            BASE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.base_duration");
            DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.duration_increase_per_level");
            BASE_ORB_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.base_orb_duration");
            ORB_DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.orb_duration_increase_per_level");
            BASE_LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.base_launch_strength");
            LAUNCH_STRENGTH_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.launch_strength_increase_per_level");
            RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.mage.ice_prison.radius");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
        }
    }
}
