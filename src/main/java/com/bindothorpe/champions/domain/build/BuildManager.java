package com.bindothorpe.champions.domain.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.build.EquipBuildEvent;
import com.bindothorpe.champions.events.build.UnequipBuildEvent;
import com.bindothorpe.champions.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BuildManager {

    private DomainController dc;
    private static BuildManager instance;
    private Map<String, Build> buildMap;
    private Map<ClassType, ItemStack[]> armorContentsMap;

    private BuildManager(DomainController dc) {
        this.dc = dc;
        this.buildMap = new HashMap<>();
        this.armorContentsMap = new HashMap<>();
        armorContentsMap.put(ClassType.ASSASSIN, new ItemStack[]{new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)});
        armorContentsMap.put(ClassType.RANGER, new ItemStack[]{new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)});
        armorContentsMap.put(ClassType.KNIGHT, new ItemStack[]{new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS), new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)});
        armorContentsMap.put(ClassType.MAGE, new ItemStack[]{new ItemStack(Material.GOLDEN_BOOTS), new ItemStack(Material.GOLDEN_LEGGINGS), new ItemStack(Material.GOLDEN_CHESTPLATE), new ItemStack(Material.GOLDEN_HELMET)});
        armorContentsMap.put(ClassType.BRUTE, new ItemStack[]{new ItemStack(Material.DIAMOND_BOOTS), new ItemStack(Material.DIAMOND_LEGGINGS), new ItemStack(Material.DIAMOND_CHESTPLATE), new ItemStack(Material.DIAMOND_HELMET)});
    }

    public static BuildManager getInstance(DomainController dc) {
        if (instance == null)
            instance = new BuildManager(dc);

        return instance;
    }

    public Map<ClassType, Set<Build>> getBuilds(Collection<String> buildsIds) {
        Map<ClassType, Set<Build>> builds = new HashMap<>();
        for (ClassType type : ClassType.values()) {
            builds.put(type, new HashSet<>());
        }

        for (String buildId : buildsIds) {
            Build build = buildMap.get(buildId);
            if (build == null) {
                throw new RuntimeException(String.format("Build with id \"%s\" not found.", buildId));
            }

            builds.get(build.getClassType()).add(build);
        }

        return builds;
    }

    public Build getBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build;
    }

    public String createEmptyBuild(ClassType classType) {
        Build build = new Build(classType);
        String buildId = build.getId();

        switch (classType) {
            case ASSASSIN -> build.levelUpSkill(SkillType.CLASS_PASSIVE, SkillId.ASSASSIN_PASSIVE, 1, 0);
            case MAGE -> build.levelUpSkill(SkillType.CLASS_PASSIVE, SkillId.MAGE_PASSIVE, 1, 0);
            default -> System.out.println("No default skill for class " + classType);
        }

        buildMap.put(buildId, build);
        return buildId;
    }

    public Build deleteBuild(String buildId) {
        return buildMap.remove(buildId);
    }

    public boolean levelUpSkillForBuild(String buildId, SkillId skillId) {
        Build build = buildMap.get(buildId);

        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        int maxLevel = dc.getSkillManager().getSkillMaxLevel(skillId);
        int cost = dc.getSkillManager().getSkillLevelUpCost(skillId);
        SkillType skillType = dc.getSkillManager().getSkillType(skillId);

        return build.levelUpSkill(skillType, skillId, maxLevel, cost);
    }

    public boolean levelDownSkillForBuild(String buildId, SkillId skillId) {
        Build build = buildMap.get(buildId);

        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        int cost = dc.getSkillManager().getSkillLevelUpCost(skillId);
        SkillType skillType = dc.getSkillManager().getSkillType(skillId);
        return build.levelDownSkill(skillType, skillId, cost);
    }

    public void equipBuildForPlayer(UUID uuid, String buildId) {
        unequipBuildForPlayer(uuid);

        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        Map<SkillType, Integer> skillLevels = build.getSkillLevels();
        for (Map.Entry<SkillType, SkillId> entry : build.getSkills().entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            dc.getSkillManager().equipSkillForUser(uuid, entry.getValue(), skillLevels.get(entry.getKey()));
        }

        equipItems(uuid, build.getClassType());

        dc.getPlayerManager().setSelectedBuildIdForPlayer(uuid, buildId);

        Bukkit.getPluginManager().callEvent(new EquipBuildEvent(build, uuid));
    }

    private void equipItems(UUID uuid, ClassType classType) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(armorContentsMap.get(classType));
        player.getInventory().setItem(0, new ItemStack(Material.IRON_SWORD));

        if(ItemUtil.isSword(player.getInventory().getItemInMainHand().getType()))
            player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));

        player.getInventory().setItem(1, new ItemStack(Material.IRON_AXE));
        if (classType == ClassType.RANGER || classType == ClassType.ASSASSIN)
            player.getInventory().setItem(2, new ItemStack(Material.BOW));
    }

    public void unequipBuildForPlayer(UUID uuid) {
        String buildId = dc.getPlayerManager().getSelectedBuildIdFromPlayer(uuid);
        if (buildId == null) {
            return;
        }

        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        for (SkillId skillId : build.getSkills().values()) {
            dc.getSkillManager().unequipSkillForPlayer(uuid, skillId);
        }

        dc.getPlayerManager().setSelectedBuildIdForPlayer(uuid, null);
        Bukkit.getPluginManager().callEvent(new UnequipBuildEvent(build, uuid));
    }

    public SkillId getSkillFromBuild(String buildId, SkillType skillType) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build.getSkill(skillType);
    }

    public int getSkillLevelFromBuild(String buildId, SkillType skillType) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }
        return build.getSkillLevel(skillType);
    }

    public ClassType getClassTypeFromBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        return buildMap.get(buildId).getClassType();
    }

    public int getSkillPointsFromBuild(String buildId) {
        Build build = buildMap.get(buildId);
        if (build == null) {
            throw new IllegalArgumentException(String.format("Build with id \"%s\" not found.", buildId));
        }

        return build.getSkillPoints();
    }


    public void addBuild(Build build) {
        buildMap.put(build.getId(), build);
    }
}
