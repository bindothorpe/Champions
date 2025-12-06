package com.bindothorpe.champions;

import com.bindothorpe.champions.config.CustomConfigManager;
import com.bindothorpe.champions.database.DatabaseController;
import com.bindothorpe.champions.domain.block.TemporaryBlockManager;
import com.bindothorpe.champions.domain.build.BuildManager;
import com.bindothorpe.champions.domain.combat.CombatLogger;
import com.bindothorpe.champions.domain.cooldown.CooldownManager;
import com.bindothorpe.champions.domain.customItem.CustomItemManager;
import com.bindothorpe.champions.domain.entityStatus.EntityStatusManager;
import com.bindothorpe.champions.domain.game.GameManager;
import com.bindothorpe.champions.domain.game.map.GameMapManager;
import com.bindothorpe.champions.domain.item.GameItemManager;
import com.bindothorpe.champions.domain.player.PlayerManager;
import com.bindothorpe.champions.domain.scoreboard.ScoreboardManager;
import com.bindothorpe.champions.domain.skill.SkillManager;
import com.bindothorpe.champions.domain.sound.SoundManager;
import com.bindothorpe.champions.domain.statusEffect.StatusEffectManager;
import com.bindothorpe.champions.domain.team.TeamManager;
import com.bindothorpe.champions.gui.GuiManager;

import java.sql.SQLException;
import java.util.logging.Level;

public class DomainController {

    private final ChampionsPlugin plugin;
    private final DatabaseController databaseController;
    private final SkillManager skillManager;
    private final PlayerManager playerManager;
    private final BuildManager buildManager;
    private final GuiManager guiManager;
    private final TemporaryBlockManager temporaryBlockManager;
    private final EntityStatusManager entityStatusManager;
    private final StatusEffectManager statusEffectManager;
    private final GameItemManager gameItemManager;
    private final CombatLogger combatLogger;
    private final GameManager gameManager;
    private final TeamManager teamManager;
    private final CustomItemManager customItemManager;
    private final ScoreboardManager scoreboardManager;
    private final CooldownManager cooldownManager;
    private final GameMapManager gameMapManager;
    private final CustomConfigManager customConfigManager;
    private final SoundManager soundManager;

    public DomainController(ChampionsPlugin plugin) {
        this.plugin = plugin;
        databaseController = DatabaseController.getInstance(this);
        skillManager = SkillManager.getInstance(this);
        playerManager = PlayerManager.getInstance(this);
        buildManager = BuildManager.getInstance(this);
        guiManager = GuiManager.getInstance(this);
        temporaryBlockManager = TemporaryBlockManager.getInstance(this);
        entityStatusManager = EntityStatusManager.getInstance(this);
        statusEffectManager = StatusEffectManager.getInstance(this);
        gameItemManager = GameItemManager.getInstance(this);
        combatLogger = CombatLogger.getInstance(this);
        gameManager = GameManager.getInstance(this);
        teamManager = TeamManager.getInstance(this);
        customItemManager = CustomItemManager.getInstance(this);
        scoreboardManager = ScoreboardManager.getInstance(this);
        cooldownManager = CooldownManager.getInstance(this);
        gameMapManager = GameMapManager.getInstance(this);
        customConfigManager = CustomConfigManager.getInstance(this);
        soundManager = SoundManager.getInstance(this);
        try {
            databaseController.initializeDatabase();
        } catch (SQLException e) {
            getPlugin().getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    public ChampionsPlugin getPlugin() {
        return plugin;
    }

    public DatabaseController getDatabaseController() {
        return databaseController;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public BuildManager getBuildManager() {
        return buildManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public TemporaryBlockManager getTemporaryBlockManager() {
        return temporaryBlockManager;
    }

    public EntityStatusManager getEntityStatusManager() {
        return entityStatusManager;
    }

    public StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    public GameItemManager getGameItemManager() {
        return gameItemManager;
    }

    public CombatLogger getCombatLogger() {
        return combatLogger;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public GameMapManager getGameMapManager() {
        return gameMapManager;
    }

    public CustomConfigManager getCustomConfigManager() {
        return customConfigManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }
}
