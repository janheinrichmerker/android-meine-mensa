package com.heinrichreimer.meinemensa.network;

public class SyncStatusChangedEvent {
    private static int COUNTER = 0;

    private SyncStatusChangedEvent() {
    }

    static SyncStatusChangedEvent start() {
        COUNTER++;
        return new SyncStatusChangedEvent();
    }

    static SyncStatusChangedEvent finish() {
        COUNTER = Math.max(0, COUNTER - 1);
        return new SyncStatusChangedEvent();
    }

    public static boolean isSyncing() {
        return COUNTER > 0;
    }
}
