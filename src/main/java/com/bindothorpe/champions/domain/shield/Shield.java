package com.bindothorpe.champions.domain.shield;

import com.bindothorpe.champions.events.update.UpdateEvent;
import com.bindothorpe.champions.events.update.UpdateType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class Shield{

    private double amount;
    private final boolean isTemporary;
    private final double duration;
    private final boolean isDecaying;
    private final double decayPerTick;
    private final ShieldManager shieldManager;
    private final UUID owner;

    private Shield(double amount, boolean isTemporary, double duration, boolean isDecaying, double decayPerTick, ShieldManager shieldManager, UUID owner) {
        this.amount = amount;
        this.isTemporary = isTemporary;
        this.duration = duration;
        this.isDecaying = isDecaying;
        this.decayPerTick = decayPerTick;
        this.shieldManager = shieldManager;
        this.owner = owner;
        startDecay();
    }

    public double getAmount() {
        return amount;
    }

    public double reduceAmount(double amount) {
        this.amount -= amount;
        if (this.amount <= 0) {
            onBreak();
        }
        return this.amount;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public boolean isDecaying() {
        return isDecaying;
    }

    public void onBreak() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void startDecay() {
        if (isDecaying) {
            shieldManager.getDomainController().getPlugin().getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onTick(UpdateEvent event) {
                    if (event.getUpdateType() == UpdateType.TICK) {
                        reduceAmount(decayPerTick);
                    }
                }
            }, shieldManager.getDomainController().getPlugin());
        }
    }


    public static class ShieldBuilder {

        private final double amount;
        private boolean isTemporary;
        private double duration;
        private boolean isDecaying;
        private double decayPerTick;
        private final ShieldManager shieldManager;
        private final UUID owner;

        public ShieldBuilder(double amount, ShieldManager shieldManager, UUID owner) {
            this.amount = amount;
            this.shieldManager = shieldManager;
            this.owner = owner;
        }

        public ShieldBuilder temporary(boolean isTemporary, double duration) {
            this.isTemporary = isTemporary;
            this.duration = duration;
            return this;
        }

        public ShieldBuilder decaying(boolean isDecaying, double decayPerTick) {
            this.isDecaying = isDecaying;
            this.decayPerTick = decayPerTick;
            return this;
        }

        public Shield build() {
            return new Shield(amount, isTemporary, duration, isDecaying, decayPerTick, shieldManager, owner);
        }
    }
}
