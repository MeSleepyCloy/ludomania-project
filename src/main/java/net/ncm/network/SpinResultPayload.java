package net.ncm.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

import java.util.List;

public record SpinResultPayload(List<ItemStack> wonItems) implements CustomPayload {
    public static final CustomPayload.Id<SpinResultPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "spin_result"));

    public static final PacketCodec<RegistryByteBuf, SpinResultPayload> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()), SpinResultPayload::wonItems,
            SpinResultPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}