package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.cooldown.CooldownEndEvent;
import com.bindothorpe.champions.events.skill.SkillUseEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ItemUtil;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Skill implements Listener {

    private final Map<UUID, Integer> users;

    protected DomainController dc;
    private final String name;
    private final SkillId id;
    private final SkillType skillType;
    private final ClassType classType;
    protected static double BASE_COOLDOWN;
    protected static double COOLDOWN_REDUCTION_PER_LEVEL;
    protected static int MAX_LEVEL;
    protected static int LEVEL_UP_COST;

    public Skill(DomainController dc,String name, SkillId id, SkillType skillType, ClassType classType) {
        this.dc = dc;
        this.name = name;
        this.id = id;
        this.skillType = skillType;
        this.classType = classType;
        this.users = new HashMap<>();
    }


    public SkillId getId() {
        return id;
    }

    public void addUser(UUID uuid, int skillLevel) {
        users.put(uuid, skillLevel);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public String getName() {
        return name;
    }

    public abstract List<Component> getDescription(int skillLevel);

    public double getBaseCooldown() {
        return BASE_COOLDOWN;
    }
    public double getCooldownReductionPerLevel() {
        return COOLDOWN_REDUCTION_PER_LEVEL;
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public int getLevelUpCost() {
        return LEVEL_UP_COST;
    }

    protected void startCooldown(UUID uuid, double overwriteCooldown) {

        double duration = 0;

        if(overwriteCooldown > 0) {
            duration = overwriteCooldown;
        } else {
            if (BASE_COOLDOWN <= 0) return;
            duration = BASE_COOLDOWN - (COOLDOWN_REDUCTION_PER_LEVEL * (users.getOrDefault(uuid, 1) - 1));

            if (duration <= 0)
                return;

        }

        double cooldownMultiplier = dc.getEntityStatusManager().getMultiplicationValue(uuid, EntityStatusType.COOLDOWN_REDUCTION) - 1;
        double cooldownReduction = duration * cooldownMultiplier;

        dc.getCooldownManager().startCooldown(uuid, this, duration - cooldownReduction);
    }

    protected void startCooldown(UUID uuid) {
        startCooldown(uuid, -1);
    }


    protected final boolean activate(UUID uuid, Event event) {
        return activate(uuid, event, true);
    }

    protected final boolean activate(UUID uuid, Event event, boolean startCooldownOnSuccess) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return false;

        if (!canUse(uuid, event))
            return false;

        SkillUseEvent skillUseEvent = new SkillUseEvent(player, getId(), users.get(uuid));
        Bukkit.getPluginManager().callEvent(skillUseEvent);

        if (skillUseEvent.isCancelled())
            return false;

        ChatUtil.sendSkillMessage(player, getName(), users.get(uuid));

        if(startCooldownOnSuccess) {
            startCooldown(uuid);
        }

        return true;
    }

    protected boolean canUse(UUID uuid, Event event) {

        if (!users.containsKey(uuid))
            return false;

        if(!canUseHook(uuid, event)) {
            return false;
        }

        if (isOnCooldown(uuid)) {
            double cooldownRemaining = getCooldownRemaining(uuid);
            Player player = Bukkit.getPlayer(uuid);

            if (player == null)
                return false;

            ChatUtil.sendMessage(player, ChatUtil.Prefix.COOLDOWN, Component.text("You cannot use ").color(NamedTextColor.GRAY)
                    .append(Component.text(this.name).color(NamedTextColor.YELLOW))
                    .append(Component.text(" for ").color(NamedTextColor.GRAY))
                    .append(Component.text(String.format(Locale.US, "%.1f", cooldownRemaining)).color(NamedTextColor.YELLOW))
                    .append(Component.text(" seconds").color(NamedTextColor.GRAY)));

            return false;
        }


        return true;
    }

    public boolean isOnCooldown(UUID uuid) {
        return dc.getCooldownManager().isOnCooldown(uuid, this);
    }

    private double getCooldownRemaining(UUID uuid) {
        return dc.getCooldownManager().getCooldownRemaining(uuid, this);
    }

    protected boolean canUseHook(UUID uuid, Event event) {
        return true;
    }

    protected boolean isUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public ClassType getClassType() {
        return classType;
    }

    protected int getSkillLevel(UUID uuid) {
        return users.get(uuid);
    }

    protected Set<UUID> getUsers() {
        return users.keySet();
    }

    @EventHandler
    public final void onCooldownEnd(CooldownEndEvent event) {
        if (!equals(event.getSource()))
            return;

        Player player = Bukkit.getPlayer(event.getUuid());

        if (player == null)
            return;

        ChatUtil.sendMessage(player, ChatUtil.Prefix.COOLDOWN, Component.text("You can use ").color(NamedTextColor.GRAY)
                .append(Component.text(this.name).color(NamedTextColor.YELLOW))
                .append(Component.text(" again.").color(NamedTextColor.GRAY)));
        dc.getSoundManager().playSound(player, CustomSound.SKILL_COOLDOWN_END);

        onCooldownEnd(event.getUuid(), event.getSource());

        if(canDisplayOnSkillType() && isItemStackOfSkillType(player.getInventory().getItemInMainHand())) {
            clearActionBar(player);
        }
    }

    public void onCooldownEnd(UUID uuid, Object source) {
    }

    protected NamespacedKey getNamespacedKey(@NotNull Player player) {
        return getNamespacedKey(player.getUniqueId());
    }


    protected NamespacedKey getNamespacedKey(@NotNull UUID playerUUID) {
        return new NamespacedKey(dc.getPlugin(), String.format("%s_%s", playerUUID, getId()));
    }

    private void clearActionBar(Player player) {
        ChatUtil.sendActionBarMessage(player, Component.text(""));
    }

    @EventHandler
    public void onPlayerLeaveCurrentItemSlot(PlayerItemHeldEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;
        if(!canDisplayOnSkillType()) return;
        if(!isOnCooldown(event.getPlayer().getUniqueId())) return;

        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());

        if(!isItemStackOfSkillType(previousItem)) return;

        clearActionBar(event.getPlayer());
    }

    @EventHandler
    public void onTick(UpdateEvent event) {

        if(!canDisplayOnSkillType()) return;

        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid : users.keySet()) {
            if(!isOnCooldown(uuid)) continue;

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            if(!isItemStackOfSkillType(player.getInventory().getItemInMainHand())) continue;

            double cooldownReminingInSeconds = getCooldownRemaining(uuid);
            double cooldownPercentage = dc.getCooldownManager().getCooldownPercentage(uuid, this);

            ChatUtil.sendActionBarMessage(player, ComponentUtil.cooldownRemainingBar(name, cooldownPercentage, cooldownReminingInSeconds));
        }
    }

    private boolean canDisplayOnSkillType() {
        return skillType == SkillType.SWORD || skillType == SkillType.AXE || skillType == SkillType.BOW;
    }

    private boolean isItemStackOfSkillType(ItemStack itemStack) {
        if(itemStack == null) return false;

        if(skillType == SkillType.SWORD && ItemUtil.isSword(itemStack.getType())) return true;
        if(skillType == SkillType.BOW && ItemUtil.isBow(itemStack.getType())) return true;
        return skillType == SkillType.AXE && ItemUtil.isAxe(itemStack.getType());
    }
}
