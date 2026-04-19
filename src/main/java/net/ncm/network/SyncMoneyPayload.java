package net.ncm.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record SyncMoneyPayload(long balance) implements CustomPayload {
    public static final CustomPayload.Id<SyncMoneyPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "sync_money"));
    public static final PacketCodec<RegistryByteBuf, SyncMoneyPayload> CODEC = CustomPayload.codecOf(
            (payload, buf) -> buf.writeLong(payload.balance()),
            buf -> new SyncMoneyPayload(buf.readLong())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}