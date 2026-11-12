# Developer API

Maven/Gradle: depend on the `api` module (`io.github.blazevortex:api:1.0.0`) or compile against the plugin JAR as `provided`.

```java
BlazeVortex bvx = BlazeVortexProvider.get();
Component text = bvx.renderer().render("Welcome <fire>", RenderContext.CHAT);
bvx.emojis().register(EmojiRegistration.builder("logo", path).size(75).build());
bvx.videos().createScreen(request);
bvx.sounds().play(SoundPlayRequest.of("fanfare").player(uuid).build());
bvx.resourcePacks().build();
```

Events (engine bus; Bukkit adapters also log pack/join status):

- `EmojiRegisterEvent` (cancellable)
- `MediaUploadEvent` (cancellable)
- `VideoStartEvent` / `VideoStopEvent`
- `DisplayCreateEvent` (cancellable)
- `SoundPlayEvent` (cancellable)
- `ResourcePackBuildEvent`

Subscribe:

```java
((io.github.blazevortex.core.BlazeVortexEngine) BlazeVortexProvider.get())
    .events()
    .subscribe(EmojiRegisterEvent.class, event -> { });
```

A working sketch lives in `example/src/main/java/io/github/blazevortex/example/ExampleBvxIntegration.java`.

PlaceholderAPI (optional): `%bvx_emoji_<name>%`, `%bvx_emojis%`.

softdepend: `[BlazeVortex]`. Do not import `io.github.blazevortex.bukkit` or `paper` packages from other plugins.
