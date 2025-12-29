package com.bindothorpe.champions.database.service;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.database.repository.GameMapRepository;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameObject;
import com.bindothorpe.champions.domain.game.map.GameObjectType;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameMapService {

    private final DomainController dc;
    private final GameMapRepository repository;
    private final Logger LOGGER;

    public GameMapService(DomainController dc, Connection connection) {
        this.dc = dc;
        this.repository = new GameMapRepository(connection);
        LOGGER = dc.getPlugin().getLogger();
    }



    /**
     * Initialize database tables (async version)
     */
    public CompletableFuture<Boolean> initializeTables() {
        return runAsync(() -> {
            try {
                repository.initializeTables();
                LOGGER.info("GameMap tables initialized successfully");
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize GameMap tables", e);
                return false;
            }
        });
    }

    /**
     * Initialize database tables synchronously (for plugin startup)
     */
    public void initializeTablesSync() throws SQLException {
        repository.initializeTables();
    }

    // ========== GameMap Service Methods ==========

    /**
     * Save a GameMap to the database (create or update)
     * Runs async and callbacks on main thread
     */
    public CompletableFuture<Boolean> saveGameMap(GameMap gameMap) {
        return runAsync(() -> {
            try {
                if (repository.existsGameMap(gameMap.getId())) {
                    repository.updateGameMap(gameMap);
                    LOGGER.info("Updated GameMap: " + gameMap.getId());
                } else {
                    repository.createGameMap(gameMap);
                    LOGGER.info("Created GameMap: " + gameMap.getId());
                }
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to save GameMap: " + gameMap.getId(), e);
                return false;
            }
        });
    }

    /**
     * Save a GameMap and execute callback on main thread
     */
    public void saveGameMap(GameMap gameMap, Consumer<Boolean> callback) {
        saveGameMap(gameMap).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Load a GameMap from the database
     */
    public CompletableFuture<Optional<GameMap>> loadGameMap(String id) {
        return runAsync(() -> {
            try {
                Optional<GameMap> gameMap = repository.getGameMap(id);
                if (gameMap.isPresent()) {
                    LOGGER.info("Loaded GameMap: " + id);
                } else {
                    LOGGER.warning("GameMap not found: " + id);
                }
                return gameMap;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load GameMap: " + id, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Load a GameMap and execute callback on main thread
     */
    public void loadGameMap(String id, Consumer<Optional<GameMap>> callback) {
        loadGameMap(id).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Load all GameMaps from the database
     */
    public CompletableFuture<Set<GameMap>> loadAllGameMaps() {
        return runAsync(() -> {
            try {
                Set<GameMap> gameMaps = repository.getAllGameMaps();
                LOGGER.info("Loaded " + gameMaps.size() + " GameMaps");
                return gameMaps;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to load all GameMaps", e);
                return Set.of();
            }
        });
    }

    /**
     * Load all GameMaps and execute callback on main thread
     */
    public void loadAllGameMaps(Consumer<Set<GameMap>> callback) {
        loadAllGameMaps().thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Delete a GameMap from the database
     */
    public CompletableFuture<Boolean> deleteGameMap(String id) {
        return runAsync(() -> {
            try {
                repository.deleteGameMap(id);
                LOGGER.info("Deleted GameMap: " + id);
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to delete GameMap: " + id, e);
                return false;
            }
        });
    }

    /**
     * Delete a GameMap and execute callback on main thread
     */
    public void deleteGameMap(String id, Consumer<Boolean> callback) {
        deleteGameMap(id).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Check if a GameMap exists in the database
     */
    public CompletableFuture<Boolean> gameMapExists(String id) {
        return runAsync(() -> {
            try {
                return repository.existsGameMap(id);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to check if GameMap exists: " + id, e);
                return false;
            }
        });
    }

    // ========== GameObject Service Methods ==========

    /**
     * Add a GameObject to a GameMap and save it
     */
    public CompletableFuture<Boolean> addGameObject(String mapId, GameObject gameObject) {
        return runAsync(() -> {
            try {
                repository.createGameObject(mapId, gameObject);
                LOGGER.info("Added GameObject to map " + mapId + ": " + gameObject.getType());
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to add GameObject to map: " + mapId, e);
                return false;
            }
        });
    }

    /**
     * Add a GameObject with callback on main thread
     */
    public void addGameObject(String mapId, GameObject gameObject, Consumer<Boolean> callback) {
        addGameObject(mapId, gameObject).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Remove a GameObject from a GameMap
     */
    public CompletableFuture<Boolean> removeGameObject(String mapId, Vector location) {
        return runAsync(() -> {
            try {
                repository.deleteGameObject(mapId, location);
                LOGGER.info("Removed GameObject from map " + mapId + " at location: " + location);
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to remove GameObject from map: " + mapId, e);
                return false;
            }
        });
    }

    /**
     * Remove a GameObject with callback on main thread
     */
    public void removeGameObject(String mapId, Vector location, Consumer<Boolean> callback) {
        removeGameObject(mapId, location).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Get all GameObjects for a specific map
     */
    public CompletableFuture<Set<GameObject>> getGameObjectsForMap(String mapId) {
        return runAsync(() -> {
            try {
                return repository.getGameObjectsForMap(mapId);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to get GameObjects for map: " + mapId, e);
                return Set.of();
            }
        });
    }

    /**
     * Get all GameObjects with callback on main thread
     */
    public void getGameObjectsForMap(String mapId, Consumer<Set<GameObject>> callback) {
        getGameObjectsForMap(mapId).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Get GameObjects of a specific type for a map
     */
    public CompletableFuture<Set<GameObject>> getGameObjectsByType(String mapId, GameObjectType type) {
        return runAsync(() -> {
            try {
                return repository.getGameObjectsByType(mapId, type);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to get GameObjects by type for map: " + mapId, e);
                return Set.of();
            }
        });
    }

    /**
     * Get GameObjects by type with callback on main thread
     */
    public void getGameObjectsByType(String mapId, GameObjectType type, Consumer<Set<GameObject>> callback) {
        getGameObjectsByType(mapId, type).thenAcceptAsync(callback, this::runSync);
    }

    /**
     * Refresh a GameMap's objects from the database
     */
    public CompletableFuture<Boolean> refreshGameMapObjects(GameMap gameMap) {
        return runAsync(() -> {
            try {
                Set<GameObject> gameObjects = repository.getGameObjectsForMap(gameMap.getId());
                gameMap.setGameObjects(gameObjects);
                gameMap.setSaved(true);
                LOGGER.info("Refreshed GameMap objects: " + gameMap.getId());
                return true;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to refresh GameMap objects: " + gameMap.getId(), e);
                return false;
            }
        });
    }

    /**
     * Save only if the GameMap has unsaved changes
     */
    public CompletableFuture<Boolean> saveIfDirty(GameMap gameMap) {
        if (!gameMap.isSaved()) {
            return saveGameMap(gameMap);
        }
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Bulk save multiple GameMaps
     */
    public CompletableFuture<Integer> saveAllGameMaps(Set<GameMap> gameMaps) {
        return runAsync(() -> {
            int successCount = 0;
            for (GameMap gameMap : gameMaps) {
                try {
                    if (repository.existsGameMap(gameMap.getId())) {
                        repository.updateGameMap(gameMap);
                    } else {
                        repository.createGameMap(gameMap);
                    }
                    successCount++;
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Failed to save GameMap during bulk save: " + gameMap.getId(), e);
                }
            }
            LOGGER.info("Bulk saved " + successCount + " out of " + gameMaps.size() + " GameMaps");
            return successCount;
        });
    }

    /**
     * Clone a GameMap with a new ID
     */
    public CompletableFuture<Optional<GameMap>> cloneGameMap(String sourceId, String newId, String newName) {
        return runAsync(() -> {
            try {
                Optional<GameMap> source = repository.getGameMap(sourceId);
                if (source.isEmpty()) {
                    LOGGER.warning("Cannot clone: source GameMap not found: " + sourceId);
                    return Optional.empty();
                }

                GameMap original = source.get();
                GameMap clone = new GameMap(newId, newName);
                clone.setGameObjects(new java.util.HashSet<>(original.getGameObjects()));

                repository.createGameMap(clone);
                LOGGER.info("Cloned GameMap from " + sourceId + " to " + newId);
                return Optional.of(clone);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to clone GameMap: " + sourceId, e);
                return Optional.empty();
            }
        });
    }

    /**
     * Get count of GameObjects in a map
     */
    public CompletableFuture<Integer> getGameObjectCount(String mapId) {
        return getGameObjectsForMap(mapId).thenApply(Set::size);
    }

    /**
     * Check if a map has any objects of a specific type
     */
    public CompletableFuture<Boolean> hasGameObjectType(String mapId, GameObjectType type) {
        return getGameObjectsByType(mapId, type).thenApply(objects -> !objects.isEmpty());
    }

    // ========== Bukkit Threading Helper Methods ==========

    /**
     * Run task asynchronously (off main thread)
     */
    private <T> CompletableFuture<T> runAsync(java.util.function.Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Run task on main server thread
     */
    private void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(dc.getPlugin(), runnable);
        }
    }

    /**
     * Synchronous operations for when you need immediate results on main thread
     * WARNING: These will block the main thread! Use sparingly.
     */
    public Optional<GameMap> loadGameMapSync(String id) {
        try {
            return repository.getGameMap(id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load GameMap synchronously: " + id, e);
            return Optional.empty();
        }
    }

    public boolean saveGameMapSync(GameMap gameMap) {
        try {
            if (repository.existsGameMap(gameMap.getId())) {
                repository.updateGameMap(gameMap);
            } else {
                repository.createGameMap(gameMap);
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save GameMap synchronously: " + gameMap.getId(), e);
            return false;
        }
    }
}