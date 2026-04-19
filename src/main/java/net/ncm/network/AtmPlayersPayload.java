package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

import java.util.List;

public record AtmPlayersPayload(List<String> players) implements CustomPayload {
    public static final CustomPayload.Id<AtmPlayersPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "atm_players"));

    public static final PacketCodec<RegistryByteBuf, AtmPlayersPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.collect(PacketCodecs.toList()), AtmPlayersPayload::players,
            AtmPlayersPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}