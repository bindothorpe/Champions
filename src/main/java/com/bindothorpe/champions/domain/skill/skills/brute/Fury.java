package com.bindothorpe.champions.domain.skill.skills.brute;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ItemUtil;
import com.bindothorpe.champions.util.actionBar.ActionBarPriority;
import com.bindothorpe.champions.util.actionBar.ActionBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Fury extends Skill implements ReloadableData {

    private final Map<UUID, Integer> furyStacksMap = new HashMap<>();
    private final Set<UUID> markedForClear = new HashSet<>();

    private double DECAY_DELAY;
    private int STACK_DECAY_RATE;
    private int MAX_STACKS;
    private int STACK_ON_DEAL_DAMAGE;
    private int STACK_ON_TAKE_DAMAGE;
    private double BASE_HEAL_PER_STACK;
    private double HEAL_PER_STACK_INCREASE_PER_LEVEL;
    private double DAMAGE_INCREASE_PER_MISSING_HEALTH;

    public Fury(DomainController dc) {
        super(dc, "Fury", SkillId.FURY, SkillType.SWORD, ClassType.BRUTE);
    }

    @EventHandler
    public void onDropItem(PlayerRightClickEvent event) {
        if(!activate(event.getPlayer().getUniqueId(), event)) return;

        performHeal(event.getPlayer());
    }

    private void performHeal(Player player) {
        player.heal(furyStacksMap.remove(player.getUniqueId()) * calculateBasedOnLevel(BASE_HEAL_PER_STACK, HEAL_PER_STACK_INCREASE_PER_LEVEL, getSkillLevel(player)));
    }

    private double getDamageIncrease(Player player) {
        if(player.getAttribute(Attribute.MAX_HEALTH) == null) return 0.0;
        double missingHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue() - player.getHealth();
        return missingHealth * DAMAGE_INCREASE_PER_MISSING_HEALTH;
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event) {
        if(event.getDamager() instanceof Player damager) {
            event.modifyDamage(getDamageIncrease(damager));
            modifyStacks(damager.getUniqueId(), STACK_ON_DEAL_DAMAGE);
        } else if (event.getDamagee() instanceof Player damagee) {
            modifyStacks(damagee.getUniqueId(), STACK_ON_TAKE_DAMAGE);
        }
    }

    private void modifyStacks(UUID uuid, int stackModification) {
        setFuryStacks(uuid, furyStacksMap.getOrDefault(uuid, 0) + stackModification);
    }

    private void setFuryStacks(UUID uuid, int stacks) {
        if(stacks > MAX_STACKS) stacks = MAX_STACKS;
        if(stacks > 0) {
            furyStacksMap.put(uuid, stacks);
        } else {
            furyStacksMap.remove(uuid);
            markedForClear.add(uuid);
        }

    }

    private void sendStacksToPlayer(UUID uuid, int stacks) {


        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;

        if(stacks == 0) {
            ActionBarUtil.sendMessage(player, Component.text(""), ActionBarPriority.LOW);
            return;
        }

        ActionBarUtil.sendMessage(
                player,
                Component.text(String.format("%s ", getName()), NamedTextColor.WHITE, TextDecoration.BOLD)
                        .append(ComponentUtil.progressBar("|", NamedTextColor.DARK_RED, NamedTextColor.GRAY, 40, (double) stacks / MAX_STACKS).decoration(TextDecoration.BOLD, false))
                        .append(Component.text(String.format(" %d", stacks), NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)),
                ActionBarPriority.LOW
        );
    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerRightClickEvent rightClickEvent)) return AttemptResult.FALSE;

        if(!isUser(uuid)) return AttemptResult.FALSE;

        if(!rightClickEvent.isSword()) return AttemptResult.FALSE;

        if(!furyStacksMap.containsKey(uuid)) return AttemptResult.FALSE;

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onPlayerLeaveSword(PlayerItemHeldEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;

        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        if(previousItem == null) return;
        if(!ItemUtil.isSword(previousItem.getType())) return;

        clearActionBar(event.getPlayer());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(!event.isTick()) return;
        for(UUID uuid : getUsers()) {
            handleDecreasingFury(uuid);
            handleDisplayingFury(uuid);
        }
    }

    private void handleDecreasingFury(UUID uuid) {
        if(!furyStacksMap.containsKey(uuid)) return;
        if(furyStacksMap.get(uuid) <= 0) return;
        if(dc.getCombatLogger().isInCombat(uuid, DECAY_DELAY)) return;

        modifyStacks(uuid, -STACK_DECAY_RATE);
    }

    private void handleDisplayingFury(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if(player == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if(!ItemUtil.isSword(item.getType())) return;

        if(markedForClear.contains(uuid)) {
            clearActionBar(player);
            markedForClear.remove(uuid);
            return;
        }

        if(!furyStacksMap.containsKey(uuid)) return;
        if(furyStacksMap.get(uuid) == 0) return;

        sendStacksToPlayer(uuid, furyStacksMap.get(uuid));
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        furyStacksMap.remove(uuid);
        markedForClear.remove(uuid);
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            DECAY_DELAY = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("decay_delay"));
            STACK_DECAY_RATE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("stack_decay_rate"));
            MAX_STACKS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_stacks"));
            STACK_ON_DEAL_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("stack_on_deal_damage"));
            STACK_ON_TAKE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("stack_on_take_damage"));
            BASE_HEAL_PER_STACK = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_heal_per_stack"));
            HEAL_PER_STACK_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("heal_per_stack_increase_per_level"));
            DAMAGE_INCREASE_PER_MISSING_HEALTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_increase_per_missing_health"));
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
                ComponentUtil.active()
                        .append(ComponentUtil.rightClick(true))
                        .append(Component.text("to consume all stacks. Heal ", NamedTextColor.GRAY))
                        .append(ComponentUtil.skillValuesBasedOnLevel(BASE_HEAL_PER_STACK, HEAL_PER_STACK_INCREASE_PER_LEVEL, skillLevel, MAX_LEVEL, NamedTextColor.YELLOW))
                        .append(Component.text(" health per Fury stack consumed.", NamedTextColor.GRAY)),
                30));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                ComponentUtil.passive()
                        .append(Component.text("Gain ", NamedTextColor.GRAY))
                        .append(Component.text(STACK_ON_DEAL_DAMAGE, NamedTextColor.GRAY))
                        .append(Component.text(STACK_ON_DEAL_DAMAGE == 1 ? " stack" : " stacks", NamedTextColor.GRAY))
                        .append(Component.text(" of Fury when you attack an enemy, and ", NamedTextColor.GRAY))
                        .append(Component.text(STACK_ON_TAKE_DAMAGE, NamedTextColor.GRAY))
                        .append(Component.text(STACK_ON_TAKE_DAMAGE == 1 ? " stack" : " stacks", NamedTextColor.GRAY))
                        .append(Component.text(" of Fury when you take damage. Maximum ", NamedTextColor.GRAY))
                        .append(Component.text(MAX_STACKS, NamedTextColor.GRAY))
                        .append(Component.text(MAX_STACKS == 1 ? " stack." : " stacks.", NamedTextColor.GRAY)),
                30));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Start losing stacks after being out of combat for ", NamedTextColor.GRAY)
                        .append(Component.text(DECAY_DELAY, NamedTextColor.GRAY))
                        .append(Component.text(DECAY_DELAY == 1.0 ? " second." : " seconds.", NamedTextColor.GRAY)),
                30));
        lore.add(Component.text(" "));
        lore.addAll(ComponentUtil.wrapComponentWithFormatting(
                Component.text("Deal bonus attack damage based on missing health.", NamedTextColor.GRAY),
                30));
        return lore;
    }
}
