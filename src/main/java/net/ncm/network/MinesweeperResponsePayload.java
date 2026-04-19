package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record MinesweeperResponsePayload(boolean success) implements CustomPayload {
    public static final CustomPayload.Id<MinesweeperResponsePayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "mine_response"));
    public static final PacketCodec<RegistryByteBuf, MinesweeperResponsePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, MinesweeperResponsePayload::success,
            MinesweeperResponsePayload::new
    );
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}