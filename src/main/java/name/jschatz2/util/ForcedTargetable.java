package name.jschatz2.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ForcedTargetable {
    private static final Set<UUID> FORCED = new HashSet<>();

    private ForcedTargetable() {}

    public static void mark(PlayerEntity player, boolean forced) {
        if (forced) {
            FORCED.add(player.getUuid());
        } else {
            FORCED.remove(player.getUuid());
        }
    }

    public static boolean isForced(LivingEntity entity) {
        return entity instanceof PlayerEntity player && FORCED.contains(player.getUuid());
    }
}