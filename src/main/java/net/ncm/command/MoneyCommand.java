package net.ncm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.ncm.MoneyState;
import net.ncm.network.SyncMoneyPayload;

public class MoneyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("money")
                .requires(source -> source.hasPermissionLevel(2)) // Только для админов

                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> execute(context, "add"))
                                )
                        )
                )

                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> execute(context, "set"))
                                )
                        )
                )

                .then(CommandManager.literal("withdraw")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> execute(context, "withdraw"))
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context, String action) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
        long amount = LongArgumentType.getLong(context, "amount");
        long currentBalance = MoneyState.getBalance(context.getSource().getServer(), target.getUuid());
        long newBalance = currentBalance;

        switch (action) {
            case "add" -> newBalance = currentBalance + amount;
            case "set" -> newBalance = amount;
            case "withdraw" -> {
                newBalance = currentBalance - amount;
                if (newBalance < 0) newBalance = 0; // Не уходим в минус
            }
        }

        MoneyState state = MoneyState.getServerState(context.getSource().getServer());
        state.balances.put(target.getUuid(), newBalance);
        state.markDirty();

        ServerPlayNetworking.send(target, new SyncMoneyPayload(newBalance));

        String actionText = switch (action) {
            case "add" -> "Выдано";
            case "set" -> "Установлено";
            case "withdraw" -> "Отнято";
            default -> "";
        };
        context.getSource().sendFeedback(() -> Text.literal(actionText + " " + amount + " изумрудов игроку " + target.getName().getString()), false);
        return 1;
    }
}