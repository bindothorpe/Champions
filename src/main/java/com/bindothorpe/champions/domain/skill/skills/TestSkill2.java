package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestSkill2 extends Skill {

    private List<Integer> damage = Arrays.asList(3, 5, 8, 12);
    private List<Double> passive = Arrays.asList(0.8, 1.2, 1.4, 1.6);

    public TestSkill2() {
        super(SkillId.TEST_SKILL_2, SkillType.SWORD, ClassType.GLOBAL, "Test Skill 2", new ArrayList<>(), Arrays.asList(8.5, 7.0, 4.5, 3.5), 4, 2);
    }

    @EventHandler
    public void onRightClickEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();



        if(!activate(player.getUniqueId(), event))
            return;

        LivingEntity entity = (LivingEntity) event.getRightClicked();
        entity.damage(damage.get(getSkillLevel(player.getUniqueId()) - 1), player);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event e) {
        if(!(e instanceof PlayerInteractAtEntityEvent))
            return false;

        PlayerInteractAtEntityEvent event = (PlayerInteractAtEntityEvent) e;

        if (!event.getHand().equals(EquipmentSlot.HAND))
            return false;

        if(!event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("SWORD"))
            return false;

        if(!(event.getRightClicked() instanceof LivingEntity))
            return false;

        return super.canUseHook(uuid, event);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> description = new ArrayList<>();
        description.add(Component.text("Active: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click").color(NamedTextColor.YELLOW))
                .append(Component.text(" to deal").color(NamedTextColor.GRAY)));
        description.add(ComponentUtil.skillLevelValues(skillLevel, damage, NamedTextColor.RED)
                .append(Component.text(" damage").color(NamedTextColor.RED)));
        description.add(Component.text(" "));
        description.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("increases ").color(NamedTextColor.GRAY))
                .append(Component.text("attack damage").color(NamedTextColor.GRAY)));
        description.add(Component.text("by ").color(NamedTextColor.GRAY)
                .append(ComponentUtil.skillLevelValues(skillLevel, passive, NamedTextColor.RED))
                .append(Component.text(" %").color(NamedTextColor.RED)));

        return description;
    }
}
