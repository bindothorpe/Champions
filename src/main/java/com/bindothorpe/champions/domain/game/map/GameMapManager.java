package com.bindothorpe.champions.domain.game.map;

import com.bindothorpe.champions.ChampionsPlugin;
import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.util.PersistenceUtil;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.*;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class GameMapManager {

    private static GameMapManager instance;

    private final Map<String, GameMap> gameMaps;
    private final Map<UUID, GameMap> editingPlayersMap;

    private GameMapManager() {
        gameMaps = new HashMap<>();
        editingPlayersMap = new HashMap<>();
    }

    public static GameMapManager getInstance() {
        if(instance == null) {
            instance = new GameMapManager();
        }

        return instance;
    }

    public @Nullable GameMap getEditingMapForPlayer(@NotNull UUID uuid) {
        return editingPlayersMap.get(uuid);
    }

    public @Nullable GameMap getEditingMapForPlayer(@NotNull Player player) {
        return getEditingMapForPlayer(player.getUniqueId());
    }

    public Set<String> getGameMapIds() {
        return gameMaps.keySet();
    }

    public @Nullable GameMap createMap(@NotNull DomainController dc, @NotNull String id, @NotNull String name, @NotNull World world, boolean teleportOnLoaded) {
        if(getGameMapIds().contains(id)) return null;

        AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
        SlimeWorld slimeWorld;
        try {
            slimeWorld = asp.readVanillaWorld(world.getWorldFolder(), world.getName(), dc.getDatabaseController().getMysqlLoader());
            asp.saveWorld(slimeWorld);
        } catch (InvalidWorldException e) {
            throw new RuntimeException("World does not exist.");
        } catch (WorldLoadedException e) {
            throw new RuntimeException("World is still loaded.");
        } catch (WorldTooBigException e) {
            throw new RuntimeException("World is too big.");
        } catch (IOException | WorldAlreadyExistsException e) {
            throw new RuntimeException(e);
        }

        PersistenceUtil.setData(world, getKey(dc.getPlugin()), PersistentDataType.STRING, id);
        GameMap map = new GameMap(id, name);

        gameMaps.put(id, map);

        if(teleportOnLoaded) {
            try {
                slimeWorld = asp.readWorld(dc.getDatabaseController().getMysqlLoader(), world.getName(), false, new SlimePropertyMap());
                SlimeWorldInstance slimeWorldInstance = asp.loadWorld(slimeWorld, true);
            } catch (UnknownWorldException | IOException | CorruptedWorldException | NewerFormatException e) {
                throw new RuntimeException(e);
            }
        }

        return map;
    }

    public void saveMap(@NotNull String id) {
        //TODO: Implement saving it.
    }

    public void editMap(@NotNull DomainController dc, @NotNull Player player, @NotNull String id) throws Exception {
        World world = player.getWorld();

        if(!Objects.equals(getGameMapIdFromWorld(dc, world), id)) {
            throw new Exception("The world does not match the game map id.");
        }

        editingPlayersMap.put(player.getUniqueId(), gameMaps.get(id));
    }

//    private void setEditModeForPlayer(@NotNull Player player, @NotNull String id, boolean editMode) {
//        if(editingPlayersMap.containsKey(player.getUniqueId()) && editMode) return;
//
//        if(!editingPlayersMap.containsKey(player.getUniqueId()) && !editMode) return;
//
//        if(!gameMaps.containsKey(id)) return;
//
//        if(editMode) {
//            editingPlayersMap.put(player.getUniqueId(), gameMaps.get(id));
//        } else {
//            editingPlayersMap.remove(player.getUniqueId());
//        }
//    }

    public boolean isGameMapWorld(@NotNull DomainController dc, @NotNull World world) {
        return PersistenceUtil.hasData(world, getKey(dc.getPlugin()), PersistentDataType.STRING);
    }

    public @Nullable String getGameMapIdFromWorld(@NotNull DomainController dc, @NotNull World world) {
        if(!isGameMapWorld(dc, world)) return null;
        return PersistenceUtil.getData(world, getKey(dc.getPlugin()), PersistentDataType.STRING);
    }

    private NamespacedKey getKey(ChampionsPlugin plugin) {
        return new NamespacedKey(plugin, "game-map-id");
    }
}
