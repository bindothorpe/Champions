package com.bindothorpe.champions.domain.skill.skills.mage;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.items.IceOrbItem;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
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

public class IcePrison extends Skill {

    private final List<Double> duration = Arrays.asList(3.0, 4.0, 5.0);


    public IcePrison(DomainController dc) {
        super(dc, SkillId.ICE_PRISON, SkillType.AXE, ClassType.MAGE, "Ice Prison", Arrays.asList(5.0, 3.0, 2.0), 3, 1);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        boolean success = activate(player.getUniqueId(), event);

        if (!success) {
            return;
        }

        dc.spawnGameItem(new IceOrbItem(dc, 5, duration.get(getSkillLevel(player.getUniqueId()) - 1), player), player.getEyeLocation(), player.getLocation().getDirection(), 1.5);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event e) {
        if (!(e instanceof PlayerInteractEvent))
            return false;

        PlayerInteractEvent event = (PlayerInteractEvent) e;

        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND))
            return false;

        if (!event.getPlayer().getInventory().getItemInMainHand().getType().toString().contains("_AXE"))
            return false;

        return super.canUseHook(uuid, e);
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
                .append(ComponentUtil.skillLevelValues(skillLevel, duration, NamedTextColor.YELLOW))
                .append(Component.text(" seconds").color(NamedTextColor.GRAY)));

        return description;
    }
}
