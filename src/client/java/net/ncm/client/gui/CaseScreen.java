package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.ncm.CaseRegistry;
import net.ncm.network.PayForCasePayload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CaseScreen extends Screen {
    private final String caseId;
    private final CaseRegistry.CaseData caseData;
    private final List<CaseRegistry.LootEntry> pool;

    private static final int[] AMOUNTS = {1, 2, 3, 5, 10};
    private int amountIndex = 0;

    private final int SPIN_DURATION = 10101;
    private final int ALIGN_DURATION = 650;

    private enum State { IDLE, WAITING_FOR_SERVER, SPINNING, ALIGNING, FINISHED }
    private State currentState = State.IDLE;

    private ButtonWidget toggleAmountButton;
    private ButtonWidget spinButton;
    private ButtonWidget closeButton;

    private final List<Roulette> roulettes = new ArrayList<>();
    private long stateStartTime = 0;

    private double dropScroll = 0;
    private final List<CaseError> errors = new ArrayList<>();

    public CaseScreen(String caseId) {
        super(Text.literal(caseId));
        this.caseId = caseId;
        this.caseData = CaseRegistry.getCase(caseId);
        this.pool = caseData.pool();
    }

    private CaseRegistry.LootEntry getRandomEntryWithChance() {
        int totalWeight = pool.stream().mapToInt(CaseRegistry.LootEntry::weight).sum();
        int randomVal = new Random().nextInt(totalWeight);
        for (CaseRegistry.LootEntry entry : pool) {
            randomVal -= entry.weight();
            if (randomVal < 0) return entry;
        }
        return pool.get(0);
    }

    private void showError(String text) {
        errors.add(new CaseError(text, Util.getMeasuringTimeMs()));
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    public void onCaseError(String message) {
        this.currentState = State.IDLE;
        this.updateButtonVisibility();
        this.showError(message);
    }

    @Override
    protected void init() {
        super.init();

        this.toggleAmountButton = ButtonWidget.builder(Text.literal("Количество: " + AMOUNTS[amountIndex]), button -> {
            if (currentState == State.IDLE) {
                amountIndex = (amountIndex + 1) % AMOUNTS.length;
                button.setMessage(Text.literal("Количество: " + AMOUNTS[amountIndex]));
                updateButtonVisibility();
            }
        }).dimensions(this.width / 2 - 125, this.height / 2 - 10, 120, 20).build();

        this.spinButton = ButtonWidget.builder(Text.literal("Крутить"), button -> {
            if (currentState == State.IDLE) {
                long totalCost = caseData.price() * AMOUNTS[amountIndex];
                if (MoneyHud.getActualBalance() < totalCost) {
                    showError("Недостаточно средств!");
                    return;
                }

                int emptySlots = 0;
                for (int i = 0; i < 36; i++) {
                    if (this.client.player.getInventory().getMainStacks().get(i).isEmpty()) {
                        emptySlots++;
                    }
                }
                if (emptySlots < AMOUNTS[amountIndex]) {
                    showError("Инвентарь забит!");
                    return;
                }

                currentState = State.WAITING_FOR_SERVER;
                updateButtonVisibility();
                ClientPlayNetworking.send(new PayForCasePayload(caseId, AMOUNTS[amountIndex]));
            }
        }).dimensions(this.width / 2 + 5, this.height / 2 - 10, 120, 20).build();

        this.closeButton = ButtonWidget.builder(Text.literal("Забрать"), button -> {
            this.close();
        }).dimensions(this.width / 2 - 60, this.height - 40, 120, 20).build();

        this.addDrawableChild(toggleAmountButton);
        this.addDrawableChild(spinButton);
        this.addDrawableChild(closeButton);
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (toggleAmountButton != null && spinButton != null && closeButton != null) {
            toggleAmountButton.visible = (currentState == State.IDLE);

            spinButton.visible = (currentState == State.IDLE || currentState == State.WAITING_FOR_SERVER);
            spinButton.active = (currentState == State.IDLE);

            closeButton.visible = (currentState == State.FINISHED);

            if (currentState == State.IDLE) {
                long totalCost = caseData.price() * AMOUNTS[amountIndex];
                spinButton.setMessage(Text.literal("Крутить (" + totalCost + ")"));
            } else if (currentState == State.WAITING_FOR_SERVER) {
                spinButton.setMessage(Text.literal("Ожидание..."));
            }
        }
    }

    public void startSpinWithResults(List<ItemStack> wonItems) {
        if (currentState != State.WAITING_FOR_SERVER) return;

        this.currentState = State.SPINNING;
        this.updateButtonVisibility();
        this.stateStartTime = Util.getMeasuringTimeMs();
        this.roulettes.clear();

        int amount = AMOUNTS[amountIndex];
        int itemWidth = 40;

        int visibleItems = (amount == 10) ? 5 : 7;
        int boxWidth = itemWidth * visibleItems;

        for (int i = 0; i < amount; i++) {
            this.roulettes.add(new Roulette(50, boxWidth, itemWidth, wonItems.get(i)));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentState == State.IDLE) {
            dropScroll -= verticalAmount * 20;

            int itemSize = 40;
            int spacing = 10;
            int cols = 4;
            int rows = (int) Math.ceil((double) pool.size() / cols);
            int totalContentHeight = rows * (itemSize + spacing) + 10;

            int startY = this.height / 2 + 35;
            int boxHeight = this.height - startY - 10;
            if (boxHeight > 130) {
                boxHeight = 130;
            }

            double maxScroll = Math.max(0, totalContentHeight - boxHeight);

            if (dropScroll < 0) dropScroll = 0;
            if (dropScroll > maxScroll) dropScroll = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        Text translatedTitle = Text.translatable("case.ludomania." + caseId);
        context.drawCenteredTextWithShadow(this.textRenderer, translatedTitle, this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Ваш баланс: " + MoneyHud.getActualBalance()), this.width / 2, 35, 0xFFFFAA00);

        updateSpinLogic();

        if (currentState == State.IDLE || currentState == State.WAITING_FOR_SERVER) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Выберите количество кейсов"), this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            renderDropList(context);
        } else if (currentState == State.SPINNING || currentState == State.ALIGNING) {
            renderRoulettes(context);
        } else if (currentState == State.FINISHED) {
            renderWinScreen(context);
        }

        renderErrors(context);
    }

    private void renderDropList(DrawContext context) {
        int cols = 4;
        int itemSize = 40;
        int spacing = 10;
        int boxWidth = cols * (itemSize + spacing) + 10;

        int startY = this.height / 2 + 35;
        int boxHeight = this.height - startY - 10;

        if (boxHeight > 130) {
            boxHeight = 130;
            startY = this.height - boxHeight - 20;
        }

        int startX = (this.width - boxWidth) / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Возможный дроп (Прокрутка колесиком):"), this.width / 2, startY - 15, 0xAAAAAA);
        context.fill(startX, startY, startX + boxWidth, startY + boxHeight, 0x88000000);
        context.enableScissor(startX, startY, startX + boxWidth, startY + boxHeight);

        for (int i = 0; i < pool.size(); i++) {
            CaseRegistry.LootEntry entry = pool.get(i);

            int col = i % cols;
            int row = i / cols;

            int x = startX + 10 + col * (itemSize + spacing);
            int y = startY + 10 + row * (itemSize + spacing) - (int) dropScroll;

            if (y + itemSize > startY && y < startY + boxHeight) {
                context.fill(x, y, x + itemSize, y + itemSize, 0xAA222222);
                context.drawBorder(x, y, itemSize, itemSize, entry.rarity().getColor());
                context.getMatrices().push();
                context.getMatrices().translate(x + itemSize / 2f, y + itemSize / 2f, 0);
                context.getMatrices().scale(1.5f, 1.5f, 1.5f);
                context.drawItem(entry.item(), -8, -8);
                context.getMatrices().pop();
            }
        }
        context.disableScissor();
        context.drawBorder(startX - 1, startY - 1, boxWidth + 2, boxHeight + 2, 0xFFFFFFFF);
    }

    private void renderErrors(DrawContext context) {
        long now = Util.getMeasuringTimeMs();
        Iterator<CaseError> it = errors.iterator();
        while (it.hasNext()) {
            CaseError err = it.next();
            long elapsed = now - err.startTime;
            if (elapsed > 2000) {
                it.remove();
                continue;
            }
            float progress = elapsed / 2000.0f;
            int alpha = (int) ((1.0f - progress) * 255);
            if (alpha < 5) alpha = 5;

            int y = this.height / 2 + 30 - (int) (progress * 30);
            context.drawCenteredTextWithShadow(this.textRenderer, err.text, this.width / 2, y, (alpha << 24) | 0xFF5555);
        }
    }

    private void renderRoulettes(DrawContext context) {
        int amount = AMOUNTS[amountIndex];
        int cols = (amount == 10) ? 2 : 1;
        int rows = (amount == 10) ? 5 : amount;
        int itemWidth = 40;
        int boxHeight = 40;
        int visibleItems = (amount == 10) ? 5 : 7;

        int boxWidth = itemWidth * visibleItems;
        int spacingX = 20;
        int spacingY = 5;
        int totalWidth = cols * boxWidth + (cols - 1) * spacingX;
        int totalHeight = rows * boxHeight + (rows - 1) * spacingY;
        int startX = (this.width - totalWidth) / 2;
        int startY = (this.height - totalHeight) / 2 + 10;

        for (int i = 0; i < amount; i++) {
            int col = (amount == 10) ? (i / rows) : 0;
            int row = i % rows;
            int x = startX + col * (boxWidth + spacingX);
            int y = startY + row * (boxHeight + spacingY);
            roulettes.get(i).render(context, x, y, boxWidth, boxHeight, itemWidth);
        }
    }

    private void renderWinScreen(DrawContext context) {
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Вы выиграли:"), this.width / 2, 50, 0xFF55FF55);
        int amount = AMOUNTS[amountIndex];
        int cols = amount >= 5 ? 5 : amount;
        int rows = (int) Math.ceil((double) amount / cols);
        int slotSize = 40;

        int spacingX = 60;
        int spacingY = 40;

        int totalW = cols * slotSize + (cols - 1) * spacingX;
        int totalH = rows * slotSize + (rows - 1) * spacingY;
        int sX = (this.width - totalW) / 2;
        int sY = (this.height - totalH) / 2 + 10;

        for (int i = 0; i < amount; i++) {
            int c = i % cols;
            int r = i / cols;
            int x = sX + c * (slotSize + spacingX);
            int y = sY + r * (slotSize + spacingY);

            Roulette roulette = roulettes.get(i);
            int rarityColor = roulette.wonEntry.rarity().getColor();

            ///  Background  and Frame Item
            context.fill(x, y, x + slotSize, y + slotSize, 0x88000000);
            context.drawBorder(x, y, slotSize, slotSize, rarityColor);

            /// Item
            context.getMatrices().push();
            context.getMatrices().translate(x + slotSize / 2f, y + slotSize / 2f, 0);
            context.getMatrices().scale(1.5F, 1.5F, 1.5F);
            context.drawItem(roulette.wonEntry.item(), -8, -8);
            context.getMatrices().pop();

            Text itemName = roulette.wonEntry.item().getName();

            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 200);

            context.drawCenteredTextWithShadow(this.textRenderer, itemName, x + slotSize / 2, y + slotSize + 6, rarityColor);
            context.getMatrices().pop();
        }
    }

    private void updateSpinLogic() {
        long currentTime = Util.getMeasuringTimeMs();
        if (currentState == State.SPINNING) {
            long elapsed = currentTime - stateStartTime;
            if (elapsed >= SPIN_DURATION) {
                for (Roulette r : roulettes) r.currentScroll = r.offsetTargetScroll;
                currentState = State.ALIGNING;
                stateStartTime = currentTime;
            } else {
                double t = (double) elapsed / SPIN_DURATION;
                double easeOut = 1.0 - Math.pow(1.0 - t, 3);
                for (Roulette r : roulettes) {
                    r.currentScroll = r.startScroll + (r.offsetTargetScroll - r.startScroll) * easeOut;
                }
                if (!roulettes.isEmpty()) roulettes.get(0).playTickSoundIfNeeded();
            }
        }
        else if (currentState == State.ALIGNING) {
            long elapsed = currentTime - stateStartTime;
            if (elapsed >= ALIGN_DURATION) {
                for (Roulette r : roulettes) {
                    r.currentScroll = r.exactCenterScroll;
                }
                currentState = State.FINISHED;
                updateButtonVisibility();
            } else {
                double t = (double) elapsed / ALIGN_DURATION;
                double easeInOut = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
                for (Roulette r : roulettes) {
                    r.currentScroll = r.offsetTargetScroll + (r.exactCenterScroll - r.offsetTargetScroll) * easeInOut;
                }
                if (!roulettes.isEmpty()) roulettes.get(0).playTickSoundIfNeeded();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return currentState == State.IDLE || currentState == State.FINISHED;
    }

    private class Roulette {
        List<CaseRegistry.LootEntry> ribbonEntries = new ArrayList<>();
        double currentScroll = 0, startScroll = 0;
        double exactCenterScroll, offsetTargetScroll;
        CaseRegistry.LootEntry wonEntry = null;
        int itemWidth;
        int lastTickIndex = -1;

        Roulette(int winningIndex, int boxWidth, int itemWidth, ItemStack serverWonItem) {
            this.itemWidth = itemWidth;
            for (int i = 0; i < 60; i++) ribbonEntries.add(getRandomEntryWithChance());

            CaseRegistry.LootEntry matchedEntry = pool.get(0);
            for (CaseRegistry.LootEntry entry : pool) {
                if (ItemStack.areEqual(entry.item(), serverWonItem)) {
                    matchedEntry = entry;
                    break;
                }
            }
            if (matchedEntry == pool.get(0) && !ItemStack.areEqual(pool.get(0).item(), serverWonItem)) {
                for (CaseRegistry.LootEntry entry : pool) {
                    if (entry.item().getItem() == serverWonItem.getItem()) {
                        matchedEntry = entry;
                        break;
                    }
                }
            }

            ribbonEntries.set(winningIndex, matchedEntry);
            this.wonEntry = matchedEntry;

            this.exactCenterScroll = (winningIndex * itemWidth) - ((double) boxWidth / 2) + ((double) itemWidth / 2);
            Random random = new Random();
            int randomOffset = random.nextInt(itemWidth - 10) - ((itemWidth - 10) / 2);
            this.offsetTargetScroll = this.exactCenterScroll + randomOffset;
        }

        void playTickSoundIfNeeded() {
            int currentIndex = (int) (currentScroll / itemWidth);
            if (lastTickIndex != -1 && currentIndex != lastTickIndex) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.2f, 1.5f + (float)Math.random() * 0.2f);
                }
            }
            lastTickIndex = currentIndex;
        }

        void render(DrawContext context, int boxX, int boxY, int boxWidth, int boxHeight, int itemWidth) {
            context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF222222);
            context.enableScissor(boxX, boxY, boxX + boxWidth, boxY + boxHeight);
            for (int i = 0; i < ribbonEntries.size(); i++) {
                int itemX = (int) (boxX - currentScroll + (i * itemWidth));
                if (itemX + itemWidth >= boxX && itemX <= boxX + boxWidth) {
                    context.drawBorder(itemX, boxY, itemWidth, boxHeight, 0xFF555555);
                    context.drawItem(ribbonEntries.get(i).item(), itemX + (itemWidth - 16) / 2, boxY + (boxHeight - 16) / 2);
                }
            }
            context.disableScissor();
            context.drawBorder(boxX - 1, boxY - 1, boxWidth + 2, boxHeight + 2, 0xFFFFFFFF);
            context.fill(boxX + boxWidth / 2 - 1, boxY - 5, boxX + boxWidth / 2 + 1, boxY + boxHeight + 5, 0xFFFF0000);
        }
    }

    private static class CaseError {
        String text;
        long startTime;
        CaseError(String text, long startTime) {
            this.text = text;
            this.startTime = startTime;
        }
    }
}