package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record CaseErrorPayload(String message) implements CustomPayload {
    public static final CustomPayload.Id<CaseErrorPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "case_error"));

    public static final PacketCodec<RegistryByteBuf, CaseErrorPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, CaseErrorPayload::message,
            CaseErrorPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}