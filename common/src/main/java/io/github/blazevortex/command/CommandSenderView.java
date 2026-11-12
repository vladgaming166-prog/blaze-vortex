package io.github.blazevortex.command;

import java.util.UUID;

public interface CommandSenderView {

    String name();

    UUID uuid();

    boolean isPlayer();

    boolean hasPermission(String permission);

    void send(String message);
}
