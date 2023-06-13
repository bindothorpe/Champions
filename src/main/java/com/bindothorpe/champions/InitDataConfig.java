package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.item.listeners.GameItemListener;
import com.bindothorpe.champions.domain.skill.skills.assassin.AssassinPassive;
import com.bindothorpe.champions.domain.skill.skills.mage.Explosion;
import com.bindothorpe.champions.domain.skill.skills.ranger.BouncingArrow;
import com.bindothorpe.champions.domain.skill.skills.ranger.KitingArrow;
import com.bindothorpe.champions.domain.skill.skills.mage.IcePrison;
import com.bindothorpe.champions.domain.skill.skills.global.TestSkill2;
import com.bindothorpe.champions.events.damage.EntityDamageByEntityListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class InitDataConfig {

    private final DomainController dc;

    public InitDataConfig(DomainController dc) {
        this.dc = dc;
    }

    public void initialize() {
        PluginManager pm = Bukkit.getPluginManager();
        dc.registerSkill(new IcePrison(dc));
        dc.registerSkill(new TestSkill2(dc));
        dc.registerSkill(new KitingArrow(dc));
        dc.registerSkill(new BouncingArrow(dc));
        dc.registerSkill(new Explosion(dc));
        dc.registerSkill(new AssassinPassive(dc));
        pm.registerEvents(new EntityDamageByEntityListener(dc), dc.getPlugin());
        pm.registerEvents(new GameItemListener(dc), dc.getPlugin());

    }
}
