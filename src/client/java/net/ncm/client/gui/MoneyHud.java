package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MoneyHud implements HudRenderCallback {

    private static final Identifier BACKGROUND = Identifier.of("ludomania", "textures/hud/hud.png");

    private static long targetBalance = 0;
    private static double displayBalance = 0;
    private static long startBalance = 0;
    private static long animationStartTime = 0;
    private static final long ANIM_DURATION = 1200L; // 2 секунды

    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);
    private static final List<FlyingNumber> flyingNumbers = new ArrayList<>();

    public static long getActualBalance() {
        return targetBalance;
    }

    public static void setBalance(long newBalance) {
        if (targetBalance == newBalance) return;

        if (animationStartTime == 0) {
            targetBalance = newBalance;
            displayBalance = newBalance;
            startBalance = newBalance;
            animationStartTime = Util.getMeasuringTimeMs() - ANIM_DURATION;
            return;
        }

        long diff = newBalance - targetBalance;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            if (diff > 0) {
                client.player.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            } else {
                client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }

            flyingNumbers.add(new FlyingNumber(diff, Util.getMeasuringTimeMs()));
        }

        startBalance = (long) displayBalance;
        targetBalance = newBalance;
        animationStartTime = Util.getMeasuringTimeMs();
    }

    public static void setBalanceInstant(long newBalance) {
        targetBalance = newBalance;
        displayBalance = newBalance;
        startBalance = newBalance;
        animationStartTime = Util.getMeasuringTimeMs() - ANIM_DURATION;
        flyingNumbers.clear();
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.player == null) return;

        long now = Util.getMeasuringTimeMs();

        if (now < animationStartTime + ANIM_DURATION) {
            double t = (double) (now - animationStartTime) / ANIM_DURATION;
            double easeOut = 1.0 - Math.pow(1.0 - t, 3);
            displayBalance = startBalance + (targetBalance - startBalance) * easeOut;
        } else {
            displayBalance = targetBalance;
        }

        int panelWidth = 120;
        int panelHeight = 35;

        int x = 10;
        int y = 10;

        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND,
                x, y-1, 0, 0, panelWidth, panelHeight, panelWidth, panelHeight);

        Identifier skin = client.player.getSkinTextures().texture();
        context.getMatrices().push();
        context.getMatrices().translate(20.5, 18.4, 0);
        context.getMatrices().scale(2.0f, 2.0f, 1.0f);
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, skin, 0, 0, 8.0F, 8.0F, 8, 8, 64, 64);
        context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured, skin, 0, 0, 40.0F, 8.0F, 8, 8, 64, 64);
        context.getMatrices().pop();

        String moneyText = formatMoney((long) displayBalance);
        int textX = x + 40;
        int textY = y + 13;
        context.drawTextWithShadow(client.textRenderer, moneyText, textX, textY, 0xFFFFFF);

        double offsetX = -1;
        double offsetY = 8;

        int textWidth = client.textRenderer.getWidth(moneyText);
        context.drawItem(EMERALD,
                textX + textWidth + (int)Math.round(offsetX), y + (int)Math.round(offsetY));;

        Iterator<FlyingNumber> it = flyingNumbers.iterator();
        while (it.hasNext()) {
            FlyingNumber fn = it.next();
            long elapsed = now - fn.startTime;
            if (elapsed > 2000) {
                it.remove();
                continue;
            }

            float progress = elapsed / 2000.0f;
            int alpha = (int) ((1.0f - progress) * 255);
            if (alpha < 5) alpha = 5;

            int currentY;
            if (fn.diff > 0) {
                currentY = textY - 20 + (int) (progress * 20);
            } else {
                currentY = textY + (int) (progress * 20);
            }

            String sign = fn.diff > 0 ? "+" : "-";
            String fnText = sign + formatMoney(Math.abs(fn.diff));
            int color = fn.diff > 0 ? 0x55FF55 : 0xFF5555; // Зеленый или Красный
            int colorWithAlpha = (alpha << 24) | color;

            context.drawTextWithShadow(client.textRenderer, fnText, textX, currentY, colorWithAlpha);
        }
    }

    public static String formatMoney(long amount) {
        if (amount < 1_000L) return String.valueOf(amount);
        if (amount < 1_000_000L) return formatDecimal(amount / 1_000.0) + "k";
        if (amount < 1_000_000_000L) return formatDecimal(amount / 1_000_000.0) + "m";
        if (amount < 1_000_000_000_000L) return formatDecimal(amount / 1_000_000_000.0) + "b";
        if (amount < 1_000_000_000_000_000L) return formatDecimal(amount / 1_000_000_000_000.0) + "t";
        if (amount < 1_000_000_000_000_000_000L) return formatDecimal(amount / 1_000_000_000_000_000.0) + "q";
        return formatDecimal(amount / 1_000_000_000_000_000_000.0) + "Q";
    }

    public static String formatDecimal(double value) {
        String s = String.format(Locale.US, "%.1f", value);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }

    private static class FlyingNumber {
        long diff;
        long startTime;
        FlyingNumber(long diff, long startTime) {
            this.diff = diff;
            this.startTime = startTime;
        }
    }
}