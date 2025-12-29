package com.bindothorpe.champions.commands.champions;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameMapManager;
import com.bindothorpe.champions.events.game.map.PlayerStartEditingMapEvent;
import com.bindothorpe.champions.events.game.map.PlayerStopEditingMapEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.bindothorpe.champions.domain.game.map.GameMapListener.MapEditToolItemStack;

public class MapCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createMapCommand(DomainController dc) {
        return Commands.literal("map")
                .then(Commands.literal("edit")
                        .requires((ctx) -> !isEditing(ctx, dc))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((ctx, builder) -> getAmountSuggestions(ctx, builder, dc))
                                .executes((ctx) -> MapCommand.editMapWithArguments(ctx, dc))))
                .then(Commands.literal("tool")
                        .requires((ctx) -> isEditing(ctx, dc))
                        .executes((ctx) -> MapCommand.giveMapTool(ctx, dc)))
                .then(Commands.literal("create")
                        .requires((ctx) -> !isEditing(ctx, dc))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes((ctx) -> MapCommand.createMapWithArguments(ctx, dc)))))
                .then(Commands.literal("save")
                        .requires((ctx) -> isEditing(ctx, dc))
                        .executes((ctx) -> MapCommand.saveMap(ctx, dc)))
                .then(Commands.literal("done")
                        .requires((ctx) -> isEditing(ctx, dc))
                        .executes((ctx) -> MapCommand.doneEditingMap(ctx, dc)))
                .then(Commands.literal("cancel")
                        .requires((ctx) -> isEditing(ctx, dc))
                        .executes((ctx) -> MapCommand.cancelEditingMap(ctx, dc, false))
                        .then(Commands.literal("--confirm")
                                .requires((ctx) -> isEditing(ctx, dc))
                                .executes((ctx) -> MapCommand.cancelEditingMap(ctx, dc, true))));
    }

    private static boolean isEditing(CommandSourceStack ctx, DomainController dc) {
        if(!(ctx.getSender() instanceof Player player)) return false;
        return GameMapManager.getInstance(dc).getEditingMapForPlayer(player) != null;
    }

    private static int giveMapTool(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        GameMap editingMap = GameMapManager.getInstance(dc).getEditingMapForPlayer(player);

        if(editingMap == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are not currently editing a map.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        player.getInventory().setItem(8, MapEditToolItemStack());
        return Command.SINGLE_SUCCESS;
    }

    private static int editMapWithArguments(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String id = StringArgumentType.getString(ctx, "id");

        if(GameMapManager.getInstance(dc).getEditingMapForPlayer(player) != null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You cannot start editing a map while you are already editing a map.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        try {
            GameMapManager.getInstance(dc).editMap(dc, player, id);
        } catch (Exception e) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(e.getMessage(), NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("You started editing %s.", id), NamedTextColor.GRAY));
        PlayerStartEditingMapEvent event = new PlayerStartEditingMapEvent(player, GameMapManager.getInstance(dc).getEditingMapForPlayer(player));
        event.callEvent();

        return Command.SINGLE_SUCCESS;
    }

    private static int createMapWithArguments(CommandContext<CommandSourceStack> ctx, DomainController dc) {

        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String id = StringArgumentType.getString(ctx, "id");
        String name = StringArgumentType.getString(ctx, "name");

        if(GameMapManager.getInstance(dc).getGameMapIds().contains(id)) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Map with id '%s' already exists.", id), NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }


        if(GameMapManager.getInstance(dc).createMap(dc, id, name) == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Failed to create map with id '%s'", id), NamedTextColor.RED));
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                    Component.text(String.format("Successfully created map with id '%s'. You can now start editing it by running ", id), NamedTextColor.GRAY)
                            .append(Component.text(String.format("/champions map edit %s", id), NamedTextColor.WHITE))
                            .append(Component.text(".", NamedTextColor.GRAY)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int saveMap(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        GameMap editingMap = GameMapManager.getInstance(dc).getEditingMapForPlayer(player);

        if(editingMap == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are not currently editing a map.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if(editingMap.getSlimeWorld() == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Error: Map world is not loaded.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Saving map... This may take a moment.", NamedTextColor.GRAY));

        // Run save operations asynchronously (I/O operations)
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                // Save SlimeWorld (blocks until complete)
                AdvancedSlimePaperAPI.instance().saveWorld(editingMap.getSlimeWorld());

                // Save GameMap metadata and GameObjects to database
                try {
                    dc.getDatabaseController().getGameMapService().saveGameMapSync(editingMap);
                } catch (Exception e) {
                    throw new IOException("Failed to save map data to database", e);
                }

                // Return to main thread for player feedback
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> {
                    ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                            Component.text(String.format("Map with id %s saved successfully!", editingMap.getId()), NamedTextColor.GRAY));
                });

            } catch (IOException e) {
                // Return to main thread for error feedback
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> {
                    ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                            Component.text("Failed to save map: " + e.getMessage(), NamedTextColor.RED));
                    dc.getPlugin().getLogger().severe("Failed to save map " + editingMap.getId() + ": " + e.getMessage());
                    dc.getPlugin().getLogger().warning(e.getMessage());
                });
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int doneEditingMap(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        GameMap editingMap = GameMapManager.getInstance(dc).getEditingMapForPlayer(player);

        if(editingMap == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are not currently editing a map.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if(editingMap.getSlimeWorld() == null || editingMap.getSlimeWorldInstance() == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Error: Map world is not loaded.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Saving and closing map... This may take a moment.", NamedTextColor.GRAY));

        // Teleport player out first (must be on main thread)
        if(Bukkit.getWorlds().get(0) != null) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        // Run save operations asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(dc.getPlugin(), () -> {
            try {
                // Save SlimeWorld (blocks until complete)
                AdvancedSlimePaperAPI.instance().saveWorld(editingMap.getSlimeWorld());

                // Save GameMap metadata and GameObjects to database
                try {
                    dc.getDatabaseController().getGameMapService().saveGameMapSync(editingMap);
                } catch (Exception e) {
                    throw new IOException("Failed to save map data to database", e);
                }

                // Return to main thread to unload world and clean up
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> {
                    try {
                        // Unload the world using Bukkit API (must be on main thread)
                        World world = editingMap.getSlimeWorldInstance().getBukkitWorld();
                        boolean unloaded = Bukkit.unloadWorld(world, false); // false = don't save again (we already saved)

                        if (!unloaded) {
                            throw new Exception("Failed to unload world - Bukkit.unloadWorld returned false");
                        }

                        // Remove player from editing map
                        GameMapManager.getInstance(dc).stopEditingMap(player);

                        // Fire event
                        PlayerStopEditingMapEvent event = new PlayerStopEditingMapEvent(player, editingMap, true);
                        event.callEvent();

                        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                                Component.text(String.format("Map with id %s saved successfully!", editingMap.getId()), NamedTextColor.GRAY));

                    } catch (Exception e) {
                        ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                                Component.text("Map saved but failed to unload: " + e.getMessage(), NamedTextColor.YELLOW));
                        dc.getPlugin().getLogger().warning("Failed to unload map " + editingMap.getId() + ": " + e.getMessage());
                    }
                });

            } catch (IOException e) {
                // Return to main thread for error feedback
                Bukkit.getScheduler().runTask(dc.getPlugin(), () -> {
                    ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                            Component.text("Failed to save map: " + e.getMessage(), NamedTextColor.RED));
                    dc.getPlugin().getLogger().severe("Failed to save map " + editingMap.getId() + ": " + e.getMessage());
                    dc.getPlugin().getLogger().warning(e.getMessage());
                });
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static int cancelEditingMap(CommandContext<CommandSourceStack> ctx, DomainController dc, boolean confirmed) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        GameMap editingMap = GameMapManager.getInstance(dc).getEditingMapForPlayer(player);

        if(editingMap == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You are not currently editing a map.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        // Require confirmation if map has unsaved changes
        if(!confirmed && !editingMap.isSaved()) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                    Component.text("You have unsaved changes!", NamedTextColor.RED)
                            .append(Component.text("\n"))
                            .append(Component.text("All changes since your last save will be lost.", NamedTextColor.GRAY))
                            .append(Component.text("\n"))
                            .append(Component.text("To confirm, use: ", NamedTextColor.GRAY))
                            .append(Component.text("/champions map cancel --confirm", NamedTextColor.WHITE)));
            return Command.SINGLE_SUCCESS;
        }

        if(editingMap.getSlimeWorldInstance() == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("Error: Map world is not loaded.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        // Teleport player out first (must be on main thread)
        if(Bukkit.getWorlds().get(0) != null) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        try {
            // Unload world WITHOUT saving (discards all changes)
            // Use Bukkit API with save=false to discard changes
            World world = editingMap.getSlimeWorldInstance().getBukkitWorld();
            boolean unloaded = Bukkit.unloadWorld(world, false); // false = don't save, discard changes

            if (!unloaded) {
                throw new Exception("Failed to unload world - Bukkit.unloadWorld returned false");
            }

            // Remove player from editing map
            GameMapManager.getInstance(dc).stopEditingMap(player);

            // Fire event
            PlayerStopEditingMapEvent event = new PlayerStopEditingMapEvent(player, editingMap, false);
            event.callEvent();

            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                    Component.text("Editing cancelled. All unsaved changes have been discarded.", NamedTextColor.GRAY));

        } catch (Exception e) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                    Component.text("Failed to unload map: " + e.getMessage(), NamedTextColor.RED));
            dc.getPlugin().getLogger().severe("Failed to unload map " + editingMap.getId() + ": " + e.getMessage());
            dc.getPlugin().getLogger().warning(e.getMessage());
        }

        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> getAmountSuggestions(final CommandContext<CommandSourceStack> ctx, final SuggestionsBuilder builder, DomainController dc) {
        GameMapManager.getInstance(dc).getGameMapIds().forEach(builder::suggest);
        return builder.buildFuture();
    }
}