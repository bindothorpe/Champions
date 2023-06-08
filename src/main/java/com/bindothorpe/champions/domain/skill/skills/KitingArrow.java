package com.bindothorpe.champions.domain.skill.skills;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.player.PlayerEffect;
import com.bindothorpe.champions.domain.player.PlayerEffectType;
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

public class KitingArrow extends Skill {

    private final List<Float> speed = Arrays.asList(0.5F, 0.8F, 1.0F);

    public KitingArrow(DomainController dc) {
        super(dc, SkillId.KITING_ARROW, SkillType.PASSIVE_A, ClassType.RANGER, "Kiting Arrow", null, 3, 1);
    }

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        if(!(event.getEntity() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getEntity();

        if(!(arrow.getShooter() instanceof Player))
            return;

        Player player = (Player) arrow.getShooter();

        if(!isUser(player.getUniqueId()))
            return;

        player.sendMessage("You shot an arrow!");
        dc.addEffectToPlayer(player.getUniqueId(), new PlayerEffect(PlayerEffectType.MOVEMENT_SPEED, speed.get(getSkillLevel(player.getUniqueId()) - 1), 2, false, getId()));
        dc.addEffectToPlayer(player.getUniqueId(), new PlayerEffect(PlayerEffectType.MOVEMENT_SPEED, speed.get(getSkillLevel(player.getUniqueId()) - 1), 2, true, getId()));
        player.setWalkSpeed((float) (speed.get(getSkillLevel(player.getUniqueId()) - 1)));
    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Passive: ").color(NamedTextColor.WHITE)
                .append(Component.text("Shooting an arrow gives you").color(NamedTextColor.GRAY)));
        lore.add(ComponentUtil.skillLevelValues(skillLevel, speed, NamedTextColor.WHITE)
                .append(Component.text(" movement speed for").color(NamedTextColor.GRAY)));
        lore.add(Component.text("2 seconds").color(NamedTextColor.GRAY));
        return lore;
    }
}
