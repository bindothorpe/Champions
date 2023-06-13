package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.block.TemporaryBlock;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ShapeUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class TestSkill extends Skill {

    private final List<Double> healing = Arrays.asList(3.0, 5.0, 8.5);
    private final List<Double> passiveHealing = Arrays.asList(0.5, 1.0, 1.5);


    public TestSkill(DomainController dc) {
        super(dc, SkillId.TEST_SKILL, SkillType.AXE, ClassType.MAGE, "Test Skill", Arrays.asList(5.0, 3.0, 2.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        boolean success = activate(player.getUniqueId(), event);

        if (!success) {
            return;
        }

        List<Vector> vectors = ShapeUtil.sphere(5).stream().sorted((v1, v2) -> v2.getBlockY() - v1.getBlockY()).collect(Collectors.toList());

        for(int i = 0; i < vectors.size(); i++) {
            Vector v = vectors.get(i);
            dc.spawnTemporaryBlock(player.getLocation().clone().add(v), Material.ICE, 5 + (i / 200D));
        }
    }

//    @EventHandler
//    public void onTwoSecondsPassed(UpdateEvent event) {
//        if(!event.getUpdateType().equals(UpdateType.TWO_SECOND))
//            return;
//
//        for(UUID uuid : getUsers()) {
//            Player player = Bukkit.getPlayer(uuid);
//            if(player == null)
//                continue;
//
//            //TODO: This logic is not working correctly, fix this.
//            double healingDone = Math.min(player.getHealth() + healing.get(getSkillLevel(player.getUniqueId()) - 1), Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) - player.getHealth();
//
//
//            player.setHealth(Math.min(player.getHealth() + passiveHealing.get(getSkillLevel(uuid) - 1), Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()));
//            player.sendMessage(Component.text("You have been healed for ").color(NamedTextColor.GRAY)
//                    .append(Component.text(healingDone).color(NamedTextColor.GREEN))
//                    .append(Component.text(" health").color(NamedTextColor.GRAY)));
//        }
//    }

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
        List<Component> description = new ArrayList<>();

        description.add(Component.text("Active: ").color(NamedTextColor.WHITE)
                .append(Component.text("right-click").color(NamedTextColor.YELLOW))
                .append(Component.text(" to heal").color(NamedTextColor.GRAY)));
        description.add(ComponentUtil.skillLevelValues(skillLevel, healing, NamedTextColor.GREEN)
                .append(Component.text(" hp").color(NamedTextColor.GREEN)));
        description.add(Component.text(" "));
        description.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("after not getting").color(NamedTextColor.GRAY)));
        description.add(Component.text("hit for 5 seconds, heal").color(NamedTextColor.GRAY));
        description.add(ComponentUtil.skillLevelValues(skillLevel, passiveHealing, NamedTextColor.GREEN)
                .append(Component.text(" hp").color(NamedTextColor.GREEN))
                .append(Component.text(" every").color(NamedTextColor.GRAY)));
        description.add(Component.text("2 seconds").color(NamedTextColor.GRAY));


        return description;
    }
}
