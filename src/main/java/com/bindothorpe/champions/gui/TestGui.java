package com.bindothorpe.champions.gui;

import com.bindothorpe.champions.gui.items.global.BorderItem;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.entity.Player;

public class TestGui {

    private ChestGui gui;

    public void showGui(Player player) {
        if(gui == null)
            createGui();

        gui.show(player);
    }

    private void createGui() {
        gui = new ChestGui(6, "Select Build");
        StaticPane pane1 = new StaticPane(0, 0, 9, 6, Pane.Priority.HIGH);

        GuiItem border = new BorderItem();

        for(int i = 0; i < 54; i++) {
            pane1.addItem(border, i % 9, i / 9);
        }


//        pane1.addItem(new ClassIconItem(ClassType.ASSASSIN, dc), 0, 1);
//        pane1.addItem(new ClassIconItem(ClassType.MAGE), 2, 1);
//        pane1.addItem(new ClassIconItem(ClassType.KNIGHT), 4, 1);
//        pane1.addItem(new ClassIconItem(ClassType.RANGER), 6, 1);
//        pane1.addItem(new ClassIconItem(ClassType.BRUTE), 8, 1);


        gui.addPane(pane1);
    }

}
