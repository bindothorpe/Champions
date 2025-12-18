package com.bindothorpe.champions.domain.skill.skills.ranger;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.LotusTrapItem;
import com.bindothorpe.champions.domain.skill.*;
import com.bindothorpe.champions.domain.sound.CustomSound;
import com.bindothorpe.champions.events.interact.PlayerDropItemWrapperEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.timer.Timer;
import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import com.bindothorpe.champions.util.ItemUtil;
import com.bindothorpe.champions.util.actionBar.ActionBarPriority;
import com.bindothorpe.champions.util.actionBar.ActionBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LotusTrap extends Skill implements ReloadableData {

    private int BASE_CHARGE_COUNT;
    private int CHARGE_COUNT_INCREASE_PER_LEVEL;
    private double BASE_DAMAGE;
    private double DAMAGE_INCREASE_PER_LEVEL;
    private double BASE_SLOW;
    private double SLOW_INCREASE_PER_LEVEL;
    private double DETECTION_RADIUS;
    private double EXPLOSION_RADIUS;
    private double LAUNCH_STRENGTH;
    private double BASE_DURATION;
    private double DURATION_INCREASE_PER_LEVEL;
    private double ACTIVATION_DELAY;
    private double TRIGGER_DELAY;

    private final Map<UUID, Integer> trapCharges = new HashMap<>();
    private final Map<UUID, Timer> timerMap = new HashMap<>();
    private final Map<UUID, Set<LotusTrapItem>> lotusTrapItemMap = new HashMap<>();

    public LotusTrap(DomainController dc) {
        super(dc, "Lotus Trap", SkillId.LOTUS_TRAP, SkillType.PASSIVE_A, ClassType.RANGER);
    }

    @Override
    public void onRemoveUser(UUID uuid) {
        // Remove all remaining lotus trap items
        Set<LotusTrapItem> traps = lotusTrapItemMap.get(uuid);
        if(traps == null) return;
        if(!traps.isEmpty()) {
            for(LotusTrapItem item : traps) {
                item.remove();
            }
        }

        lotusTrapItemMap.remove(uuid);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemWrapperEvent event) {
        if(!isUser(event.getPlayer())) return;

        UUID uuid = event.getPlayer().getUniqueId();

        if(!activate(uuid, event, false)) return;

        trapCharges.put(uuid, trapCharges.get(uuid) - 1);

        LotusTrapItem item = new LotusTrapItem(
                dc,
                event.getPlayer(),
                DETECTION_RADIUS,
                calculateBasedOnLevel(BASE_DURATION, DURATION_INCREASE_PER_LEVEL, getSkillLevel(uuid)),
                ACTIVATION_DELAY,
                TRIGGER_DELAY,
                EXPLOSION_RADIUS,
                calculateBasedOnLevel(BASE_DAMAGE, DAMAGE_INCREASE_PER_LEVEL, getSkillLevel(uuid)),
                calculateBasedOnLevel(BASE_SLOW, SLOW_INCREASE_PER_LEVEL, getSkillLevel(uuid)),
                true
                );

        dc.getGameItemManager().spawnGameItem(
                item,
                event.getPlayer().getEyeLocation(),
                event.getPlayer().getLocation().getDirection(),
                LAUNCH_STRENGTH // 0.4
        );

        lotusTrapItemMap.computeIfAbsent(uuid, k -> new HashSet<>());
        lotusTrapItemMap.get(uuid).add(item);
        item.onRemove = () -> lotusTrapItemMap.get(uuid).remove(item);

    }

    @Override
    protected AttemptResult canUseHook(UUID uuid, Event event) {
        if(!(event instanceof PlayerDropItemWrapperEvent dropItemEvent)) return AttemptResult.FALSE;

        trapCharges.computeIfAbsent(uuid, k -> calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(uuid)));

        if(trapCharges.get(uuid) == 0) {
            return new AttemptResult(
                    false,
                    Component.text("You cannot use ").color(NamedTextColor.GRAY)
                            .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                            .append(Component.text(" for ").color(NamedTextColor.GRAY))
                            .append(Component.text(String.format(Locale.US, "%.1f", timerMap.get(uuid).getTimeLeftInSeconds())).color(NamedTextColor.YELLOW))
                            .append(Component.text(" seconds").color(NamedTextColor.GRAY)),
                    ChatUtil.Prefix.COOLDOWN
            );
        }

        return super.canUseHook(uuid, event);
    }

    @EventHandler
    public void onPlayerLeaveCurrentItemSlotForCharges(PlayerItemHeldEvent event) {
        if(!isUser(event.getPlayer().getUniqueId())) return;
//        if(!canDisplayOnSkillType()) return;

        ItemStack previousItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());

        if(!isItemStackOfSkillType(previousItem)) return;

        clearActionBar(event.getPlayer());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {

        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        for(UUID uuid : getUsers()) {
            trapCharges.computeIfAbsent(uuid, k -> calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(uuid)));

            Player player = Bukkit.getPlayer(uuid);

            if(player == null) continue;

            // Start cooldown logic

            if(trapCharges.get(uuid) < calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(player))) {
                if(timerMap.get(uuid) == null || !timerMap.get(uuid).isRunning()) {
                    Timer timer = new Timer(
                            dc.getPlugin(),
                            calculateBasedOnLevel(BASE_COOLDOWN, COOLDOWN_REDUCTION_PER_LEVEL, getSkillLevel(player)),
                            () -> {
                                trapCharges.put(uuid, trapCharges.get(uuid) + 1);
                                timerMap.remove(uuid);
                                dc.getSoundManager().playSound(player, CustomSound.SKILL_COOLDOWN_END);
                            }
                    );
                    timer.start();
                    timerMap.put(uuid, timer);
                }
            }

            if(!ItemUtil.isBow(player.getInventory().getItemInMainHand().getType())) continue;

//            double percentage = timerMap.containsKey(uuid) ? timerMap.get(uuid).getPercentage() : -1;

            ActionBarUtil.sendMessage(
                    player,
                    ComponentUtil.skillCharges(null, trapCharges.get(player.getUniqueId()), calculateBasedOnLevel(BASE_CHARGE_COUNT, CHARGE_COUNT_INCREASE_PER_LEVEL, getSkillLevel(player)), -1),
                    ActionBarPriority.LOW
            );

        }

    }

    @Override
    public boolean isItemStackOfSkillType(ItemStack itemStack) {
        if(itemStack == null) return false;

        return getSkillType() == SkillType.PASSIVE_A && ItemUtil.isBow(itemStack.getType());
    }

    @Override
    public boolean onReload() {
        try {
            MAX_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("max_level"));
            LEVEL_UP_COST = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("level_up_cost"));
            BASE_COOLDOWN = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_cooldown"));
            COOLDOWN_REDUCTION_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("cooldown_reduction_per_level"));
            BASE_CHARGE_COUNT = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("base_charge_count"));
            CHARGE_COUNT_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getInt(getConfigPath("charge_count_increase_per_level"));
            BASE_DAMAGE = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_damage"));
            DAMAGE_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("damage_increase_per_level"));
            BASE_DURATION = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_duration"));
            DURATION_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("duration_increase_per_level"));
            ACTIVATION_DELAY = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("activation_duration"));
            TRIGGER_DELAY = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("trigger_delay"));
            BASE_SLOW = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("base_slow"));
            SLOW_INCREASE_PER_LEVEL = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("slow_increase_per_level"));
            DETECTION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("detection_radius"));
            EXPLOSION_RADIUS = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("explosion_radius"));
            LAUNCH_STRENGTH = dc.getCustomConfigManager().getConfig("skill_config").getFile().getDouble(getConfigPath("launch_strength"));
            dc.getPlugin().getLogger().info(String.format("Successfully reloaded %s.", getName()));
            return true;
        } catch (Exception e) {
            dc.getPlugin().getLogger().warning(String.format("Failed to reload %s.", getName()));
            return false;
        }
    }
    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }
}