package net.ncm.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.ncm.Ludomania;
import net.ncm.client.gui.AtmScreen;
import net.ncm.client.gui.CaseScreen;
import net.ncm.client.gui.MoneyHud;
import net.ncm.client.render.PedestalBlockEntityRenderer;
import net.ncm.network.AtmPlayersPayload;
import net.ncm.network.OpenCasePayload;
import net.ncm.network.SpinResultPayload;
import net.ncm.client.gui.MinesweeperScreen;
import net.ncm.network.OpenMinesweeperPayload;
import net.ncm.network.MinesweeperResponsePayload;
import net.ncm.network.SyncMoneyPayload;

import net.ncm.client.gui.QuestHud;
import net.ncm.network.SyncQuestPayload;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.ncm.client.render.TraderEntityRenderer;
import net.ncm.client.gui.TraderScreen;


@Environment(EnvType.CLIENT)
public class LudomaniaClient implements ClientModInitializer {
    public static boolean isFirstJoin = true;

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(Ludomania.TRADER_ENTITY, TraderEntityRenderer::new);
        HandledScreens.register(Ludomania.TRADER_SCREEN_HANDLER, TraderScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(OpenMinesweeperPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new MinesweeperScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MinesweeperResponsePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof MinesweeperScreen screen) {
                    screen.onStartResponse(payload.success());
                }
            });
        });

        BlockRenderLayerMap.INSTANCE.putBlock(Ludomania.MINESWEEPER_BLOCK, RenderLayer.getCutout());

        BlockEntityRendererFactories.register(Ludomania.PEDESTAL_BLOCK_ENTITY, PedestalBlockEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Ludomania.PEDESTAL_BLOCK, RenderLayer.getCutout());

        ClientPlayNetworking.registerGlobalReceiver(SyncQuestPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                QuestHud.setQuest(payload.questId(), payload.instant());
            });
        });

        HudRenderCallback.EVENT.register(new QuestHud());
        ClientPlayNetworking.registerGlobalReceiver(SpinResultPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof CaseScreen caseScreen) {
                    caseScreen.startSpinWithResults(payload.wonItems());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenCasePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new CaseScreen(payload.caseId()));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(SyncMoneyPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (isFirstJoin) {
                    MoneyHud.setBalanceInstant(payload.balance());
                    isFirstJoin = false;
                } else {
                    MoneyHud.setBalance(payload.balance());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(AtmPlayersPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new AtmScreen(payload.players()));
            });
        });

        HudRenderCallback.EVENT.register(new MoneyHud());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            QuestHud.displayQuestId = "";
            QuestHud.isAnimatingOut = false;
            isFirstJoin = true;
            MoneyHud.setBalanceInstant(0);
        });
    }
}