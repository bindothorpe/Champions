package com.bindothorpe.champions.domain.skill.skills.knight;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusManager;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.items.ExplosiveItem;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import com.bindothorpe.champions.events.interact.PlayerRightClickEvent;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;

public class Riposte extends Skill {

    private static final double RIPOSITE_DURATION = 1.0D;

    private final Map<UUID, Long> blockingUsersMap = new HashMap<>();
    private final Set<UUID> blockedAttackUsersSet = new HashSet<>();


    public Riposte(DomainController dc) {
        super(dc, SkillId.RIPOSTE, SkillType.SWORD, ClassType.KNIGHT, "Riposte", List.of(14d, 13d, 12d, 11d, 10d), 5, 1);
    }

    @EventHandler
    public void onRightClick(PlayerRightClickEvent event) {
        Player player = event.getPlayer();

        if(!event.isSword())
            return;

        if(blockingUsersMap.containsKey(player.getUniqueId()))
            return;

        if (!activate(player.getUniqueId(), event))
            return;


        //Add the user to the map
        blockingUsersMap.put(player.getUniqueId(), System.currentTimeMillis() + ((long) RIPOSITE_DURATION * 1000L));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCustomDamageBlock(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        if(event.getDamagee() == null) return;

        if(!(event.getDamagee() instanceof Player damagee)) return;

        if(!damagee.isBlocking()) return;

        if(!blockingUsersMap.containsKey(damagee.getUniqueId())) return;

        if(event.getDamager() == null) return;

        if(blockedAttackUsersSet.contains(damagee.getUniqueId())) return;

        event.setCancelled(true);

        this.blockedAttackUsersSet.add(damagee.getUniqueId());
        this.blockingUsersMap.put(damagee.getUniqueId(), System.currentTimeMillis() + ((long) RIPOSITE_DURATION * 1000L));

        ChatUtil.sendMessage(
                damagee,
                ChatUtil.Prefix.SKILL,
                Component.text("You blocked with ").color(NamedTextColor.GRAY)
                        .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                        .append(Component.text(" level ").color(NamedTextColor.GRAY))
                        .append(Component.text(getSkillLevel(damagee.getUniqueId())).color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.GRAY)));

    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onCustomDamageRiposte(CustomDamageEvent event) {
        if(event.isCancelled()) return;

        if(!event.getSource().equals(CustomDamageSource.ATTACK)) return;

        if(event.getDamager() == null) return;

        if(!blockingUsersMap.containsKey(event.getDamager().getUniqueId())) return;

        if(!blockedAttackUsersSet.contains(event.getDamager().getUniqueId())) return;

        blockingUsersMap.remove(event.getDamager().getUniqueId());
        blockedAttackUsersSet.remove(event.getDamager().getUniqueId());
        event.getCommand().damage(event.getCommand().getDamage() + 2D);
//        EntityStatusManager.getInstance(dc).addEntityStatus(event.getDamager().getUniqueId(), new EntityStatus(
//                EntityStatusType.ATTACK_DAMAGE_DONE,
//                2.0D,
//                0.1D,
//                false,
//                false,
//                this
//        ));


    }

    @EventHandler
    public void onExpire(UpdateEvent event) {
        if(!event.getUpdateType().equals(UpdateType.TICK)) return;

        Set<UUID> expiredUsersSet = new HashSet<>();
        blockingUsersMap.entrySet().stream()
                .filter((entry) -> System.currentTimeMillis() > entry.getValue())
                .forEach((entry) -> expiredUsersSet.add(entry.getKey()));

        expiredUsersSet
                .forEach((uuid) -> {
                    blockingUsersMap.remove(uuid);
                    blockedAttackUsersSet.remove(uuid);
                    //Send a fail message
                    Player player = Bukkit.getPlayer(uuid);
                    if(player == null) return;
                    ChatUtil.sendMessage(
                            player,
                            ChatUtil.Prefix.SKILL,
                            Component.text("You failed to ").color(NamedTextColor.GRAY)
                                    .append(Component.text(getName()).color(NamedTextColor.YELLOW))
                                    .append(Component.text(".").color(NamedTextColor.GRAY))
                                    );
                });

    }

    @Override
    public List<Component> getDescription(int skillLevel) {
        return List.of();
    }

}
