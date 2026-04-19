package net.ncm;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;
import net.ncm.network.SyncMoneyPayload;
import net.ncm.network.SyncQuestPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestState extends PersistentState {
    public final Map<UUID, String> activeQuests = new HashMap<>();

    public QuestState() {}

    public QuestState(Map<UUID, String> map) {
        this.activeQuests.putAll(map);
    }

    public static final Codec<QuestState> CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.STRING)
            .xmap(QuestState::new, state -> state.activeQuests);

    public static final PersistentStateType<QuestState> TYPE = new PersistentStateType<>(
            Ludomania.MOD_ID + "_quests",
            QuestState::new,
            CODEC,
            null
    );

    public static QuestState getServerState(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }

    public static String getActiveQuest(MinecraftServer server, UUID player) {
        String data = getServerState(server).activeQuests.getOrDefault(player, "");
        if (data.contains(":")) {
            return data.split(":")[0];
        }
        return data;
    }

    public static String getRawQuest(MinecraftServer server, UUID player) {
        return getServerState(server).activeQuests.getOrDefault(player, "");
    }

    public static int getQuestProgress(MinecraftServer server, UUID player) {
        String data = getServerState(server).activeQuests.getOrDefault(player, "");
        if (data.contains(":")) {
            try {
                return Integer.parseInt(data.split(":")[1]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static void setQuestProgress(MinecraftServer server, ServerPlayerEntity player, int progress) {
        QuestState state = getServerState(server);
        String currentQuest = getActiveQuest(server, player.getUuid());
        if (!currentQuest.isEmpty()) {
            String raw = currentQuest + ":" + progress;
            state.activeQuests.put(player.getUuid(), raw);
            state.markDirty();
           ServerPlayNetworking.send(player, new SyncQuestPayload(raw, true));
        }
    }

    public static void setActiveQuest(MinecraftServer server, UUID player, String questId) {
        QuestState state = getServerState(server);
        if (questId == null || questId.isEmpty()) {
            state.activeQuests.remove(player);
        } else {
            state.activeQuests.put(player, questId + ":0");
        }
        state.markDirty();
    }

    public static void completeQuest(MinecraftServer server, ServerPlayerEntity player, String questId) {
        QuestRegistry.QuestData data = QuestRegistry.getQuest(questId);
        if (data == null) return;

        setActiveQuest(server, player.getUuid(), "");
        ServerPlayNetworking.send(player, new SyncQuestPayload("", false));

        if (data.reward() > 0) {
            MoneyState.addBalance(server, player.getUuid(), data.reward());
            ServerPlayNetworking.send(player, new SyncMoneyPayload(MoneyState.getBalance(server, player.getUuid())));
            player.sendMessage(Text.literal("§aЗадание выполнено! Награда: " + data.reward() + ""), false);
        } else {
            player.sendMessage(Text.literal("§aЗадание выполнено!"), false);
        }

        player.playSoundToPlayer(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0f, 1.0f);
    }
}