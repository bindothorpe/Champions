package com.bindothorpe.champions;

import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.combat.CombatListener;
import com.bindothorpe.champions.domain.item.listeners.GameItemListener;
import com.bindothorpe.champions.domain.skill.skills.assassin.AssassinPassive;
import com.bindothorpe.champions.domain.skill.skills.brute.ExplosiveBomb;
import com.bindothorpe.champions.domain.skill.skills.brute.HeadButt;
import com.bindothorpe.champions.domain.skill.skills.global.TestSkill;
import com.bindothorpe.champions.domain.skill.skills.mage.Explosion;
import com.bindothorpe.champions.domain.skill.skills.ranger.BouncingArrow;
import com.bindothorpe.champions.domain.skill.skills.ranger.HuntersHeart;
import com.bindothorpe.champions.domain.skill.skills.ranger.KitingArrow;
import com.bindothorpe.champions.domain.skill.skills.mage.IcePrison;
import com.bindothorpe.champions.domain.statusEffect.effects.RootStatusEffect;
import com.bindothorpe.champions.domain.statusEffect.effects.StunStatusEffect;
import com.bindothorpe.champions.events.damage.EntityDamageByEntityListener;
import com.bindothorpe.champions.events.interact.InteractListener;
import com.bindothorpe.champions.listeners.BuildListener;
import com.bindothorpe.champions.listeners.PlayerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.List;

public class InitDataConfig {

    private final DomainController dc;

    public InitDataConfig(DomainController dc) {
        this.dc = dc;
    }

    public void initialize() {
        PluginManager pm = Bukkit.getPluginManager();
        dc.registerSkill(new IcePrison(dc));
        dc.registerSkill(new TestSkill(dc));
//        dc.registerSkill(new TestSkill2(dc));
        dc.registerSkill(new KitingArrow(dc));
        dc.registerSkill(new BouncingArrow(dc));
        dc.registerSkill(new Explosion(dc));
        dc.registerSkill(new AssassinPassive(dc));
        dc.registerSkill(new HuntersHeart(dc));
        dc.registerSkill(new ExplosiveBomb(dc));
        dc.registerSkill(new HeadButt(dc));

        dc.registerStatusEffect(new RootStatusEffect(dc));
        dc.registerStatusEffect(new StunStatusEffect(dc));

        pm.registerEvents(new EntityDamageByEntityListener(dc), dc.getPlugin());
        pm.registerEvents(new GameItemListener(dc), dc.getPlugin());
        pm.registerEvents(new CombatListener(dc), dc.getPlugin());
        pm.registerEvents(new InteractListener(), dc.getPlugin());
        pm.registerEvents(new BuildListener(dc), dc.getPlugin());

        pm.registerEvents(new PlayerConnectionListener(dc), dc.getPlugin());

        DatabaseController dbc = dc.getDatabaseController();

        for(Player player : Bukkit.getOnlinePlayers()) {
            dbc.getBuildsByPlayerUUID(player.getUniqueId(), new DatabaseResponse<List<Build>>() {
                @Override
                public void onResult(List<Build> result) {
                    for (Build build : result) {
                        dc.addBuildIdToPlayer(player.getUniqueId(), build.getClassType(), build.getId());
                        dc.addBuild(build);
                    }
                }
            });

            dbc.getPlayerSelectedBuildByUUID(player.getUniqueId(), new DatabaseResponse<String>() {
                @Override
                public void onResult(String result) {
                    dc.setSelectedBuildIdForPlayer(player.getUniqueId(), result);
                    if (result != null)
                        dc.equipBuildForPlayer(player.getUniqueId(), result);
                }
            });
        }
    }
}
