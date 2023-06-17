package com.bindothorpe.champions.database;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.service.BuildService;
import com.bindothorpe.champions.domain.build.Build;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class DatabaseController {

    private final DomainController dc;
    private static DatabaseController instance;
    private Connection connection;

    private BuildService buildService;

    private DatabaseController(DomainController dc) {
        this.dc = dc;
    }

    public static DatabaseController getInstance(DomainController dc) {
        if (instance == null)
            instance = new DatabaseController(dc);
        return instance;
    }

    private BuildService getBuildService() throws SQLException {
        if (buildService == null)
            buildService = new BuildService(dc, getConnection());
        return buildService;
    }

    private Connection getConnection() throws SQLException {

        if (connection != null) {
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://localhost/Champions";
        String user = "root";
        String password = "";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        System.out.println("Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {

        Statement statement = getConnection().createStatement();

        //Create the player_stats table
        String sql = "CREATE TABLE IF NOT EXISTS build (id VARCHAR(255) PRIMARY KEY, player_id VARCHAR(255), class_type VARCHAR(255), sword_skill VARCHAR(255), sword_level INT, axe_skill VARCHAR(255), axe_level INT, bow_skill VARCHAR(255), bow_level INT, passive_a_skill VARCHAR(255), passive_a_level INT, passive_b_skill VARCHAR(255), passive_b_level INT, passive_c_skill VARCHAR(255), passive_c_level INT, class_passive VARCHAR(255), skill_points INT)";
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS player (id VARCHAR(255) PRIMARY KEY, build_id VARCHAR(255), FOREIGN KEY (build_id) REFERENCES build(id) ON DELETE SET NULL)";
        statement.execute(sql);

        statement.close();

        System.out.println("Database initialized.");
    }

    public void createPlayerSelectedBuild(UUID uuid, String buildId) {
        try {
            getBuildService().createPlayerSelectedBuild(uuid, buildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePlayerSelectedBuild(UUID uuid, String buildId) {
        try {
            getBuildService().updatePlayerSelectedBuild(uuid, buildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getPlayerSelectedBuildByUUID(UUID uuid, DatabaseResponse<String> response) {
        try {
            getBuildService().findPlayerSelectedBuildByUUID(uuid, response);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createBuild(Build build, UUID ownerId) {
        try {
            getBuildService().createBuild(build, ownerId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getBuildsByPlayerUUID(UUID uuid, DatabaseResponse<List<Build>> response) {
        try {
            getBuildService().getBuildsByPlayerUUID(uuid, response);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateBuild(Build build) {
        try {
            getBuildService().updateBuild(build);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteBuild(String id) {
        try {
            getBuildService().deleteBuild(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
