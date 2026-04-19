package net.ncm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.ncm.QuestRegistry;
import net.ncm.QuestState;
import net.ncm.network.SyncQuestPayload;

import java.util.Collection;

public class QuestCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("quest")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("questId", StringArgumentType.word())
                                        .executes(context -> execute(context, "add"))
                                )
                        )
                )
                .then(CommandManager.literal("win")
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                                .then(CommandManager.argument("questId", StringArgumentType.word())
                                        .executes(context -> execute(context, "win"))
                                )
                        )
                )
                .then(CommandManager.literal("check")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .then(CommandManager.argument("questId", StringArgumentType.word())
                                        .executes(context -> execute(context, "check"))
                                )
                        )
                )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> context, String action) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "targets");
        String questId = StringArgumentType.getString(context, "questId");

        QuestRegistry.QuestData data = QuestRegistry.getQuest(questId);
        if (data == null) {
            context.getSource().sendFeedback(() -> Text.literal("§cКвест с таким названием не найден!"), false);
            return 0;
        }

        int successCount = 0;

        for (ServerPlayerEntity target : targets) {
            if (action.equals("add")) {
                QuestState.setActiveQuest(context.getSource().getServer(), target.getUuid(), questId);
                ServerPlayNetworking.send(target, new SyncQuestPayload(questId + ":0", false));

                target.sendMessage(Text.literal("§eНовое задание: §f" + data.title()), false);
                target.playSoundToPlayer(net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.MASTER, 1.0f, 1.0f);
                successCount++;
            }

            else if (action.equals("win")) {
                String currentQuest = QuestState.getActiveQuest(context.getSource().getServer(), target.getUuid());
                if (!currentQuest.equals(questId)) {
                    continue;
                }

                QuestState.completeQuest(context.getSource().getServer(), target, questId);
                successCount++;
            }

            else if (action.equals("check")) {
                String currentQuest = QuestState.getActiveQuest(context.getSource().getServer(), target.getUuid());
                if (currentQuest.equals(questId)) {
                    successCount++;
                }
            }
        }

        if (successCount > 0) {
            final int count = successCount;
            context.getSource().sendFeedback(() -> Text.literal("§aДействие '" + action + "' успешно применено к " + count + " игрокам."), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("§cДействие не применено ни к одному игроку."), false);
        }

        return successCount;
    }
}