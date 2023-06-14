package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitingArrow extends Skill {

    private final List<Float> speed = Arrays.asList(0.05F, 0.1F, 0.15F);
    private final List<Double> speedDuration = Arrays.asList(0.5, 1.0, 1.5);

    public KitingArrow(DomainController dc) {
        super(dc, SkillId.KITING_ARROW, SkillType.PASSIVE_A, ClassType.RANGER, "Kiting Arrow", null, 3, 1);
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if (!isUser(player.getUniqueId()))
            return;

        dc.addStatusToEntity(player.getUniqueId(), new EntityStatus(EntityStatusType.MOVEMENT_SPEED, speed.get(getSkillLevel(player.getUniqueId()) - 1), speedDuration.get(getSkillLevel(player.getUniqueId()) - 1), false, this));
        dc.updateEntityStatus(player.getUniqueId(), EntityStatusType.MOVEMENT_SPEED);
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        List<Integer> speedDisplay = speed.stream().map(speed -> (int) (speed * 100)).collect(Collectors.toList());
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("Shooting an arrow gives you").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, speedDisplay, NamedTextColor.WHITE)
                .append(Component.text(" movement speed for").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, speedDuration, NamedTextColor.WHITE)
                .append(Component.text(" seconds").color(NamedTextColor.GRAY)));
        return lore;
    }
}
