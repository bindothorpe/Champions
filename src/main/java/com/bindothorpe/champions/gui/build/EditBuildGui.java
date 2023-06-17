package com.bindothorpe.champions.gui.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillId;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.events.build.UpdateBuildEvent;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.build.DeleteBuildItem;
import com.bindothorpe.champions.gui.items.global.BackItem;
import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.bindothorpe.champions.gui.items.skill.SkillItem;
import com.bindothorpe.champions.gui.items.skill.SkillPointsItem;
import com.bindothorpe.champions.gui.items.skill.SkillTypeItem;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

public class EditBuildGui extends PlayerGui {

    private String buildId;
    private ClassType classType;
    private int buildNumber;

    public EditBuildGui(UUID uuid, String buildId, int buildNumber, DomainController dc) {
        super(uuid, dc);
        this.buildId = buildId;
        this.classType = dc.getClassTypeFromBuild(buildId);
        this.buildNumber = buildNumber;
        initialize();
        gui.setOnClose(this::handleClose);
    }

    private void handleClose(InventoryCloseEvent event) {
        if(buildId.equals(dc.getSelectedBuildIdFromPlayer(uuid))) {
            dc.equipBuildForPlayer(uuid, buildId);
        }
    }

    @Override
    protected void initialize() {
        gui = new ChestGui(6, String.format("Edit %s %d", TextUtil.camelCasing(classType.toString()), buildNumber));

        StaticPane root = new StaticPane(0, 0, 9, 6);
        for(int i = 0; i < 54; i++) {
            int x = i % 9;
            int y = i / 9;
            if(x == 0 || x == 8)
                root.addItem(new BorderItem(), x, y);
        }
        root.addItem(new BackItem(event -> dc.openBuildsOverviewGui(event.getWhoClicked().getUniqueId(), classType)), 0, 0);
        root.addItem(new SkillPointsItem(buildId, dc), 0, 1);
        root.addItem(new DeleteBuildItem(buildId, dc), 0, 5);

        for(SkillType skillType : SkillType.values()) {
            if(skillType.equals(SkillType.CLASS_PASSIVE)) {
                continue;
            }

            root.addItem(new SkillTypeItem(skillType), 1, skillType.ordinal());
            OutlinePane skillTypePane = new OutlinePane(2, skillType.ordinal(), 6, 1);

            for(SkillId skillId : dc.getClassSkillsForSkillType(classType, skillType).stream().sorted(Comparator.comparing(Enum::toString)).collect(Collectors.toList())) {
                skillTypePane.addItem(new SkillItem(buildId, buildNumber, skillId, dc));
            }
            gui.addPane(skillTypePane);
        }


        gui.addPane(root);

    }
}
