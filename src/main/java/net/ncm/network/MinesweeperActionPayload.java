package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record MinesweeperActionPayload(int action, long amount) implements CustomPayload {
    public static final CustomPayload.Id<MinesweeperActionPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "mine_action"));
    public static final PacketCodec<RegistryByteBuf, MinesweeperActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, MinesweeperActionPayload::action,
            PacketCodecs.VAR_LONG, MinesweeperActionPayload::amount,
            MinesweeperActionPayload::new
    );
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}