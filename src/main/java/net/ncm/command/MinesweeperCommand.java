package net.ncm.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ncm.network.OpenMinesweeperPayload;

public class MinesweeperCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("minesweeper")
                .executes(context -> {
                    if (context.getSource().isExecutedByPlayer()) {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        ServerPlayNetworking.send(player, new OpenMinesweeperPayload());
                    }
                    return 1;
                })
        );
    }
}