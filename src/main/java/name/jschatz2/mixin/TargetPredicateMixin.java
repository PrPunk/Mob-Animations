package name.jschatz2.mixin;

import name.jschatz2.util.ForcedTargetable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetPredicate.class)
public abstract class TargetPredicateMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void jschatz2$forceTargetable(ServerWorld world, LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> cir) {
        if (ForcedTargetable.isForced(targetEntity)) {
            cir.setReturnValue(true);
        }
    }
}