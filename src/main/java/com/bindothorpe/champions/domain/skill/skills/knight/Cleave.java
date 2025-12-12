package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.*;

public class Cleave extends Skill implements ReloadableData {

    private double BASE_DAMAGE_PERCENTAGE;
    private double DAMAGE_PERCENTAGE_INCREASE_PER_LEVEL;
    private double RANGE;

    public Cleave(DomainController dc) {
        super(dc, "Cleave", SkillId.CLEAVE, SkillType.PASSIVE_A, ClassType.KNIGHT);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getDamager() instanceof Player player)) return;
        if(!isUser(player.getUniqueId())) return;

        for(Entity entity : event.getDamagee().getNearbyEntities(RANGE, RANGE, RANGE)) {
            if(!(entity instanceof LivingEntity livingEntity)) continue;

            if(!dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, player)) continue;

            if(getId().toString().equals(event.getDamageSourceString())) return;

            CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, livingEntity, player, null, event.getOriginalDamage() * calculateBasedOnLevel(BASE_DAMAGE_PERCENTAGE, DAMAGE_PERCENTAGE_INCREASE_PER_LEVEL, getSkillLevel(player.getUniqueId())), event.getAttackLocation(), CustomDamageSource.ATTACK, getId().toString());
            CustomDamageCommand command = new CustomDamageCommand(dc, customDamageEvent);
            customDamageEvent.setCommand(command);

            customDamageEvent.callEvent();

            if(customDamageEvent.isCancelled()) return;

            command.execute();
        }

    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            RANGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("range"));
            BASE_DAMAGE_PERCENTAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_damage_percentage"));
            DAMAGE_PERCENTAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_percentage_increase_per_level"));;
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }

    @Override
    public List<Component> getDescription(int skillLevel) {

        List<Component> lore = new ArrayList<>(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Your attacks deal ").color(NamedTextColor.GRAY)
                        .append(ComponentUtil.skillValuesBasedOnLevel((int) (BASE_DAMAGE_PERCENTAGE * 100), (int) (DAMAGE_PERCENTAGE_INCREASE_PER_LEVEL * 100), skillLevel, MAX_LEVEL, true, NamedTextColor.YELLOW))
                        .append(Component.text(" damage to all enemies within ")
                                .append(Component.text(String.format("%.1f", RANGE)))
                                .append(Component.text(" blocks of your target enemy.")).color(NamedTextColor.GRAY)),
                35
        ));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("This only works with Axes.").color(NamedTextColor.GRAY),
                45));
        return lore;
    }
}
