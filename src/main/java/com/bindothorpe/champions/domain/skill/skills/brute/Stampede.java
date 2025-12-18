package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.skill.ReloadableData;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public class Stampede extends Skill implements ReloadableData {

    private double BASE_SPRINT_DURATION_REQUIRED;
    private double SPRINT_DURATION_REQUIRED_DECREASE_PER_LEVEL;
    private int MAX_SPEED_STACKS;
    private double SPEED_INCREASE_PER_STACK;
    private double BASE_DAMAGE;
    private double DAMAGE_INCREASE_PER_LEVEL;
    private double KNOCKBACK_INCREASE_PER_STACK;

    private final Map<UUID, Integer> stampedeStackMap = new HashMap<>();
    private final Map<UUID, Long> sprintingStartMap = new HashMap<>();

    public Stampede(DomainController dc) {
        super(dc, "Stampede", SkillId.STAMPEDE, SkillType.PASSIVE_B, ClassType.BRUTE);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        stampedeStackMap.remove(uuid);
        sprintingStartMap.remove(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED, this);
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
    }

    @EventHandler
    public void onCustomDamageDealt(CustomDamageEvent event) {
        if(event.isCancelled()) return;
        handleStampedeHitOther(event);
        handleOtherHitStampede(event);
    }

    private void handleStampedeHitOther(CustomDamageEvent event) {
        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        if(!(event.getDamager() instanceof Player player)) return;

        if(!isUser(player)) return;

        if(!stampedeStackMap.containsKey(player.getUniqueId())) return;

        dc.getEntityStatusManager().addEntityStatus(player.getUniqueId(), new EntityStatus(
                EntityStatusType.ATTACK_KNOCKBACK_DONE,
                KNOCKBACK_INCREASE_PER_STACK * stampedeStackMap.get(player.getUniqueId()),
                0.1,
                true,
                false,
                this
        ));

        dc.getEntityStatusManager().addEntityStatus(player.getUniqueId(), new EntityStatus(
                EntityStatusType.ATTACK_DAMAGE_DONE,
                calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(player)) * stampedeStackMap.get(player.getUniqueId()),
                0.1,
                false,
                false,
                this
        ));


        dc.getSoundManager().playSound(player, CustomSound.SKILL_STAMPEDE_HIT, 0.4f * stampedeStackMap.get(player.getUniqueId()));
        cleanUser(player);
    }

    private void handleOtherHitStampede(CustomDamageEvent event) {

        if(!(event.getDamagee() instanceof Player player)) return;

        if(!isUser(player)) return;

        cleanUser(player);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.isTick()) return;

        for(Player player : getOnlineAlivePlayerUsers()) {
            if(player.isSprinting()) {
                handleSprintingUser(player);
            } else {
                cleanUser(player);
            }
        }
    }

    private void handleSprintingUser(Player player) {
        UUID uuid = player.getUniqueId();
        // Add the user if they are not added yet
        stampedeStackMap.computeIfAbsent(uuid, k -> 0);
        sprintingStartMap.computeIfAbsent(uuid, k -> System.currentTimeMillis());

        // If they reached high enough sprinting duration increase stampede stack
        if(sprintingStartMap.get(uuid) + (calculateBasedOnLevel(BASE_SPRINT_DURATION_REQUIRED, -SPRINT_DURATION_REQUIRED_DECREASE_PER_LEVEL, getSkillLevel(uuid)) * 1000L) > System.currentTimeMillis()) return;

        int currentStampedeStack = stampedeStackMap.get(uuid);

        if(currentStampedeStack == MAX_SPEED_STACKS) return;

        int newStampedeStacks = currentStampedeStack + 1;
        stampedeStackMap.put(uuid, newStampedeStacks);
        sprintingStartMap.put(uuid, System.currentTimeMillis());

        //Play sound effect, and increase move speed
        dc.getEntityStatusManager().addEntityStatus(uuid, new EntityStatus(
                EntityStatusType.MOVEMENT_SPEED,
                calculateBasedOnLevel(0.0, (SPEED_INCREASE_PER_STACK / 10), newStampedeStacks + 1),
                -1,
                false,
                false,
                this
                ));
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);
        dc.getSoundManager().playSound(player, CustomSound.SKILL_STAMPEDE_CHARGE, 0.2f * newStampedeStacks + 1.0f);
    }

    private void cleanUser(Player player) {
        UUID uuid = player.getUniqueId();
        stampedeStackMap.remove(uuid);
        sprintingStartMap.remove(uuid);
        dc.getEntityStatusManager().removeEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED, this);
        dc.getEntityStatusManager().updateEntityStatus(uuid, EntityStatusType.MOVEMENT_SPEED);

    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_SPRINT_DURATION_REQUIRED = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_sprint_duration_required"));
            SPRINT_DURATION_REQUIRED_DECREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("sprint_duration_required_decrease_per_level"));
            MAX_SPEED_STACKS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_speed_stacks"));
            SPEED_INCREASE_PER_STACK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("speed_increase_per_stack"));
            KNOCKBACK_INCREASE_PER_STACK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("knockback_increase_per_stack"));
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_damage"));
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_increase_per_level"));
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
                ComponentUtil.passive()
                        .append(Component.text("You slowly build up speed as you sprint. You gain a level of speed every ", NamedTextColor.GRAY))
                        .append(ComponentUtil.skillValuesBasedOnLevel(BASE_SPRINT_DURATION_REQUIRED, -SPRINT_DURATION_REQUIRED_DECREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                        .append(Component.text(String.format(" seconds, up to speed %s.", TextUtil.intToRoman((int) (SPEED_INCREASE_PER_STACK * MAX_SPEED_STACKS))), NamedTextColor.GRAY)),
                25));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Attacking during stampede deals ", NamedTextColor.GRAY)
                        .append(ComponentUtil.skillValuesBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                        .append(Component.text(String.format(" bonus damage per speed level, and +%d%% knockback per speed level.", (int) (KNOCKBACK_INCREASE_PER_STACK * 100)), NamedTextColor.GRAY)),
                25));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Resets if you take damage.", NamedTextColor.GRAY),
                25));
        return lore;
    }
}
