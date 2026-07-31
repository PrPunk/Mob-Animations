package name.jschatz2.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import name.jschatz2.access.FangsAnimationAccess;

@Mixin(EvokerEntity.class)
public abstract class EvokerEntityMixin extends SpellcastingIllagerEntity implements FangsAnimationAccess {

    protected EvokerEntityMixin(EntityType<? extends SpellcastingIllagerEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void jschatz2$setFangsCasting(boolean casting) {
        this.setSpell(casting ? Spell.FANGS : Spell.NONE);
    }
}