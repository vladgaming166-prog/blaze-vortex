package io.github.blazevortex.api.cosmetic;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CosmeticRegistry {

    Collection<Cosmetic> all();

    Optional<Cosmetic> find(String id);

    void equip(UUID playerId, String cosmeticId);

    void unequip(UUID playerId, String cosmeticId);

    Collection<String> equipped(UUID playerId);
}
