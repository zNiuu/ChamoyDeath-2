package io.github.zniuu.chamoydeath.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatModeManager {

    private final Map<UUID, ChatMode> modos = new HashMap<>();

    public ChatMode getMode(UUID uuid) {
        return modos.getOrDefault(uuid, ChatMode.GLOBAL);
    }

    public void setMode(UUID uuid, ChatMode mode) {
        modos.put(uuid, mode);
    }
}
