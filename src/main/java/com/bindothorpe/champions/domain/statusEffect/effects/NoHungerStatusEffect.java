package com.bindothorpe.champions.domain.statusEffect.effects;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.UUID;

public class NoHungerStatusEffect extends StatusEffect {
    public NoHungerStatusEffect(DomainController dc) {
        super(dc, "No Hunger", StatusEffectType.NO_HUNGER);
    }

    @Override
    public void handleEntityValueChanged(UUID uuid) {
        if(isActive(uuid)) {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null) return;

            player.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if(event.getUpdateType().equals(UpdateType.FIVE_SECOND)) {
            for(UUID uuid : getActiveUserUUIDs()) {
                Player player = Bukkit.getPlayer(uuid);

                if(player == null) return;

                player.setFoodLevel(20);
            }
        }
    }
}
