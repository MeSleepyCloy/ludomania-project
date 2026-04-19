package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record SellItemPayload(String traderId, int offerIndex) implements CustomPayload {
    public static final CustomPayload.Id<SellItemPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "sell_item"));
    public static final PacketCodec<RegistryByteBuf, SellItemPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SellItemPayload::traderId,
            PacketCodecs.VAR_INT, SellItemPayload::offerIndex,
            SellItemPayload::new
    );
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}