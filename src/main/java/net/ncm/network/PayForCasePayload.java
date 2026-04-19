package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record PayForCasePayload(String caseId, int amount) implements CustomPayload {
    public static final CustomPayload.Id<PayForCasePayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "pay_for_case"));

    public static final PacketCodec<RegistryByteBuf, PayForCasePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, PayForCasePayload::caseId,
            PacketCodecs.VAR_INT, PayForCasePayload::amount,
            PayForCasePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}