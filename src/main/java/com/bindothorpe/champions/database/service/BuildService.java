package com.bindothorpe.champions.database.service;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.DatabaseResponse;
import com.bindothorpe.champions.database.repository.BuildRepository;
import com.bindothorpe.champions.domain.build.Build;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.UUID;

public class BuildService {

    private DomainController dc;
    private final BuildRepository buildRepository;

    public BuildService(DomainController dc, Connection connection) {
        this.dc = dc;
        this.buildRepository = new BuildRepository(connection);
    }

    public void createPlayerSelectedBuild(UUID uuid, String buildId) {

        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                buildRepository.createPlayerSelectedBuild(uuid.toString(), buildId);

            } catch (SQLIntegrityConstraintViolationException e) {
                updatePlayerSelectedBuild(uuid, buildId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

    }

    public void updatePlayerSelectedBuild(UUID uuid, String buildId) {
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                buildRepository.updatePlayerSelectedBuild(uuid.toString(), buildId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void findPlayerSelectedBuildByUUID(UUID uuid, DatabaseResponse<String> response)  {
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                String buildId = buildRepository.findSelectedBuildIdByUUID(uuid.toString());
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> response.onResult(buildId));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createBuild(Build build, UUID ownerId) {
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                buildRepository.createBuild(build, ownerId.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void getBuildsByPlayerUUID(UUID uuid, DatabaseResponse<List<Build>> response) {
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                List<Build> builds = buildRepository.findBuildsByUUID(uuid.toString());
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> response.onResult(builds));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void updateBuild(Build build) {
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                buildRepository.updateBuild(build);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void deleteBuild(String id) {
        try {
            buildRepository.deleteBuild(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
