package com.bindothorpe.champions.gui.items.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.Build;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.events.build.DeleteBuildEvent;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DeleteBuildItem extends GuiItem {

    private final DomainController dc;
    private final String buildId;
    private final ClassType classType;

    public DeleteBuildItem(String buildId, DomainController dc) {
        super(new ItemStack(Material.TNT));
        this.dc = dc;
        this.buildId = buildId;
        this.classType = dc.getBuildManager().getClassTypeFromBuild(buildId);

        initialize();
        setAction(this::handleClick);
    }

    private void handleClick(InventoryClickEvent event) {
        if(!(event.isShiftClick() && event.isRightClick())) {
            return;
        }

        if(dc.getPlayerManager().removeBuildIdFromPlayer(event.getWhoClicked().getUniqueId(), buildId)) {

            Build build = dc.getBuildManager().deleteBuild(buildId);
            if(build != null)
                Bukkit.getPluginManager().callEvent(new DeleteBuildEvent(build, event.getWhoClicked().getUniqueId()));

            dc.getGuiManager().openBuildsOverviewGui(event.getWhoClicked().getUniqueId(), classType);
        }

    }

    private void initialize() {
        ItemMeta meta = getItem().getItemMeta();
        meta.displayName(Component.text("Delete Build").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("Sneak + Right-click ").color(NamedTextColor.YELLOW)
                .append(Component.text("to delete this build").color(NamedTextColor.GRAY)));
        lore.add(Component.text(" "));
        lore.add(Component.text("WARNING: THIS ACTION IS IRREVERSIBLE").color(NamedTextColor.RED));

        meta.lore(lore);
        getItem().setItemMeta(meta);
    }
}
