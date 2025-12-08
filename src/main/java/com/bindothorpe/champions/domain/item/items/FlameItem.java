package com.bindothorpe.champions.domain.item.items;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.command.damage.CustomDamageCommand;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.events.damage.CustomDamageEvent;
import com.bindothorpe.champions.events.damage.CustomDamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class FlameItem extends GameItem {

    private final double collisionDamage;

    public FlameItem(DomainController dc, Entity owner, double collisionDamage, SkillId sourceSkillId) {
        super(dc, Material.BLAZE_POWDER, 0.7D, owner, 1.0, 0.15);
        this.collisionDamage = collisionDamage;
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

        CustomDamageEvent customDamageEvent = new CustomDamageEvent(dc, livingEntity, (LivingEntity) getOwner(), collisionDamage, getLocation(), CustomDamageSource.SKILL_PROJECTILE, dc.getSkillManager().getSkillName(SkillId.FLESH_HOOK));
        CustomDamageCommand customDamageCommand = new CustomDamageCommand(dc, customDamageEvent);
        customDamageEvent.setCommand(customDamageCommand);

        Bukkit.getPluginManager().callEvent(customDamageEvent);

        if(dc.getTeamManager().getTeamFromEntity(livingEntity) != null && dc.getTeamManager().getTeamFromEntity(livingEntity).equals(dc.getTeamManager().getTeamFromEntity(getOwner())) && (!livingEntity.equals(getOwner())))
            return;

        if(customDamageEvent.isCancelled())
            return;

        customDamageCommand.execute();
        remove();

    }

    @Override
    public void onCollideWithBlock(Block block) {
        remove();
    }

    @Override
    public void onDespawn() {

    }
}
