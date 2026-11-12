package io.github.blazevortex.item;

import io.github.blazevortex.api.item.CustomItem;

import java.util.List;
import java.util.Optional;

public record CustomItemImpl(
        String id,
        String material,
        String displayName,
        List<String> lore,
        String modelPath,
        Integer customModelDataValue
) implements CustomItem {
    @Override
    public Optional<String> model() {
        return Optional.ofNullable(modelPath);
    }

    @Override
    public Optional<Integer> customModelData() {
        return Optional.ofNullable(customModelDataValue);
    }
}
