package com.bindothorpe.champions.domain.skill.subSkills;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.AttemptResult;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.interact.PlayerLeftClickEvent;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles Priming (PlayerLeftClickEvent), labeling arrows when shot (ProjectileLaunchEvent) and the can use hook.
 */
public abstract class PrimeArrowSkill extends Skill {


    protected final Set<UUID> primed = new HashSet<>();
    protected final Set<Arrow> arrows = new HashSet<>();

    public PrimeArrowSkill(DomainController dc, String name, SkillId id, SkillType skillType, ClassType classType) {
        super(dc, name, id, skillType, classType);
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerLeftClickEvent event) {
        if (!activate(event.getPlayer().getUniqueId(), event, true, false)) {
            return;
        }

        ChatUtil.sendMessage(event.getPlayer(), ChatUtil.Prefix.SKILL, Component.text("You primed ").color(NamedTextColor.GRAY)
                .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                .append(Component.text(" level ").color(NamedTextColor.GRAY))
                .append(Component.text(getSkillLevel(event.getPlayer())).color(NamedTextColor.YELLOW))
                .append(Component.text(".").color(NamedTextColor.GRAY))
        );

        primed.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow))
            return;

        if (!(arrow.getShooter() instanceof Player player))
            return;

        if (!primed.contains(player.getUniqueId()))
            return;

        setArrowOfSkill(arrow, true);
        primed.remove(player.getUniqueId());
        arrows.add(arrow);

        ChatUtil.sendSkillMessage(player, getName(), getSkillLevel(player));
        onSkillArrowLaunch(arrow, player);
    }

    protected void onSkillArrowLaunch(Arrow arrow, Player shooter) {

    }

    /**
     * Returns weather or not the arrow is from this skill
     * @param arrow that needs to be checked.
     * @return true if the arrow is an arrow of this skill.
     */
    protected boolean isArrowOfSkill(Arrow arrow) {
        return arrow.hasMetadata(getId().toString());
    }

    protected void setArrowOfSkill(Arrow arrow, boolean isOfSkill) {
        if(isOfSkill) {
            //Add metadata to arrow
            arrow.setMetadata(getId().toString(), new FixedMetadataValue(dc.getPlugin(), true));
        } else {
            //Remove metadata from arrow
            arrow.removeMetadata(getId().toString(), dc.getPlugin());
        }
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerLeftClickEvent e))
            return AttemptResult.FALSE;

        if (!e.isBow())
            return AttemptResult.FALSE;

        if (primed.contains(uuid))
            return new AttemptResult(
                    false,
                    Component.text("You have already primed ", NamedTextColor.GRAY)
                            .append(Component.text(getName(), NamedTextColor.YELLOW))
                            .append(Component.text(".", NamedTextColor.GRAY)),
                    ChatUtil.Prefix.SKILL
            );

        return super.canUseHook(uuid, event);
    }
}
