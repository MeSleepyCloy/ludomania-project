package net.ncm.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.ncm.block.entity.PedestalBlockEntity;

public class PedestalBlockEntityRenderer implements BlockEntityRenderer<PedestalBlockEntity> {

    public PedestalBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

    }

    @Override
    public void render(PedestalBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, net.minecraft.util.math.Vec3d cameraPos) {
        ItemStack stack = entity.getInventory().get(0);
        if (stack.isEmpty()) return;

        matrices.push();

        long time = entity.getWorld() == null ? 0 : entity.getWorld().getTime();

        float offset = MathHelper.sin((time + tickProgress) / 10.0F) * 0.1F;

        matrices.translate(0.5, 1.2 + offset, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((time + tickProgress) * 4));
        matrices.scale(1.5f, 1.5f, 1.5f);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                stack,
                net.minecraft.item.ItemDisplayContext.GROUND, // <-- ИСПРАВЛЕНО
                light,
                overlay,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0
        );

        matrices.pop();
    }
}