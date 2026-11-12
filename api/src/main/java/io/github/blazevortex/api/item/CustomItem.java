package io.github.blazevortex.api.item;

import java.util.List;
import java.util.Optional;

public interface CustomItem {

    String id();

    String material();

    String displayName();

    List<String> lore();

    Optional<String> model();

    Optional<Integer> customModelData();
}
