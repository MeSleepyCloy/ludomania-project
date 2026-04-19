package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record OpenTraderPayload(String traderId, int entityId) implements CustomPayload {
    public static final CustomPayload.Id<OpenTraderPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "open_trader"));
    public static final PacketCodec<RegistryByteBuf, OpenTraderPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenTraderPayload::traderId,
            PacketCodecs.VAR_INT, OpenTraderPayload::entityId,
            OpenTraderPayload::new
    );
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}