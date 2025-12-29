package com.bindothorpe.champions.dialogs.map.edit;

import com.bindothorpe.champions.domain.build.ClassType;
import com.bindothorpe.champions.util.TextUtil;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SelectClassTypeDialog {

    public final static String INPUT_KEY = "classType";

    public static Dialog createActions(Consumer<ClassType> consumer) {
        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Class Type", NamedTextColor.WHITE))
                        .body(List.of(
                                DialogBody.plainMessage(Component.text("Select the class type for the champion selector game object", NamedTextColor.WHITE))
                        )).build())
                .type(DialogType.multiAction(
                        List.of(
                                getActionButtonForClassType(ClassType.ASSASSIN, consumer),
                                getActionButtonForClassType(ClassType.MAGE, consumer),
                                getActionButtonForClassType(ClassType.KNIGHT, consumer),
                                getActionButtonForClassType(ClassType.RANGER, consumer),
                                getActionButtonForClassType(ClassType.BRUTE, consumer)),
                        ActionButton.create(
                                Component.text("Cancel", NamedTextColor.GRAY),
                                null,
                                100,
                                null
                        ),
                        1
                        )
                )
        );
    }

    private static ActionButton getActionButtonForClassType(ClassType classType, Consumer<ClassType> consumer) {
        return ActionButton.create(
                Component.text(TextUtil.camelCasing(classType.name()), NamedTextColor.WHITE),
                null,
                100,
                DialogAction.customClick(
                        (view, audience) -> {
                            consumer.accept(classType);
                        },
                        ClickCallback.Options.builder()
                                .uses(1)
                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                .build()
                )
        );
    }


    public static Dialog create(DialogActionCallback dialogActionCallback) {

        List<SingleOptionDialogInput.OptionEntry> options = new ArrayList<>();

        Arrays.stream(ClassType.values()).filter(classType -> !classType.equals(ClassType.GLOBAL))
                .forEach(classType -> options.add(SingleOptionDialogInput.OptionEntry.create(
                        classType.toString(),
                        Component.text(classType.name()),
                        classType.equals(ClassType.ASSASSIN))));

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Component.text("Select a class"))
                        .inputs(List.of(
//                                ActionButton.create(
//                                        Component.text("Assassin", NamedTextColor.WHITE),
//                                        null,
//                                        100,
//                                        DialogAction.customClick(dialogActionCallback,
//                                                ClickCallback.Options.builder()
//                                                        .uses(1)
//                                                        .lifetime(ClickCallback.DEFAULT_LIFETIME)
//                                                        .build())
//                                )
                                DialogInput.singleOption("classType", Component.text("Class"), options)
                                        .build()
                        )).build())
                .type(DialogType.confirmation(
                        ActionButton.create(
                                Component.text("Confirm", NamedTextColor.WHITE),
                                Component.text("Click to confirm your input."),
                                100,
                                DialogAction.customClick(dialogActionCallback,
                                        ClickCallback.Options.builder()
                                                .uses(1) // Set the number of uses for this callback. Defaults to 1
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME) // Set the lifetime of the callback. Defaults to 12 hours
                                                .build())
                        ),
                        ActionButton.create(
                                Component.text("Cancel", NamedTextColor.GRAY),
                                Component.text("Click to discard your input."),
                                100,
                                null
                        )
                ))
        );
    }
}
