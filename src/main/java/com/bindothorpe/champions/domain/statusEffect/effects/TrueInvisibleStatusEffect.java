package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrueInvisibleStatusEffect extends StatusEffect {

    public TrueInvisibleStatusEffect(DomainController dc) {
        super(dc, "True Invisible", StatusEffectType.TRUE_INVISIBLE);
    }

    @Override
    public void handleEntityValueChanged(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);

        if(entity == null) return;


        for(Player player: Bukkit.getOnlinePlayers()) {
            if(isActive(uuid)) {
                player.hideEntity(dc.getPlugin(), entity);
            } else {
                player.showEntity(dc.getPlugin(), entity);
            }
        }


        if(entity instanceof Player player) {
            if(isActive(uuid)) {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.EFFECT, Component.text("You are invisible.").color(NamedTextColor.GRAY));
            } else {
                ChatUtil.sendMessage(player, ChatUtil.Prefix.EFFECT, Component.text("You are no longer invisible.").color(NamedTextColor.GRAY));
            }
        }

    }
}
