package com.bindothorpe.champions.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ShieldPacketListener extends PacketAdapter {

    public ShieldPacketListener(JavaPlugin plugin) {
        super(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {

        try {
            handlePacket(event);
        } catch (IllegalArgumentException e) {
            Bukkit.getConsoleSender().sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            Bukkit.getConsoleSender().sendMessage(Component.text("Packet: " + event.getPacket().getType()).color(NamedTextColor.RED));
        }
    }

    private void handlePacket(PacketEvent event) throws IllegalArgumentException{
//        // Check if the packet has item slots
//        if (event.getPacket().getItemSlots().size() <= 0)
//            throw new IllegalArgumentException("Packet does not have item slots");
//
//        // Check if the packet is updating the off-hand of a player
//        if (event.getPacket().getItemSlots().read(0) != EnumWrappers.ItemSlot.OFFHAND)
//            throw new IllegalArgumentException("Packet is not updating the off-hand of a player");

        // Check if the packet has items
        if (event.getPacket().getItemModifier().size() <= 0)
            throw new IllegalArgumentException("Packet does not have items. ItemSlots: " +
                    event.getPacket().getItemSlots().size() + ", " + event.getPacket().getItemSlots().toString());
        Bukkit.getConsoleSender().sendMessage(Component.text("PASSED").color(NamedTextColor.GREEN));

        // Check if the item is a shield
        if (event.getPacket().getItemModifier().read(0).getType() == Material.SHIELD) {
            // Change the item to air
            event.getPacket().getItemModifier().write(0, new ItemStack(Material.AIR));
        }
    }
}
