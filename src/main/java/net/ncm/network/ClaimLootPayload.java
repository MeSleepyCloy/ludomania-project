package net.ncm.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public record ClaimLootPayload(ItemStack wonItem) implements CustomPayload {
    public static final CustomPayload.Id<ClaimLootPayload> ID = new CustomPayload.Id<>(Identifier.of(Ludomania.MOD_ID, "claim_loot"));

    public static final PacketCodec<RegistryByteBuf, ClaimLootPayload> CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, ClaimLootPayload::wonItem,
            ClaimLootPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}