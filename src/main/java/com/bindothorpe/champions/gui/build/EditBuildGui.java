package com.bindothorpe.champions.gui.build;

import com.bindothorpe.champions.DomainController;
import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.domain.skill.SkillType;
import com.bindothorpe.champions.gui.PlayerGui;
import com.bindothorpe.champions.gui.items.BackItem;
import com.bindothorpe.champions.gui.items.BorderItem;
import com.bindothorpe.champions.gui.items.SkillTypeItem;
import com.bindothorpe.champions.util.TextUtil;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import java.util.UUID;

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

        for(SkillType skillType : SkillType.values()) {
            root.addItem(new SkillTypeItem(skillType), 1, skillType.ordinal());
        }

        gui.addPane(root);

    }
}
