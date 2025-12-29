package com.bindothorpe.champions.commands.champions;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.game.map.GameMap;
import com.bindothorpe.champions.domain.game.map.GameMapManager;
import com.bindothorpe.champions.events.game.map.PlayerStartEditingMapEvent;
import com.bindothorpe.champions.util.ChatUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class MapCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createMapCommand(DomainController dc) {
        return Commands.literal("map")
                .then(Commands.literal("edit")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes((ctx) -> MapCommand.editMapWithArguments(ctx, dc))))
                .then(Commands.literal("create")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .then(Commands.argument("name", StringArgumentType.word())
                                    .executes((ctx) -> MapCommand.createMapWithArguments(ctx, dc)))));
    }

    private static int editMapWithArguments(CommandContext<CommandSourceStack> ctx, DomainController dc) {
        if(!(ctx.getSource().getSender() instanceof Player player)) return Command.SINGLE_SUCCESS;

        String id = StringArgumentType.getString(ctx, "id");

        if(GameMapManager.getInstance(dc).getEditingMapForPlayer(player) != null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text("You cannot start editing a map while you are already editing a map.", NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }

        try {
            GameMapManager.getInstance(dc).editMap(dc, player, id);
        } catch (Exception e) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(e.getMessage(), NamedTextColor.GRAY));
            throw new RuntimeException(e);
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
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Map with id '%s' already exists.", id), NamedTextColor.GRAY));
            return Command.SINGLE_SUCCESS;
        }


        if(GameMapManager.getInstance(dc).createMap(dc, id, name) == null) {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP, Component.text(String.format("Failed to create map with id '%s'", id), NamedTextColor.GRAY));
        } else {
            ChatUtil.sendMessage(player, ChatUtil.Prefix.MAP,
                    Component.text(String.format("Successfully created map with id '%s'. You can now start editing it by running ", id), NamedTextColor.GRAY)
                            .append(Component.text(String.format("/champions map edit %s", id), NamedTextColor.WHITE))
                            .append(Component.text(".", NamedTextColor.GRAY)));
        }
        return Command.SINGLE_SUCCESS;
    }
}
