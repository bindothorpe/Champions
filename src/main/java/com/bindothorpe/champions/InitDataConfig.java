package com.bindothorpe.champions;

import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.combat.CombatListener;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemId;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.customItem.items.DuskBlade;
import com.bindothorpe.champions.domain.customItem.items.LongSword;
import com.bindothorpe.champions.domain.customItem.items.Phage;
import com.bindothorpe.champions.domain.customItem.items.SerratedDirk;
import com.bindothorpe.champions.domain.game.GameListener;
import com.bindothorpe.champions.domain.game.GameManager;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePointManager;
import com.bindothorpe.champions.domain.item.listeners.GameItemListener;
import com.bindothorpe.champions.domain.skill.skills.assassin.AssassinPassive;
import com.bindothorpe.champions.domain.skill.skills.brute.ExplosiveBomb;
import com.bindothorpe.champions.domain.skill.skills.brute.GrandEntrance;
import com.bindothorpe.champions.domain.skill.skills.brute.HeadButt;
import com.bindothorpe.champions.domain.skill.skills.global.Rally;
import com.bindothorpe.champions.domain.skill.skills.global.TestSkill;
import com.bindothorpe.champions.domain.skill.skills.mage.Explosion;
import com.bindothorpe.champions.domain.skill.skills.mage.MagePassive;
import com.bindothorpe.champions.domain.skill.skills.ranger.BouncingArrow;
import com.bindothorpe.champions.domain.skill.skills.ranger.HuntersHeart;
import com.bindothorpe.champions.domain.skill.skills.ranger.KitingArrow;
import com.bindothorpe.champions.domain.skill.skills.mage.IcePrison;
import com.bindothorpe.champions.domain.skill.skills.ranger.SonarArrow;
import com.bindothorpe.champions.domain.statusEffect.effects.RootStatusEffect;
import com.bindothorpe.champions.domain.statusEffect.effects.StunStatusEffect;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.damage.EntityDamageByEntityListener;
import com.bindothorpe.champions.events.interact.InteractListener;
import com.bindothorpe.champions.gui.shop.ShopPlayerGui;
import com.bindothorpe.champions.listeners.BuildListener;
import com.bindothorpe.champions.listeners.PlayerConnectionListener;
import com.bindothorpe.champions.listeners.damage.CustomDamageListener;
import com.bindothorpe.champions.listeners.game.capturepoint.CapturePointListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.reflections.Reflections;

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
        dc.registerSkill(new MagePassive(dc));
        dc.registerSkill(new HuntersHeart(dc));
        dc.registerSkill(new ExplosiveBomb(dc));
        dc.registerSkill(new HeadButt(dc));
        dc.registerSkill(new Rally(dc));
        dc.registerSkill(new SonarArrow(dc));
        dc.registerSkill(new GrandEntrance(dc));

        dc.registerStatusEffect(new RootStatusEffect(dc));
        dc.registerStatusEffect(new StunStatusEffect(dc));

        CustomItemManager cim = CustomItemManager.getInstance(dc);

        String packageName = getClass().getPackage().getName();

        for(Class<?> clazz : new Reflections(packageName + ".domain.customItem.items").getSubTypesOf(CustomItem.class)) {
            try {
                CustomItem item = (CustomItem) clazz.getConstructor(CustomItemManager.class).newInstance(cim);
                cim.registerItem(item);
            } catch (Exception e) {
                System.out.println("Failed to register custom item: " + clazz.getName());
            }
        }


        pm.registerEvents(new EntityDamageByEntityListener(dc), dc.getPlugin());
        pm.registerEvents(new GameItemListener(dc), dc.getPlugin());
        pm.registerEvents(new CombatListener(dc), dc.getPlugin());
        pm.registerEvents(new InteractListener(), dc.getPlugin());
        pm.registerEvents(new BuildListener(dc), dc.getPlugin());
        pm.registerEvents(new GameListener(dc), dc.getPlugin());
        pm.registerEvents(new ShopPlayerGui(dc), dc.getPlugin());
        pm.registerEvents(new CustomDamageListener(dc), dc.getPlugin());
        pm.registerEvents(CapturePointManager.getInstance(), dc.getPlugin());
        pm.registerEvents(new CapturePointListener(dc), dc.getPlugin());

        pm.registerEvents(new PlayerConnectionListener(dc), dc.getPlugin());

        DatabaseController dbc = dc.getDatabaseController();

        for(Player player : Bukkit.getOnlinePlayers()) {

            dc.addEntityToTeam(player, TeamColor.BLUE);

            dc.getScoreboardManager().setScoreboard(player.getUniqueId());

            dc.addGold(player.getUniqueId(), 10000);
            dbc.getBuildsByPlayerUUID(player.getUniqueId(), new DatabaseResponse<List<Build>>() {
                @Override
                public void onResult(List<Build> result) {
                    for (Build build : result) {
                        dc.addBuildIdToPlayer(player.getUniqueId(), build.getClassType(), build.getId());
                        dc.addBuild(build);
                    }

                    dbc.getPlayerSelectedBuildByUUID(player.getUniqueId(), new DatabaseResponse<String>() {
                        @Override
                        public void onResult(String result) {
                            dc.setSelectedBuildIdForPlayer(player.getUniqueId(), result);
                            if (result != null)
                                dc.equipBuildForPlayer(player.getUniqueId(), result);
                        }
                    });
                }
            });


        }
    }
}
