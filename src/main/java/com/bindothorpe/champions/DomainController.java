package com.bindothorpe.champions;

import com.bindothorpe.champions.domain.block.TemporaryBlockManager;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.build.BuildManager;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.combat.CombatLogger;
import com.bindothorpe.champions.domain.entityStatus.EntityStatus;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusManager;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusType;
import com.bindothorpe.champions.domain.item.GameItem;
import com.bindothorpe.champions.domain.item.GameItemManager;
import com.bindothorpe.champions.domain.player.PlayerManager;
import com.bindothorpe.champions.domain.skill.Skill;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillManager;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.domain.statusEffect.StatusEffect;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectType;
import com.bindothorpe.champions.gui.GuiManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

import java.util.*;

public class DomainController {

    private final ChampionsPlugin plugin;
    private final SkillManager skillManager = SkillManager.getInstance(this);
    private final PlayerManager playerManager = PlayerManager.getInstance(this);
    private final BuildManager buildManager = BuildManager.getInstance(this);
    private final GuiManager guiManager = GuiManager.getInstance(this);
    private final TemporaryBlockManager temporaryBlockManager = TemporaryBlockManager.getInstance(this);
    private final EntityStatusManager entityStatusManager = EntityStatusManager.getInstance(this);
    private final StatusEffectManager statusEffectManager = StatusEffectManager.getInstance(this);
    private final GameItemManager gameItemManager = GameItemManager.getInstance(this);
    private final CombatLogger combatLogger = CombatLogger.getInstance(this);

    public DomainController(ChampionsPlugin plugin) {
        this.plugin = plugin;
    }

    public ChampionsPlugin getPlugin() {
        return plugin;
    }

    public void setSelectedBuildIdForPlayer(UUID uuid, String buildId) {
        playerManager.setSelectedBuildIdForPlayer(uuid, buildId);
    }

    public String getSelectedBuildIdFromPlayer(UUID uuid) {
        return playerManager.getSelectedBuildIdFromPlayer(uuid);
    }

    public Map<ClassType, Set<String>> getBuildIdsFromPlayer(UUID uuid) {
        return playerManager.getBuildIdsFromPlayer(uuid);
    }

    public boolean addBuildIdToPlayer(UUID uuid, ClassType classType, String buildId) {
        return playerManager.addBuildIdToPlayer(uuid, classType, buildId);
    }

    public boolean removeBuildIdFromPlayer(UUID uuid, String buildId) {
        return playerManager.removeBuildIdFromPlayer(uuid, buildId);
    }

    public void equipSkillForUser(UUID uuid, SkillId skillId, int level) {
        skillManager.equipSkillForUser(uuid, skillId, level);
    }

    public void unequipSkillForPlayer(UUID uuid, SkillId skillId) {
        skillManager.unequipSkillForPlayer(uuid, skillId);
    }

    public String getSkillName(SkillId skillId) {
        return skillManager.getSkillName(skillId);
    }

    public List<Component> getSkillDescription(SkillId skillId, int skillLevel) {
        return skillManager.getSkillDescription(skillId, skillLevel);
    }

    public List<Double> getSkillCooldownDuration(SkillId skillId) {
        return skillManager.getSkillCooldownDuration(skillId);
    }

    public int getSkillMaxLevel(SkillId skillId) {
        return skillManager.getSkillMaxLevel(skillId);
    }

    public int getSkillLevelUpCost(SkillId skillId) {
        return skillManager.getSkillLevelUpCost(skillId);
    }

    public void registerSkill(Skill skill) {
        skillManager.registerSkill(skill);
    }

    public Map<ClassType, Set<Build>> getBuilds(Collection<String> buildsIds) {
        return buildManager.getBuilds(buildsIds);
    }

    public Build getBuild(String buildId) {
        return buildManager.getBuild(buildId);
    }

    public String createEmptyBuild(ClassType classType) {
        return buildManager.createEmptyBuild(classType);
    }

    public boolean deleteBuild(String buildId) {
        return buildManager.deleteBuild(buildId);
    }

    public ClassType getClassTypeFromBuild(String buildId) {
        return buildManager.getClassTypeFromBuild(buildId);
    }

    public Set<SkillId> getClassSkillsForSkillType(ClassType classType, SkillType skillType) {
        return skillManager.getClassSkillsForSkillType(classType, skillType);
    }

    public boolean levelUpSkillForBuild(String buildId, SkillId skillId) {
        return buildManager.levelUpSkillForBuild(buildId, skillId);
    }

    public boolean levelDownSkillForBuild(String buildId, SkillId skillId) {
        return buildManager.levelDownSkillForBuild(buildId, skillId);
    }

    public void equipBuildForPlayer(UUID uuid, String buildId) {
        buildManager.equipBuildForPlayer(uuid, buildId);
    }

    public void unequipBuildForPlayer(UUID uuid) {
        buildManager.unequipBuildForPlayer(uuid);
    }

    public SkillId getSkillFromBuild(String buildId, SkillType skillType) {
        return buildManager.getSkillFromBuild(buildId, skillType);
    }

    public int getSkillLevelFromBuild(String buildId, SkillType skillType) {
        return buildManager.getSkillLevelFromBuild(buildId, skillType);
    }

    public SkillType getSkillType(SkillId skillId) {
        return skillManager.getSkillType(skillId);
    }

    public void openBuildsOverviewGui(UUID uuid, ClassType classType) {
        guiManager.openBuildsOverviewGui(uuid, classType);
    }

    public void openClassOverviewGui(UUID uuid) {
        guiManager.openClassOverviewGui(uuid);
    }

    public void openEditBuildGui(UUID uuid, String buildId, int buildNumber) {
        guiManager.openEditBuildGui(uuid, buildId, buildNumber);
    }

    public int getBuildCountByClassTypeForPlayer(ClassType classType, UUID uuid) {
        return playerManager.getBuildCountByClassTypeForPlayer(classType, uuid);
    }

    public int getMaxBuildsForPlayer(UUID uuid) {
        return playerManager.getMaxBuildsForPlayer(uuid);
    }

    public int getSkillPointsFromBuild(String buildId) {
        return buildManager.getSkillPointsFromBuild(buildId);
    }

    public void addStatusToEntity(UUID uuid, EntityStatus status) {
        entityStatusManager.addEntityStatus(uuid, status);
    }

    public void removeStatusFromEntity(UUID uuid, EntityStatusType type, Object source) {
        entityStatusManager.removeEntityStatus(uuid, type, source);
    }

    public double getFinalEntityStatusValue(UUID uuid, EntityStatusType type, double baseValue) {
        return entityStatusManager.getFinalValue(uuid, type, baseValue);
    }

    public void updateEntityStatus(UUID uuid, EntityStatusType type) {
        entityStatusManager.updateEntityStatus(uuid, type);
    }

    public void spawnTemporaryBlock(Location location, Material material, double duration) {
        temporaryBlockManager.spawnTemporaryBlock(location, material, duration);
    }

    public void spawnGameItem(GameItem gameItem, Location startingLocation, Vector direction, double strength) {
        gameItemManager.spawnGameItem(gameItem, startingLocation, direction, strength);
    }

    public boolean isGameItem(Item item) {
        return gameItemManager.isGameItem(item);
    }

    public GameItem getGameItem(Item item) {
        return gameItemManager.getGameItem(item);
    }

    public Set<GameItem> getGameItems() {
        return gameItemManager.getGameItems();
    }

    public void despawnItem(UUID uuid) {
        gameItemManager.despawnItem(uuid);
    }

    public void logDamageTaken(UUID uuid) {
        combatLogger.logDamageTaken(uuid);
    }

    public void logDamageDealt(UUID uuid) {
        combatLogger.logDamageDealt(uuid);
    }

    public boolean isInCombat(UUID uuid) {
        return combatLogger.isInCombat(uuid);
    }

    public boolean hasTakenDamageWithinDuration(UUID uuid, double duration) {
        return combatLogger.hasTakenDamageWithinDuration(uuid, duration);
    }

    public boolean hasDealtDamageWithinDuration(UUID uuid, double duration) {
        return combatLogger.hasDealtDamageWithinDuration(uuid, duration);
    }

    public void registerStatusEffect(StatusEffect statusEffect) {
        statusEffectManager.registerStatusEffect(statusEffect);
    }

    public void addStatusEffectToEntity(StatusEffectType type, UUID uuid, double duration) {
        statusEffectManager.addStatusEffectToEntity(type, uuid, duration);
    }

    public void removeStatusEffectFromPlayer(StatusEffectType type, UUID uuid) {
        statusEffectManager.removeStatusEffectFromPlayer(type, uuid);
    }

}
