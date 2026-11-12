package io.github.blazevortex.api.component;

/**
 * Where an emoji or rich component is being shown. Contexts may apply size overrides
 * without changing the emoji's base {@code size} setting.
 */
public enum RenderContext {
    CHAT,
    SCOREBOARD,
    TAB_LIST,
    BOSS_BAR,
    ACTION_BAR,
    TITLE,
    SUBTITLE,
    HOLOGRAM,
    GUI_TITLE,
    GUI_ITEM,
    ITEM_LORE,
    ITEM_NAME,
    WORLD_DISPLAY,
    VIDEO_SCREEN,
    IMAGE_DISPLAY,
    CUSTOM
}
