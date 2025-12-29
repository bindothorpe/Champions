package com.bindothorpe.champions.database.repository;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import com.bindothorpe.champions.domain.game.map.gameObjects.*;
import com.bindothorpe.champions.domain.team.TeamColor;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GameMapRepository {

    private final Connection connection;

    public GameMapRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Initialize database tables
     */
    public void initializeTables() throws SQLException {
        String createGameMapTable = """
            CREATE TABLE IF NOT EXISTS game_maps (
                id VARCHAR(255) PRIMARY KEY,
                name VARCHAR(255) NOT NULL
            )
        """;

        String createGameObjectTable = """
            CREATE TABLE IF NOT EXISTS game_objects (
                id INT AUTO_INCREMENT PRIMARY KEY,
                map_id VARCHAR(255) NOT NULL,
                type VARCHAR(50) NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                name VARCHAR(255),
                class_type VARCHAR(50),
                team_color VARCHAR(50),
                facing_direction VARCHAR(20),
                FOREIGN KEY (map_id) REFERENCES game_maps(id) ON DELETE CASCADE
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createGameMapTable);
            stmt.execute(createGameObjectTable);
        }
    }

    // ========== GameMap CRUD Operations ==========

    /**
     * Create a new GameMap in the database
     */
    public void createGameMap(GameMap gameMap) throws SQLException {
        String sql = "INSERT INTO game_maps (id, name) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, gameMap.getId());
            stmt.setString(2, gameMap.getName());
            stmt.executeUpdate();
        }

        // Save all game objects
        for (GameObject gameObject : gameMap.getGameObjects()) {
            createGameObject(gameMap.getId(), gameObject);
        }

        gameMap.setSaved(true);
    }

    /**
     * Read a GameMap by ID
     */
    public Optional<GameMap> getGameMap(String id) throws SQLException {
        String sql = "SELECT * FROM game_maps WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    GameMap gameMap = new GameMap(
                            rs.getString("id"),
                            rs.getString("name")
                    );

                    // Load game objects
                    Set<GameObject> gameObjects = getGameObjectsForMap(id);
                    gameMap.setGameObjects(gameObjects);
                    gameMap.setSaved(true);

                    return Optional.of(gameMap);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get all GameMaps
     */
    public Set<GameMap> getAllGameMaps() throws SQLException {
        Set<GameMap> gameMaps = new HashSet<>();
        String sql = "SELECT * FROM game_maps";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String id = rs.getString("id");
                GameMap gameMap = new GameMap(
                        id,
                        rs.getString("name")
                );

                // Load game objects
                Set<GameObject> gameObjects = getGameObjectsForMap(id);
                gameMap.setGameObjects(gameObjects);
                gameMap.setSaved(true);

                gameMaps.add(gameMap);
            }
        }

        return gameMaps;
    }

    /**
     * Update an existing GameMap
     */
    public void updateGameMap(GameMap gameMap) throws SQLException {
        String sql = "UPDATE game_maps SET name = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, gameMap.getName());
            stmt.setString(2, gameMap.getId());
            stmt.executeUpdate();
        }

        // Delete and recreate all game objects (simpler than selective updates)
        deleteGameObjectsForMap(gameMap.getId());
        for (GameObject gameObject : gameMap.getGameObjects()) {
            createGameObject(gameMap.getId(), gameObject);
        }

        gameMap.setSaved(true);
    }

    /**
     * Delete a GameMap by ID
     */
    public void deleteGameMap(String id) throws SQLException {
        String sql = "DELETE FROM game_maps WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
        // Game objects will be deleted automatically due to CASCADE
    }

    /**
     * Check if a GameMap exists
     */
    public boolean existsGameMap(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM game_maps WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // ========== GameObject CRUD Operations ==========

    /**
     * Create a new GameObject
     */
    public void createGameObject(String mapId, GameObject gameObject) throws SQLException {
        String sql = """
            INSERT INTO game_objects (map_id, type, x, y, z, name, class_type, team_color, facing_direction)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapId);
            stmt.setString(2, gameObject.getType().name());

            Vector loc = gameObject.worldLocation();
            stmt.setDouble(3, loc.getX());
            stmt.setDouble(4, loc.getY());
            stmt.setDouble(5, loc.getZ());

            // Set type-specific fields
            setGameObjectSpecificFields(stmt, gameObject);

            stmt.executeUpdate();
        }
    }

    /**
     * Get all GameObjects for a specific map
     */
    public Set<GameObject> getGameObjectsForMap(String mapId) throws SQLException {
        Set<GameObject> gameObjects = new HashSet<>();
        String sql = "SELECT * FROM game_objects WHERE map_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GameObject gameObject = deserializeGameObject(rs);
                    if (gameObject != null) {
                        gameObjects.add(gameObject);
                    }
                }
            }
        }

        return gameObjects;
    }

    /**
     * Get GameObjects of a specific type for a map
     */
    public Set<GameObject> getGameObjectsByType(String mapId, GameObjectType type) throws SQLException {
        Set<GameObject> gameObjects = new HashSet<>();
        String sql = "SELECT * FROM game_objects WHERE map_id = ? AND type = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapId);
            stmt.setString(2, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GameObject gameObject = deserializeGameObject(rs);
                    if (gameObject != null) {
                        gameObjects.add(gameObject);
                    }
                }
            }
        }

        return gameObjects;
    }

    /**
     * Delete all GameObjects for a specific map
     */
    public void deleteGameObjectsForMap(String mapId) throws SQLException {
        String sql = "DELETE FROM game_objects WHERE map_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapId);
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a specific GameObject by its location
     */
    public void deleteGameObject(String mapId, Vector location) throws SQLException {
        String sql = "DELETE FROM game_objects WHERE map_id = ? AND x = ? AND y = ? AND z = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapId);
            stmt.setDouble(2, location.getX());
            stmt.setDouble(3, location.getY());
            stmt.setDouble(4, location.getZ());
            stmt.executeUpdate();
        }
    }

    // ========== Helper Methods ==========

    private void setGameObjectSpecificFields(PreparedStatement stmt, GameObject gameObject) throws SQLException {
        switch (gameObject.getType()) {
            case CAPTURE_POINT -> {
                CapturePointGameObject cp = (CapturePointGameObject) gameObject;
                stmt.setString(6, cp.name());
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
            }
            case CHAMPION_SELECT -> {
                ChampionSelectGameObject cs = (ChampionSelectGameObject) gameObject;
                stmt.setNull(6, Types.VARCHAR);
                stmt.setString(7, cs.classType().name());
                stmt.setNull(8, Types.VARCHAR);
                stmt.setString(9, cs.facingDirection().name());
            }
            case SPAWN_POINT -> {
                SpawnPointGameObject sp = (SpawnPointGameObject) gameObject;
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setString(8, sp.team().name());
                stmt.setString(9, sp.facingDirection().name());
            }
            default -> {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.VARCHAR);
            }
        }
    }

    private GameObject deserializeGameObject(ResultSet rs) throws SQLException {
        GameObjectType type = GameObjectType.valueOf(rs.getString("type"));
        Vector location = new Vector(
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z")
        );

        return switch (type) {
            case CAPTURE_POINT -> new CapturePointGameObject(
                    rs.getString("name"),
                    location
            );
            case CHAMPION_SELECT -> new ChampionSelectGameObject(
                    ClassType.valueOf(rs.getString("class_type")),
                    BlockFace.valueOf(rs.getString("facing_direction")),
                    location
            );
            case SPAWN_POINT -> new SpawnPointGameObject(
                    TeamColor.valueOf(rs.getString("team_color")),
                    BlockFace.valueOf(rs.getString("facing_direction")),
                    location
            );
            case CHEST -> new ChestGameObject(location);
            case GEM -> new GemGameObject(location);
        };
    }
}