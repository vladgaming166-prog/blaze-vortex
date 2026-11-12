package io.github.blazevortex.example;

import io.github.blazevortex.api.BlazeVortex;
import io.github.blazevortex.api.BlazeVortexProvider;
import io.github.blazevortex.api.component.RenderContext;
import io.github.blazevortex.api.emoji.EmojiRegistration;
import io.github.blazevortex.api.event.EmojiRegisterEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

/**
 * Working integration example for third-party plugins.
 *
 * <p>Add {@code softdepend: [BlazeVortex]} and obtain the API through
 * {@link BlazeVortexProvider}. Never import implementation classes.
 */
public final class ExampleBvxIntegration extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!BlazeVortexProvider.isAvailable()) {
            getLogger().warning("Blaze Vortex is not loaded; example integration idle.");
            return;
        }
        BlazeVortex bvx = BlazeVortexProvider.get();
        Path icon = bvx.contentFolder().resolve("emojis/example.png");
        if (java.nio.file.Files.isRegularFile(icon)) {
            bvx.emojis().register(EmojiRegistration.builder("example_logo", icon).size(75).build());
        }
        Component motd = bvx.renderer().render("<fire> Hello from an integrating plugin", RenderContext.CHAT);
        getLogger().info("Rendered sample: " + motd);
        getLogger().info("BVX version " + bvx.version() + " with " + bvx.emojis().all().size() + " emojis");
    }

    public static Component render(String text) {
        return BlazeVortexProvider.get()
                .emojis()
                .find("fire")
                .map(emoji -> Component.text(String.valueOf(emoji.glyph())))
                .orElse(Component.text(text));
    }

    public static void listen(BlazeVortex ignored) {
        // Subscribe through the engine event bus from the implementing plugin:
        // ((io.github.blazevortex.core.BlazeVortexEngine) BlazeVortexProvider.get())
        //     .events().subscribe(EmojiRegisterEvent.class, event -> { ... });
        EmojiRegisterEvent.class.getName();
        RenderContext.CHAT.name();
    }
}
