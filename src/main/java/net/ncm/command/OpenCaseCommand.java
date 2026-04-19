package net.ncm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.ncm.network.OpenCasePayload;

public class OpenCaseCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("opencase")
                .then(CommandManager.argument("caseId", StringArgumentType.word())
                        .executes(OpenCaseCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        String caseId = StringArgumentType.getString(context, "caseId");
        ServerCommandSource source = context.getSource();

        if (source.isExecutedByPlayer()) {
            ServerPlayerEntity player = source.getPlayer();
            if (player != null) {
                ServerPlayNetworking.send(player, new OpenCasePayload(caseId));

                System.out.println("Отправлен пакет открытия кейса '" + caseId + "' игроку " + player.getName().getString());
            }
        }
        return 1;
    }
}