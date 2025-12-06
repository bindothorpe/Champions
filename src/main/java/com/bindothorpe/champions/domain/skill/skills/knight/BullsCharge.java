package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BullsCharge extends Skill {

    private final Map<UUID, Integer> activeMap = new HashMap<>();

    public BullsCharge(DomainController dc) {
        super(dc, SkillId.BULLS_CHARGE, SkillType.AXE, ClassType.KNIGHT, "Bull's Charge", List.of(10D, 9D, 8D, 7D), 4, 1);
    }

    @Override
    protected boolean canUseHook(UUID uuid, Event event) {
        if (!(event instanceof PlayerRightClickEvent playerRightClickEvent))
            return false;

        if (!playerRightClickEvent.isAxe())
            return false;

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        if (!activate(event.getPlayer().getUniqueId(), event)) {
            return;
        }

        double duration = 3;

        Player player = event.getPlayer();
        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.SPEED, player.getUniqueId(), getNamespacedKey(player), 2, duration);
        int taskId = dc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(
                dc.getPlugin(),
                () -> {
                    activeMap.remove(player.getUniqueId());
                },
                (long) (duration * 20L)
        );

        activeMap.put(player.getUniqueId(), taskId);
        dc.getSoundManager().playSound(player.getLocation(), CustomSound.SKILL_BULLS_CHARGE_ACTIVATE);
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        Entity damager = event.getDamager();
        if(!activeMap.containsKey(damager.getUniqueId())) return;

        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        Entity damagee = event.getDamagee();

        dc.getPlugin().getServer().getScheduler().cancelTask(activeMap.get(damager.getUniqueId()));

        StatusEffectManager.getInstance(dc).removeStatusEffectFromPlayer(StatusEffectType.SPEED, damager.getUniqueId(), getNamespacedKey(damager.getUniqueId()));
        StatusEffectManager.getInstance(dc).addStatusEffectToEntity(StatusEffectType.SLOW, damagee.getUniqueId(), getNamespacedKey(damager.getUniqueId()), 1, 2);
        dc.getSoundManager().playSound(event.getDamagee().getLocation(), CustomSound.SKILL_BULLS_CHARGE_ACTIVATE);

        activeMap.remove(damager.getUniqueId());
    }



    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}
