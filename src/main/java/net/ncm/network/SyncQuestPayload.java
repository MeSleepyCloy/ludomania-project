package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record SyncQuestPayload(String questId, boolean instant) implements CustomPayload {
    public static final CustomPayload.Id<SyncQuestPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "sync_quest"));

    public static final PacketCodec<RegistryByteBuf, SyncQuestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SyncQuestPayload::questId,
            PacketCodecs.BOOLEAN, SyncQuestPayload::instant,
            SyncQuestPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}