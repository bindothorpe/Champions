package com.bindothorpe.champions.domain.game;

public enum GameState {
    LOBBY, LOBBY_COUNTDOWN, GAME_START_COUNTDOWN, IN_PROGRESS, GAME_END_COUNTDOWN;

    public GameState getNextState() {
        GameState[] states = GameState.values();
        int nextStateOrdinal = (this.ordinal() + 1) % states.length;
        return states[nextStateOrdinal];
    }
}
