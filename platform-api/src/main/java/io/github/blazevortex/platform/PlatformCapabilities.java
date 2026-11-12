package io.github.blazevortex.platform;

public final class PlatformCapabilities {

    private final boolean displayEntities;
    private final boolean itemDisplays;
    private final boolean textDisplays;
    private final boolean nativeAdventure;
    private final boolean mapViews;
    private final boolean customModelData;
    private final boolean itemModelComponents;
    private final boolean paperPluginLoader;
    private final boolean brigadier;
    private final boolean resourcePackEvents;

    private PlatformCapabilities(Builder builder) {
        this.displayEntities = builder.displayEntities;
        this.itemDisplays = builder.itemDisplays;
        this.textDisplays = builder.textDisplays;
        this.nativeAdventure = builder.nativeAdventure;
        this.mapViews = builder.mapViews;
        this.customModelData = builder.customModelData;
        this.itemModelComponents = builder.itemModelComponents;
        this.paperPluginLoader = builder.paperPluginLoader;
        this.brigadier = builder.brigadier;
        this.resourcePackEvents = builder.resourcePackEvents;
    }

    public boolean displayEntities() {
        return displayEntities;
    }

    public boolean itemDisplays() {
        return itemDisplays;
    }

    public boolean textDisplays() {
        return textDisplays;
    }

    public boolean nativeAdventure() {
        return nativeAdventure;
    }

    public boolean mapViews() {
        return mapViews;
    }

    public boolean customModelData() {
        return customModelData;
    }

    public boolean itemModelComponents() {
        return itemModelComponents;
    }

    public boolean paperPluginLoader() {
        return paperPluginLoader;
    }

    public boolean brigadier() {
        return brigadier;
    }

    public boolean resourcePackEvents() {
        return resourcePackEvents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean displayEntities;
        private boolean itemDisplays;
        private boolean textDisplays;
        private boolean nativeAdventure;
        private boolean mapViews = true;
        private boolean customModelData = true;
        private boolean itemModelComponents;
        private boolean paperPluginLoader;
        private boolean brigadier;
        private boolean resourcePackEvents;

        public Builder displayEntities(boolean value) {
            this.displayEntities = value;
            return this;
        }

        public Builder itemDisplays(boolean value) {
            this.itemDisplays = value;
            return this;
        }

        public Builder textDisplays(boolean value) {
            this.textDisplays = value;
            return this;
        }

        public Builder nativeAdventure(boolean value) {
            this.nativeAdventure = value;
            return this;
        }

        public Builder mapViews(boolean value) {
            this.mapViews = value;
            return this;
        }

        public Builder customModelData(boolean value) {
            this.customModelData = value;
            return this;
        }

        public Builder itemModelComponents(boolean value) {
            this.itemModelComponents = value;
            return this;
        }

        public Builder paperPluginLoader(boolean value) {
            this.paperPluginLoader = value;
            return this;
        }

        public Builder brigadier(boolean value) {
            this.brigadier = value;
            return this;
        }

        public Builder resourcePackEvents(boolean value) {
            this.resourcePackEvents = value;
            return this;
        }

        public PlatformCapabilities build() {
            return new PlatformCapabilities(this);
        }
    }
}
