package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.timer.Timer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FlameItem extends GameItem {

    private final double collisionDamage;
    private final double despawnDelay;

    public FlameItem(DomainController dc, Entity owner, double collisionDamage, double despawnDelay, SkillId sourceSkillId) {
        super(dc, Material.BLAZE_POWDER, 0.7D, owner, 1.0, 0.15);
        this.collisionDamage = collisionDamage;
        this.despawnDelay = despawnDelay;
    }
    public FlameItem(DomainController dc, Entity owner, double collisionDamage, SkillId sourceSkillId) {
        this(dc, owner, collisionDamage, 0, sourceSkillId);
    }


    @Override
    public void onTickUpdate() {

    }

    @Override
    public void onRapidUpdate() {

    }

    @Override
    public void onCollide(Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) {
            return;
        }



        CustomDamageEvent customDamageEvent = CustomDamageEvent.getBuilder()
                .setDamager((LivingEntity) getOwner())
                .setDamagee(livingEntity)
                .setDamage(collisionDamage)
                .setCause(CustomDamageEvent.DamageCause.SKILL)
                .setCauseDisplayName(dc.getSkillManager().getSkillName(SkillId.INFERNO)) //TODO: Add skill source for game item, because flame item can also be spawned by immolate.
                .build();

        if(!dc.getTeamManager().areEntitiesOnDifferentTeams(livingEntity, getOwner()) || livingEntity.equals(getOwner()))
            customDamageEvent.setCancelled(true);

        customDamageEvent.callEvent();


        if(customDamageEvent.isCancelled())
            return;

        new CustomDamageCommand(dc, customDamageEvent).execute();
        remove();

    }

    @Override
    public void onCollideWithBlock(Block block) {
        if(despawnDelay > 0) {
            new Timer(dc.getPlugin(), despawnDelay, this::remove);
        } else {
            remove();
        }

    }

    @Override
    public void onDespawn() {

    }
}
