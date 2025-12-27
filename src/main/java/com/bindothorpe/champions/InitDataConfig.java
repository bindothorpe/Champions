package com.bindothorpe.champions;

import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.customItem.CustomItem;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.game.capturePoint.CapturePointManager;
import com.bindothorpe.champions.domain.game.map.GameMapListener;
import com.bindothorpe.champions.domain.item.listeners.GameItemListener;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.damage.EntityDamageListener;
import com.bindothorpe.champions.events.interact.InteractListener;
import com.bindothorpe.champions.events.interact.blocking.PlayerBlockListener;
import com.bindothorpe.champions.gui.shop.ShopPlayerGui;
import com.bindothorpe.champions.listeners.BuildListener;
import com.bindothorpe.champions.listeners.PlayerConnectionListener;
import com.bindothorpe.champions.listeners.damage.CustomDamageListener;
import com.bindothorpe.champions.listeners.game.capturepoint.CapturePointListener;
import com.bindothorpe.champions.listeners.game.equipment.EquipmentListener;
import com.bindothorpe.champions.listeners.projectile.ArrowListener;
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

        CustomItemManager cim = CustomItemManager.getInstance(dc);

        String packageName = getClass().getPackage().getName();

        for(Class<?> clazz : new Reflections(packageName + ".domain.skill.skills").getSubTypesOf(Skill.class)) {
            try {
                Skill skill = (Skill) clazz.getConstructor(DomainController.class).newInstance(dc);
                dc.getSkillManager().registerSkill(skill);
                dc.getSkillManager().reloadSkillData(skill.getId());
            } catch (Exception e) {
                System.out.println("Failed to register skill: " + clazz.getName());
            }
        }

        for(Class<?> clazz : new Reflections(packageName + ".domain.statusEffect.effects").getSubTypesOf(StatusEffect.class)) {
            try {
                StatusEffect statusEffect = (StatusEffect) clazz.getConstructor(DomainController.class).newInstance(dc);
                dc.getStatusEffectManager().registerStatusEffect(statusEffect);
            } catch (Exception e) {
                System.out.println("Failed to register status effect: " + clazz.getName());
            }
        }

        for(Class<?> clazz : new Reflections(packageName + ".domain.customItem.items").getSubTypesOf(CustomItem.class)) {
            try {
                CustomItem item = (CustomItem) clazz.getConstructor(CustomItemManager.class).newInstance(cim);
                cim.registerItem(item);
            } catch (Exception e) {
                System.out.println("Failed to register custom item: " + clazz.getName());
            }
        }

        pm.registerEvents(new EntityDamageListener(dc), dc.getPlugin());
        pm.registerEvents(new GameItemListener(dc), dc.getPlugin());
        pm.registerEvents(new InteractListener(dc), dc.getPlugin());
        pm.registerEvents(new PlayerBlockListener(dc), dc.getPlugin());
        pm.registerEvents(new BuildListener(dc), dc.getPlugin());
        pm.registerEvents(new ShopPlayerGui(dc), dc.getPlugin());
        pm.registerEvents(new CustomDamageListener(dc), dc.getPlugin());
        pm.registerEvents(CapturePointManager.getInstance(), dc.getPlugin());
        pm.registerEvents(new CapturePointListener(dc), dc.getPlugin());
        pm.registerEvents(new EquipmentListener(dc), dc.getPlugin());
        pm.registerEvents(new ArrowListener(dc), dc.getPlugin());
        pm.registerEvents(new GameMapListener(dc), dc.getPlugin());

        pm.registerEvents(new PlayerConnectionListener(dc), dc.getPlugin());

        DatabaseController dbc = dc.getDatabaseController();

        for(Player player : Bukkit.getOnlinePlayers()) {

            dc.getTeamManager().addEntityToTeam(player, TeamColor.BLUE);

            dc.getScoreboardManager().setScoreboard(player.getUniqueId());

            dc.getPlayerManager().addGold(player.getUniqueId(), 10000);
            dbc.getBuildsByPlayerUUID(player.getUniqueId(), new DatabaseResponse<List<Build>>() {
                @Override
                public void onResult(List<Build> result) {
                    for (Build build : result) {
                        dc.getPlayerManager().addBuildIdToPlayer(player.getUniqueId(), build.getClassType(), build.getId());
                        dc.getBuildManager().addBuild(build);
                    }

                    dbc.getPlayerSelectedBuildByUUID(player.getUniqueId(), new DatabaseResponse<String>() {
                        @Override
                        public void onResult(String result) {
                            dc.getPlayerManager().setSelectedBuildIdForPlayer(player.getUniqueId(), result);
                            if (result != null)
                                dc.getBuildManager().equipBuildForPlayer(player.getUniqueId(), result);
                        }
                    });
                }
            });


        }
    }
}
