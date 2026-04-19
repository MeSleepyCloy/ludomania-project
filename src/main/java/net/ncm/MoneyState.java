package net.ncm;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyState extends PersistentState {
    public final Map<UUID, Long> balances = new HashMap<>();

    public MoneyState() {
    }

    public MoneyState(Map<UUID, Long> map) {
        this.balances.putAll(map);
    }

    public static final Codec<MoneyState> CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.LONG)
            .xmap(MoneyState::new, state -> state.balances);

    public static final PersistentStateType<MoneyState> TYPE = new PersistentStateType<>(
            Ludomania.MOD_ID + "_money",
            MoneyState::new,
            CODEC,
            null
    );

    public static MoneyState getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return persistentStateManager.getOrCreate(TYPE);
    }

    public static long getBalance(MinecraftServer server, UUID player) {
        return getServerState(server).balances.getOrDefault(player, 0L);
    }

    public static void addBalance(MinecraftServer server, UUID player, long amount) {
        MoneyState state = getServerState(server);
        state.balances.put(player, state.balances.getOrDefault(player, 0L) + amount);
        state.markDirty();
    }
}