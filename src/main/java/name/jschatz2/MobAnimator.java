package name.jschatz2;

import java.util.Collection;

import name.jschatz2.access.FangsAnimationAccess;
import name.jschatz2.util.ForcedTargetable;
import name.jschatz2.util.TickScheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
// import net.minecraft.entity.effect.StatusEffectInstance;
// import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MobAnimator {

    public static void faceEntity(LivingEntity mob, Entity target) {
        Vec3d delta = target.getPos().subtract(mob.getPos());
        double horizontalDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        float yaw = (float) (MathHelper.atan2(delta.z, delta.x) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) -(MathHelper.atan2(delta.y, horizontalDist) * (180.0 / Math.PI));

        mob.setYaw(yaw);
        mob.setBodyYaw(yaw);
        mob.setHeadYaw(yaw);
        mob.setPitch(pitch);
    }

    public static void bigLeap(FrogEntity frog, LivingEntity target, double apexHeight) {
        World world = frog.getWorld();
        Vec3d start = frog.getPos();
        Vec3d end = target.getPos();

        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double dy = end.y - start.y;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        double gravity = 0.08; // per-tick gravity most mobs use

        double vy0 = Math.sqrt(2 * gravity * apexHeight); // vertical speed to reach apexHeight
        double t = (vy0 + Math.sqrt(vy0 * vy0 - 2 * gravity * dy)) / gravity; // ticks in the air

        // *1.15 roughly compensates for air drag, which this simple formula ignores
        double horizontalSpeed = (horizontalDist / t) * 1.15;
        double vx = horizontalDist > 0 ? horizontalSpeed * (dx / horizontalDist) : 0;
        double vz = horizontalDist > 0 ? horizontalSpeed * (dz / horizontalDist) : 0;

        frog.setVelocity(vx, vy0, vz);
        frog.velocityModified = true;
        frog.setOnGround(false);
        frog.setPose(EntityPose.LONG_JUMPING);

        world.playSound(null, frog.getBlockPos(),
            SoundEvents.ENTITY_FROG_LONG_JUMP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    public static void waitForLanding(FrogEntity frog) {
        TickScheduler.schedule(1, () -> {
            if (frog.isRemoved()) {
                return;
            }

            if (frog.isOnGround()) {
                frog.setPose(EntityPose.STANDING);
            } else {
                waitForLanding(frog);
            }
        });
    }

    public static void ramCharge(GoatEntity goat, LivingEntity target) {
        World world = goat.getWorld();

        goat.getNavigation().stop();
        faceEntity(goat, target);

        world.playSound(null, goat.getBlockPos(),
            SoundEvents.ENTITY_GOAT_PREPARE_RAM, SoundCategory.NEUTRAL, 1.0F, 1.0F);

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.sendEntityStatus(goat, (byte) 58); // triggers preparingRam = true client-side -> head lowers
        }

        TickScheduler.schedule(10, () -> {
            if (goat.isRemoved()) return;

            Vec3d start = goat.getPos();
            Vec3d end = target.getPos();
            int chargeTicks = 12; // tune for run speed

            for (int i = 1; i <= chargeTicks; i++) {
                final int tick = i;
                TickScheduler.schedule(tick, () -> {
                    if (goat.isRemoved()) return;

                    goat.getNavigation().stop();
                    faceEntity(goat, target);

                    double progress = (double) tick / chargeTicks;
                    double x = MathHelper.lerp(progress, start.x, end.x);
                    double z = MathHelper.lerp(progress, start.z, end.z);
                    double y = MathHelper.lerp(progress, start.y, end.y); // flat ground charge, no arc

                    goat.setPosition(x, y, z);
                });
            }

            TickScheduler.schedule(chargeTicks, () -> {
                if (goat.isRemoved() || target.isRemoved()) return;

                goat.setVelocity(Vec3d.ZERO);

                if (goat.getWorld() instanceof ServerWorld serverWorld2) {
                    serverWorld2.sendEntityStatus(goat, (byte) 59); // preparingRam = false -> head rises back up

                    goat.getWorld().playSound(null, goat.getBlockPos(),
                        SoundEvents.ENTITY_GOAT_RAM_IMPACT, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                    target.damage(serverWorld2, goat.getDamageSources().mobAttack(goat), 8.0F);
                }

                target.takeKnockback(1.4F, goat.getX() - target.getX(), goat.getZ() - target.getZ());
            });
        });
    }

    public static void faceEntityInstant(ChickenEntity chicken, Entity target, int holdTicks) {
        chicken.getNavigation().stop();

        for (int i = 0; i <= holdTicks; i++) {
            TickScheduler.schedule(i, () -> {
                if (chicken.isRemoved()) return;
                faceEntity(chicken, target);
            });
        }
    }

    public static int animate(Collection<? extends Entity> entities, LivingEntity target) {

        PlayerEntity player = target instanceof PlayerEntity p ? p : null;

        for (Entity entity : entities) {

            if (entity instanceof WardenEntity warden) {

                // faceEntity(warden, target);

                // warden.setAiDisabled(false);

                warden.updateAttackTarget(target);


                if (player != null) ForcedTargetable.mark(player, true);

                warden.setPose(EntityPose.ROARING);
                warden.roaringAnimationState.start(warden.age);

                TickScheduler.schedule(10, () -> {
                    warden.getWorld().playSound(null, warden.getBlockPos(),
                        SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 3.0F, 1.0F);
                });

               

                // target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 260, 0));

                // vanilla roar lasts ~84 ticks — reset only after that
                TickScheduler.schedule(84, () -> {
                    warden.setPose(EntityPose.STANDING);
                    warden.roaringAnimationState.stop();
                    if (player != null) ForcedTargetable.mark(player, false);
                    // warden.setAiDisabled(true);
                });

            } else if (entity instanceof EvokerEntity evoker) {

                faceEntity(evoker, target);

                if (player != null) ForcedTargetable.mark(player, true);

                FangsAnimationAccess access = (FangsAnimationAccess) evoker;
                access.jschatz2$setFangsCasting(true);

                evoker.getWorld().playSound(null, evoker.getBlockPos(),
                    SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK, SoundCategory.HOSTILE, 1.0F, 1.0F);

                // wind-up delay before the fangs actually bite, matching vanilla's feel
                TickScheduler.schedule(20, () -> {
                    access.jschatz2$setFangsCasting(false);

                    // spawn the fangs at the target now that the cast animation has finished
                    // ...EvokerFangsEntity spawn code from earlier...

                    if (player != null) ForcedTargetable.mark(player, false);
                });

            } else if (entity instanceof FrogEntity frog) {

                // single-tick jump, no waiting needed
                // frog.getJumpControl().setActive();
                bigLeap(frog, target, 8);

                waitForLanding(frog);

            } else if (entity instanceof BreezeEntity breeze) {

                faceEntity(breeze, target);

                if (player != null) ForcedTargetable.mark(player, true);

                breeze.setPose(EntityPose.SHOOTING);
                breeze.shootingAnimationState.start(breeze.age);

                breeze.getWorld().playSound(null, breeze.getBlockPos(),
                    SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.HOSTILE, 1.0F, 1.0F);

                TickScheduler.schedule(6, () -> {
                    BreezeWindChargeEntity windCharge = new BreezeWindChargeEntity(breeze, breeze.getWorld());

                    Vec3d origin = breeze.getEyePos();
                    windCharge.setPosition(origin.x, origin.y, origin.z);

                    Vec3d toTarget = target.getEyePos().subtract(origin).normalize();
                    windCharge.setVelocity(toTarget.x, toTarget.y, toTarget.z, 1.5F, 0.0F);

                    breeze.getWorld().spawnEntity(windCharge);
                });

                TickScheduler.schedule(14, () -> {
                    breeze.setPose(EntityPose.STANDING);
                    breeze.shootingAnimationState.stop();
                    if (player != null) ForcedTargetable.mark(player, false);
                });
            } else if (entity instanceof VillagerEntity villager) {

                faceEntity(villager, target);

                villager.getWorld().playSound(null, villager.getBlockPos(),
                    SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.NEUTRAL, 1.0F, 1.0F);

            } else if (entity instanceof IronGolemEntity golem) {

                faceEntity(golem, target);

                // make golem walk to target
                golem.getNavigation().startMovingTo(target, 1.0D); // 1.0 = normal walk speed
            } else if (entity instanceof ChickenEntity chicken) {

                faceEntityInstant(chicken, target, 10);

            } else if (entity instanceof GoatEntity goat) {
                ramCharge(goat, target);
            }
        }

        return entities.size();
    }
}