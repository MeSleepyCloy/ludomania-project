package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.ncm.network.MinesweeperActionPayload;

import java.util.Random;

public class MinesweeperScreen extends Screen {
    private enum State { IDLE, WAITING, PLAYING, GAME_OVER }
    private State currentState = State.IDLE;

    private TextFieldWidget betField;
    private BombListWidget bombListWidget;
    private ButtonWidget actionButton;

    private long betAmount = 0;
    private int bombsCount = 3; // По умолчанию 3 бомбы
    private int openedCells = 0;
    private boolean won = false;

    private final boolean[] isBomb = new boolean[25];
    private final boolean[] isOpened = new boolean[25];

    private final ItemStack BOMB_ICON = new ItemStack(Items.TNT);
    private final ItemStack SAFE_ICON = new ItemStack(Items.EMERALD);

    private String errorMessage = "";
    private long errorTime = 0;

    public MinesweeperScreen() {
        super(Text.literal("Сапёр"));
    }

    private void showError(String msg) {
        this.errorMessage = msg;
        this.errorTime = Util.getMeasuringTimeMs();
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }

    @Override
    protected void init() {
        super.init();
        int leftPanelX = this.width / 2 - 160;
        int startY = this.height / 2 - 80;

        // Поле ставки
        this.betField = new TextFieldWidget(this.textRenderer, leftPanelX, startY, 120, 20, Text.literal("Ставка"));
        this.betField.setTextPredicate(text -> text.matches("^[0-9]*$"));
        this.addDrawableChild(betField);

        // Список бомб
        this.bombListWidget = new BombListWidget(this.client, 120, 100, startY + 35, 20);
        for (int i = 1; i <= 24; i++) {
            BombEntry entry = new BombEntry(i);
            this.bombListWidget.addBombEntry(entry);
            if (i == 3) this.bombListWidget.setSelected(entry);
        }
        this.addDrawableChild(bombListWidget);

        // Кнопка действия (Начать / Забрать)
        this.actionButton = ButtonWidget.builder(Text.literal("Начать"), button -> {
            if (currentState == State.IDLE || currentState == State.GAME_OVER) {
                try {
                    betAmount = Long.parseLong(betField.getText());
                    if (betAmount <= 0) { showError("Ставка должна быть > 0"); return; }
                    if (betAmount > MoneyHud.getActualBalance()) { showError("Недостаточно средств!"); return; }

                    BombEntry selected = bombListWidget.getSelectedOrNull();
                    bombsCount = selected != null ? selected.bombs : 3;

                    currentState = State.WAITING;
                    updateUI();
                    ClientPlayNetworking.send(new MinesweeperActionPayload(0, betAmount)); // 0 = Старт
                } catch (NumberFormatException e) {
                    showError("Введите ставку!");
                }
            } else if (currentState == State.PLAYING) {
                if (openedCells > 0) {
                    cashout();
                }
            }
        }).dimensions(leftPanelX, startY + 139, 120, 20).build();
        this.addDrawableChild(actionButton);
        updateUI();
    }

    private void updateUI() {
        betField.setEditable(currentState == State.IDLE || currentState == State.GAME_OVER);
        bombListWidget.active = (currentState == State.IDLE || currentState == State.GAME_OVER);

        if (currentState == State.IDLE) {
            actionButton.setMessage(Text.literal("Начать"));
            actionButton.active = true;
        } else if (currentState == State.WAITING) {
            actionButton.setMessage(Text.literal("Ожидание..."));
            actionButton.active = false;
        } else if (currentState == State.PLAYING) {
            long win = (long) (betAmount * getMultiplier(openedCells));
            actionButton.setMessage(Text.literal("Забрать (" + win + ")"));
            actionButton.active = (openedCells > 0);
        } else if (currentState == State.GAME_OVER) {
            actionButton.setMessage(Text.literal("Играть снова"));
            actionButton.active = true;
        }
    }

    // Вызывается из LudomaniaClient, когда сервер списал ставку
    public void onStartResponse(boolean success) {
        if (!success) {
            currentState = State.IDLE;
            showError("Ошибка ставки!");
            updateUI();
            return;
        }

        currentState = State.PLAYING;
        openedCells = 0;
        won = false;
        for (int i = 0; i < 25; i++) {
            isBomb[i] = false;
            isOpened[i] = false;
        }

        Random r = new Random();
        int placed = 0;
        while (placed < bombsCount) {
            int idx = r.nextInt(25);
            if (!isBomb[idx]) {
                isBomb[idx] = true;
                placed++;
            }
        }
        updateUI();
    }

    // Математика множителя
    private double getMultiplier(int opened) {
        if (opened == 0) return 1.0;
        double m = 1.0;
        for (int i = 0; i < opened; i++) {
            m *= (25.0 - i) / (25.0 - i - bombsCount);
        }
        return Math.floor(m * 100) / 100.0; // Округляем до 2 знаков
    }

    private void cashout() {
        long win = (long) (betAmount * getMultiplier(openedCells));
        ClientPlayNetworking.send(new MinesweeperActionPayload(1, win)); // 1 = Забрать
        currentState = State.GAME_OVER;
        won = true;
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        updateUI();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentState == State.PLAYING && button == 0) {
            int gridX = this.width / 2 + 10;
            int gridY = this.height / 2 - 80;
            int cellSize = 30;
            int spacing = 4;

            for (int i = 0; i < 25; i++) {
                int col = i % 5;
                int row = i / 5;
                int x = gridX + col * (cellSize + spacing);
                int y = gridY + row * (cellSize + spacing);

                if (mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize) {
                    if (!isOpened[i]) {
                        isOpened[i] = true;
                        if (isBomb[i]) {
                            // ВЗРЫВ
                            currentState = State.GAME_OVER;
                            won = false;
                            if (this.client != null && this.client.player != null) {
                                this.client.player.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 1.0f, 1.0f);
                            }
                        } else {
                            // БЕЗОПАСНО
                            openedCells++;
                            if (this.client != null && this.client.player != null) {
                                this.client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1.0f, 1.5f + (openedCells * 0.05f));
                            }
                            // Если открыты все безопасные
                            if (openedCells == 25 - bombsCount) {
                                cashout();
                            }
                        }
                        updateUI();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, "Сапёр", this.width / 2, 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Ваш баланс: " + MoneyHud.getActualBalance(), this.width / 2, 25, 0xFFFFAA00);

        int leftPanelX = this.width / 2 - 160;
        int startY = this.height / 2 - 80;
        context.drawTextWithShadow(this.textRenderer, "Количество бомб:", leftPanelX, startY + 23, 0xAAAAAA);

        int gridX = this.width / 2 + 10;
        int gridY = this.height / 2 - 80;
        int cellSize = 30;
        int spacing = 4;

        for (int i = 0; i < 25; i++) {
            int col = i % 5;
            int row = i / 5;
            int x = gridX + col * (cellSize + spacing);
            int y = gridY + row * (cellSize + spacing);

            if (currentState == State.IDLE || currentState == State.WAITING) {
                context.fill(x, y, x + cellSize, y + cellSize, 0xFF444444);
                context.drawBorder(x, y, cellSize, cellSize, 0xFF777777);
            } else if (currentState == State.PLAYING) {
                if (isOpened[i]) {
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF228822); // Зеленый
                    context.drawItem(SAFE_ICON, x + 7, y + 7);
                } else {
                    boolean hovered = mouseX >= x && mouseX <= x + cellSize && mouseY >= y && mouseY <= y + cellSize;
                    context.fill(x, y, x + cellSize, y + cellSize, hovered ? 0xFF666666 : 0xFF444444);
                    context.drawBorder(x, y, cellSize, cellSize, 0xFF777777);
                }
            } else if (currentState == State.GAME_OVER) {
                if (isBomb[i]) {
                    context.fill(x, y, x + cellSize, y + cellSize, isOpened[i] ? 0xFFAA2222 : 0xFF882222); // Красный
                    context.drawItem(BOMB_ICON, x + 7, y + 7);
                } else if (isOpened[i]) {
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF228822);
                    context.drawItem(SAFE_ICON, x + 7, y + 7);
                } else {
                    context.fill(x, y, x + cellSize, y + cellSize, 0xFF333333);
                }
            }
        }

        if (currentState == State.PLAYING) {
            String text = "Множитель: x" + getMultiplier(openedCells);
            context.drawCenteredTextWithShadow(this.textRenderer, text, gridX + 80, gridY + 180, 0x55FF55);
        } else if (currentState == State.GAME_OVER) {
            String text = won ? "Вы выиграли " + (long)(betAmount * getMultiplier(openedCells)) + "!" : "Вы проиграли!";
            int color = won ? 0x55FF55 : 0xFF5555;
            context.drawCenteredTextWithShadow(this.textRenderer, text, gridX + 80, gridY + 180, color);
        }

        if (!errorMessage.isEmpty() && Util.getMeasuringTimeMs() - errorTime < 2000) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage, leftPanelX + 60, startY + 170, 0xFF5555);
        }
    }

    @Override
    public void close() {
        if (currentState == State.PLAYING && openedCells > 0) {
            cashout();
        } else if (currentState == State.PLAYING && openedCells == 0) {
            ClientPlayNetworking.send(new MinesweeperActionPayload(1, betAmount));
        }
        super.close();
    }

    private class BombListWidget extends AlwaysSelectedEntryListWidget<BombEntry> {
        public BombListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
            this.setX(MinesweeperScreen.this.width / 2 - 160);
        }
        public void addBombEntry(BombEntry entry) { super.addEntry(entry); }
        @Override public int getRowWidth() { return this.width - 20; }
        @Override protected int getScrollbarX() { return this.getX() + this.width - 6; }
    }

    private class BombEntry extends AlwaysSelectedEntryListWidget.Entry<BombEntry> {
        final int bombs;
        public BombEntry(int bombs) { this.bombs = bombs; }
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String text = bombs + (bombs == 1 ? " бомба" : (bombs >= 2 && bombs <= 4 ? " бомбы" : " бомб"));
            context.drawTextWithShadow(MinesweeperScreen.this.textRenderer, text, x + 5, y + 5, 0xFFFFFF);
        }
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MinesweeperScreen.this.bombListWidget.setSelected(this);
            return true;
        }
        @Override public Text getNarration() { return Text.literal(bombs + " бомб"); }
    }
}