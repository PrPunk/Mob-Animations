package name.jschatz2.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.WardenEntity$VibrationCallback")
public abstract class WardenVibrationCallbackMixin {

    @Inject(method = "accepts", at = @At("HEAD"), cancellable = true)
    private void jschatz2$ignoreMinecarts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, CallbackInfoReturnable<Boolean> cir) {
        Entity source = emitter.sourceEntity();
        if (source instanceof AbstractMinecartEntity) {
            cir.setReturnValue(false);
        }
    }
}