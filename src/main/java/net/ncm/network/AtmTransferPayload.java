package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record AtmTransferPayload(String targetName, long amount) implements CustomPayload {
    public static final CustomPayload.Id<AtmTransferPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "atm_transfer"));

    public static final PacketCodec<RegistryByteBuf, AtmTransferPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AtmTransferPayload::targetName,
            PacketCodecs.VAR_LONG, AtmTransferPayload::amount,
            AtmTransferPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}