package name.jschatz2;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;

public class MobAnimCommands {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                CommandManager.literal("mobanim")
                    .then(CommandManager.argument("mobs", EntityArgumentType.entities())

                        // no target
                        .executes(ctx -> MobAnimator.animate(
                            EntityArgumentType.getEntities(ctx, "mobs"),
                            null))

                        // optional player target
                        .then(CommandManager.argument("target", EntityArgumentType.entity())
                            .executes(ctx -> {
                                Entity entity = EntityArgumentType.getEntity(ctx, "target");

                                if (!(entity instanceof LivingEntity target)) {
                                    throw new SimpleCommandExceptionType(
                                        Text.literal("Target must be a living entity")
                                    ).create();
                                }

                                return MobAnimator.animate(
                                    EntityArgumentType.getEntities(ctx, "mobs"),
                                    target
                                );
                            })
                        )
                    )
            );
        });
    }
}