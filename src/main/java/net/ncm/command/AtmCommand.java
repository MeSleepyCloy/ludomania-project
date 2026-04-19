package net.ncm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ncm.network.AtmPlayersPayload;

import java.util.List;

public class AtmCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("atm")
                .executes(AtmCommand::execute)
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.isExecutedByPlayer()) {
            ServerPlayerEntity sender = source.getPlayer();

            List<String> onlinePlayers = source.getServer().getPlayerManager().getPlayerList().stream()
                    .filter(p -> !p.getUuid().equals(sender.getUuid()))
                    .map(p -> p.getName().getString())
                    .toList();

            ServerPlayNetworking.send(sender, new AtmPlayersPayload(onlinePlayers));
        }
        return 1;
    }
}