package com.bindothorpe.champions.domain.skill;

import com.bindothorpe.champions.util.ChatUtil;
import com.bindothorpe.champions.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AttemptResult {

    public static final AttemptResult FALSE = new AttemptResult(false);

    private final boolean result;
    private final Component message;
    private final ChatUtil.Prefix prefix;

    public AttemptResult(boolean result, Component message) {
        this.result = result;
        this.message = message;
        this.prefix = null;
    }

    public AttemptResult(boolean result, Component message, ChatUtil.Prefix prefix) {
        this.result = result;
        this.message = message;
        this.prefix = prefix;
    }

    public AttemptResult(boolean result, String message, ChatUtil.Prefix prefix) {
        this.result = result;
        this.message = message == null ? null : Component.text(message, NamedTextColor.GRAY);
        this.prefix = prefix;
    }
    public AttemptResult(boolean result, String message) {
        this.result = result;
        this.message = message == null ? null : Component.text(message, NamedTextColor.GRAY);
        this.prefix = null;
    }

    public AttemptResult(boolean result) {
        this.result = result;
        this.prefix = null;
        this.message = null;
    }

    public boolean result() {
        return result;
    }

    public Component message() {
        return message;
    }

    public ChatUtil.Prefix prefix() {
        return prefix;
    }
}
