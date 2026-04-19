package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;
import net.ncm.QuestRegistry;

public class QuestHud implements HudRenderCallback {
    public static String displayQuestId = "";
    public static int currentProgress = 0; // Переменная для хранения текущего прогресса
    public static boolean isAnimatingOut = false;
    public static long animStartTime = 0;
    public static boolean skipInAnimation = false;

    public static void setQuest(String payloadStr, boolean instant) {
        if (payloadStr == null || payloadStr.isEmpty()) {
            if (!displayQuestId.isEmpty() && !isAnimatingOut) {
                isAnimatingOut = true;
                animStartTime = Util.getMeasuringTimeMs();
            }
        } else {
            // Расшифровываем строку вида "id:прогресс"
            if (payloadStr.contains(":")) {
                String[] parts = payloadStr.split(":");
                displayQuestId = parts[0];
                try {
                    currentProgress = Integer.parseInt(parts[1]);
                } catch (Exception e) {
                    currentProgress = 0;
                }
            } else {
                displayQuestId = payloadStr;
                currentProgress = 0;
            }

            isAnimatingOut = false;
            if (instant) {
                animStartTime = Util.getMeasuringTimeMs() - 1000;
                skipInAnimation = true;
            } else {
                animStartTime = Util.getMeasuringTimeMs();
                skipInAnimation = false;
            }
        }
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (displayQuestId == null || displayQuestId.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;

        QuestRegistry.QuestData quest = QuestRegistry.getQuest(displayQuestId);
        if (quest == null) return;

        long now = Util.getMeasuringTimeMs();
        long elapsed = now - animStartTime;
        int animDuration = 500;

        if (isAnimatingOut && elapsed > animDuration) {
            displayQuestId = "";
            isAnimatingOut = false;
            return;
        }

        int screenWidth = context.getScaledWindowWidth();
        String title = "Текущее задание:";
        String questName = quest.title();
        String desc = quest.description();

        // Формируем текст прогресса, если он нужен
        String progText = "";
        boolean hasProgress = quest.maxProgress() > 1;
        if (hasProgress) {
            progText = "Прогресс: " + currentProgress + " / " + quest.maxProgress();
        }

        int width = Math.max(client.textRenderer.getWidth(questName), client.textRenderer.getWidth(desc)) + 20;
        if (hasProgress) {
            width = Math.max(width, client.textRenderer.getWidth(progText) + 20);
        }
        width = Math.max(width, 140);

        int height = hasProgress ? 65 : 50;

        double offsetX = 0;
        if (isAnimatingOut) {
            double t = (double) Math.min(elapsed, animDuration) / animDuration;
            offsetX = width * Math.pow(t, 3);
        } else if (!skipInAnimation && elapsed < animDuration) {
            double t = (double) elapsed / animDuration;
            offsetX = width * (1.0 - Math.pow(t, 3));
        }

        int x = screenWidth - width - 10 + (int) offsetX;
        int y = 10;

        context.fill(x, y, x + width, y + height, 0xAA222222);
        context.drawBorder(x, y, width, height, 0xFFFFAA00);

        context.drawTextWithShadow(client.textRenderer, title, x + 10, y + 5, 0xAAAAAA);
        context.drawTextWithShadow(client.textRenderer, questName, x + 10, y + 18, 0xFFFFFF);
        context.drawTextWithShadow(client.textRenderer, desc, x + 10, y + 33, 0x55FF55);

        // Отрисовка строки прогресса
        if (hasProgress) {
            context.drawTextWithShadow(client.textRenderer, progText, x + 10, y + 48, 0xFFFFAA00);
        }
    }
}