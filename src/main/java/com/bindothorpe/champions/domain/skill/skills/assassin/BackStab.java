package com.bindothorpe.champions.domain.skill.skills.assassin;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.List;

public class BackStab extends Skill implements ReloadableData {

    private static double DAMAGE_MOD = 4.0;

    public BackStab(DomainController dc) {
        super(dc, "Back Stab", SkillId.BACK_STAB, SkillType.PASSIVE_B, ClassType.ASSASSIN);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageFromBehind(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!event.getCause().equals(CustomDamageEvent.DamageCause.ATTACK)) return;

        if(!(event.getDamager() instanceof Player player)) return;

        if(!isUser(player.getUniqueId())) return;

        if(!isAttackFromBehind(player, event.getDamagee())) return;

        event.modifyDamage(DAMAGE_MOD);
    }

    private boolean isAttackFromBehind(Entity damager, LivingEntity damagee) {
        if(damager == null || damagee == null) return false;

        Vector lookingDirection = damagee.getLocation().getDirection().setY(0).normalize();
        Vector attackerLookingDirection = damager.getLocation().toVector().subtract(damagee.getLocation().toVector()).setY(0).normalize();
        Vector check = new Vector(lookingDirection.getX() * -1.0D, 0.0D, lookingDirection.getZ() * -1.0D);

        return !(check.subtract(attackerLookingDirection).length() >= 0.8D);

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of(
                ComponentUtil.passive().append(
                Component.text("Attacks from behind").color(NamedTextColor.GRAY)),

                Component.text("opponents deal ").color(NamedTextColor.GRAY)
                        .append(ComponentUtil.skillLevelValues(skillLevel, List.of(DAMAGE_MOD), NamedTextColor.YELLOW)
                        .append(Component.text(" additional").color(NamedTextColor.GRAY))),

                Component.text("damage.").color(NamedTextColor.GRAY)
                );
    }


    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.back_stab.max_level");
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt("skills.assassin.back_stab.level_up_cost");
            DAMAGE_MOD = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble("skills.assassin.back_stab.damage_mod");
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
}
