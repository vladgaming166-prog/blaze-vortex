package io.github.blazevortex.api.event;

import io.github.blazevortex.api.emoji.Emoji;

public final class EmojiRegisterEvent extends BvxEvent {

    private final Emoji emoji;

    public EmojiRegisterEvent(Emoji emoji) {
        this.emoji = emoji;
    }

    public Emoji emoji() {
        return emoji;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }
}
