package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record OpenCasePayload(String caseId) implements CustomPayload {
    public static final CustomPayload.Id<OpenCasePayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "open_case"));

    public static final PacketCodec<RegistryByteBuf, OpenCasePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenCasePayload::caseId,
            OpenCasePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}