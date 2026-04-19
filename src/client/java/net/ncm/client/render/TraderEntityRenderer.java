package net.ncm.client.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.ncm.TraderRegistry;
import net.ncm.entity.TraderEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class TraderEntityRenderer extends EntityRenderer<TraderEntity, TraderEntityRenderer.TraderRenderState> {

    private final TraderInternalRenderer defaultRenderer;
    private final TraderInternalRenderer slimRenderer;

    public TraderEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.defaultRenderer = new TraderInternalRenderer(ctx, false);
        this.slimRenderer = new TraderInternalRenderer(ctx, true);
    }

    @Override
    public TraderRenderState createRenderState() {
        return new TraderRenderState();
    }

    @Override
    public void updateRenderState(TraderEntity entity, TraderRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        state.traderId = entity.getTraderId();
        TraderRegistry.TraderProfile profile = TraderRegistry.getTrader(state.traderId);
        state.isSlim = profile != null && profile.isSlim();

        if (state.isSlim) {
            this.slimRenderer.updateRenderState(entity, state.internalState, tickDelta);
        } else {
            this.defaultRenderer.updateRenderState(entity, state.internalState, tickDelta);
        }
    }

    @Override
    public void render(TraderRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (state.isSlim) {
            this.slimRenderer.render(state.internalState, matrices, vertexConsumers, light);
        } else {
            this.defaultRenderer.render(state.internalState, matrices, vertexConsumers, light);
        }
    }

    public static class TraderRenderState extends EntityRenderState {
        public String traderId = "trader1";
        public boolean isSlim = false;
        public final PlayerEntityRenderState internalState = new PlayerEntityRenderState();
    }

    private static class TraderInternalRenderer extends BipedEntityRenderer<TraderEntity, PlayerEntityRenderState, PlayerEntityModel> {

        private final Map<PlayerEntityRenderState, String> stateTraderIds = Collections.synchronizedMap(new WeakHashMap<>());

        public TraderInternalRenderer(EntityRendererFactory.Context ctx, boolean slim) {
            super(ctx, new PlayerEntityModel(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), slim), 0.5f);
        }

        @Override
        public PlayerEntityRenderState createRenderState() {
            return new PlayerEntityRenderState();
        }

        @Override
        public void updateRenderState(TraderEntity entity, PlayerEntityRenderState state, float tickDelta) {
            super.updateRenderState(entity, state, tickDelta);

            stateTraderIds.put(state, entity.getTraderId());

            state.hatVisible = true;
            state.jacketVisible = true;
            state.leftSleeveVisible = true;
            state.rightSleeveVisible = true;
            state.leftPantsLegVisible = true;
            state.rightPantsLegVisible = true;
        }

        @Override
        public Identifier getTexture(PlayerEntityRenderState state) {
            String id = stateTraderIds.getOrDefault(state, "trader1");
            TraderRegistry.TraderProfile profile = TraderRegistry.getTrader(id);
            if (profile != null) {
                return profile.texture();
            }
            return TraderRegistry.getTrader("trader1").texture();
        }
    }
}