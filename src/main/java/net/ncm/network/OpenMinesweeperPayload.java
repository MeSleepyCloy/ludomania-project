package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record OpenMinesweeperPayload() implements CustomPayload {
    public static final CustomPayload.Id<OpenMinesweeperPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "open_minesweeper"));
    public static final PacketCodec<RegistryByteBuf, OpenMinesweeperPayload> CODEC = CustomPayload.codecOf((p, b) -> {}, b -> new OpenMinesweeperPayload());
    @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}