package io.github.blazevortex.command;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Permissions {

    public static final String ADMIN = "blazevortex.admin";
    public static final String RELOAD = "blazevortex.reload";
    public static final String UPLOAD = "blazevortex.upload";
    public static final String EMOJI = "blazevortex.emoji";
    public static final String IMAGE = "blazevortex.image";
    public static final String GIF = "blazevortex.gif";
    public static final String VIDEO = "blazevortex.video";
    public static final String SOUND = "blazevortex.sound";
    public static final String SCREEN = "blazevortex.screen";
    public static final String PACK = "blazevortex.pack";
    public static final String HOLOGRAM = "blazevortex.hologram";
    public static final String GUI = "blazevortex.gui";
    public static final String DEBUG = "blazevortex.debug";
    public static final String USE = "blazevortex.use";

    private Permissions() {
    }

    public static boolean allows(CommandSenderView sender, String permission) {
        return sender.hasPermission(ADMIN) || sender.hasPermission(permission);
    }

    public static Map<String, String> descriptions() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(ADMIN, "All Blaze Vortex administrative commands");
        map.put(RELOAD, "Reload BVX configuration");
        map.put(UPLOAD, "Upload content into the BVX content directory");
        map.put(EMOJI, "Manage emojis");
        map.put(IMAGE, "Manage images");
        map.put(GIF, "Manage GIFs");
        map.put(VIDEO, "Manage videos and playback");
        map.put(SOUND, "Manage custom sounds");
        map.put(SCREEN, "Manage world screens");
        map.put(PACK, "Build and send resource packs");
        map.put(HOLOGRAM, "Manage holograms");
        map.put(GUI, "Open the BVX editor GUI");
        map.put(DEBUG, "View debug and status information");
        map.put(USE, "Use BVX emojis in chat");
        return map;
    }
}
