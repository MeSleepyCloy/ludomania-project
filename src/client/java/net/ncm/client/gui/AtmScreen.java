package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.ncm.network.AtmTransferPayload;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AtmScreen extends Screen {
    private final List<String> players;

    private PlayerListWidget playerListWidget;
    private TextFieldWidget amountField;
    private ButtonWidget sendButton;

    private final long[] quickAmounts = {500, 1000, 5000, 10000};

    private final List<AtmNotification> notifications = new ArrayList<>();

    public AtmScreen(List<String> players) {
        super(Text.literal("Банкомат"));
        this.players = players;
    }

    private void showNotification(String text, boolean isError) {
        notifications.add(new AtmNotification(text, isError, Util.getMeasuringTimeMs()));
        if (this.client != null && this.client.player != null) {
            if (isError) {
                this.client.player.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            } else {
                this.client.player.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 180;
        int listWidth = 140;
        int totalWidth = listWidth + 20 + panelWidth;
        int startX = (this.width - totalWidth) / 2;
        int startY = 50;

        this.playerListWidget = new PlayerListWidget(this.client, listWidth, this.height - 100, startY, 20);
        for (String playerName : players) {
            this.playerListWidget.addPlayerEntry(new PlayerEntry(playerName));
        }
        this.addDrawableChild(playerListWidget);

        int rightPanelX = startX + listWidth + 20;

        this.amountField = new TextFieldWidget(this.textRenderer, rightPanelX, startY + 20, 110, 20, Text.literal("Сумма"));
        this.amountField.setMaxLength(15);
        this.amountField.setTextPredicate(text -> text.matches("^[0-9]*$"));
        this.addDrawableChild(amountField);

        this.sendButton = ButtonWidget.builder(Text.literal("Перевод"), button -> {
            PlayerEntry selected = playerListWidget.getSelectedOrNull();
            if (selected == null) {
                showNotification("Выберите игрока!", true);
                return;
            }
            if (amountField.getText().isEmpty()) {
                showNotification("Введите сумму!", true);
                return;
            }
            try {
                long amount = Long.parseLong(amountField.getText());
                if (amount <= 0) {
                    showNotification("Сумма должна быть > 0!", true);
                    return;
                }
                if (amount > MoneyHud.getActualBalance()) {
                    showNotification("Недостаточно средств!", true);
                    return;
                }

                ClientPlayNetworking.send(new AtmTransferPayload(selected.name, amount));
                this.close();

            } catch (NumberFormatException e) {
                showNotification("Слишком большое число!", true);
            }
        }).dimensions(rightPanelX + 115, startY + 20, 65, 20).build();
        this.addDrawableChild(sendButton);

        int btnWidth = 85;
        int btnHeight = 20;
        for (int i = 0; i < quickAmounts.length; i++) {
            long amount = quickAmounts[i];
            int col = i % 2;
            int row = i / 2;
            int btnX = rightPanelX + col * (btnWidth + 10);
            int btnY = startY + 60 + row * (btnHeight + 5);

            this.addDrawableChild(ButtonWidget.builder(Text.literal("+" + amount), button -> {
                long currentAmount = 0;
                if (!amountField.getText().isEmpty()) {
                    try {
                        currentAmount = Long.parseLong(amountField.getText());
                    } catch (NumberFormatException ignored) {}
                }
                amountField.setText(String.valueOf(currentAmount + amount));
            }).dimensions(btnX, btnY, btnWidth, btnHeight).build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Очистить"), button -> {
            amountField.setText("");
        }).dimensions(rightPanelX, startY + 60 + 2 * (btnHeight + 5), panelWidth, btnHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Ваш баланс: " + MoneyHud.getActualBalance()), this.width / 2, 30, 0xFFFFAA00);

        int listWidth = 140;
        int rightPanelX = (this.width - (listWidth + 20 + 180)) / 2 + listWidth + 20;
        int startY = 50;

        context.drawTextWithShadow(this.textRenderer, "Сумма перевода:", rightPanelX, startY + 8, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Быстрый ввод:", rightPanelX, startY + 48, 0xAAAAAA);

        if (!amountField.getText().isEmpty()) {
            try {
                long amount = Long.parseLong(amountField.getText());
                long fee = (long) (amount * 0.02);
                long receive = amount - fee;

                int color = amount > MoneyHud.getActualBalance() ? 0xFF5555 : 0x55FF55;
                context.drawTextWithShadow(this.textRenderer, "К получению: " + receive, rightPanelX, startY + 140, color);
                context.drawTextWithShadow(this.textRenderer, "Комиссия 2%: " + fee, rightPanelX, startY + 152, 0xAAAAAA);
            } catch (NumberFormatException ignored) {}
        }

        renderNotifications(context);
    }

    private void renderNotifications(DrawContext context) {
        long now = Util.getMeasuringTimeMs();
        int notificationHeight = 30;
        int spacing = 5;

        int currentY = this.height - 20 - notificationHeight;

        Iterator<AtmNotification> it = notifications.iterator();
        while (it.hasNext()) {
            AtmNotification notif = it.next();
            long elapsed = now - notif.startTime;
            if (elapsed > 3000) {
                it.remove();
                continue;
            }

            int textWidth = this.textRenderer.getWidth(notif.text);
            int boxWidth = textWidth + 30;

            double offsetX = 0;
            if (elapsed < 300) {
                double t = elapsed / 300.0;
                offsetX = boxWidth * (1.0 - Math.pow(t, 3));
            } else if (elapsed > 2700) {
                double t = (elapsed - 2700) / 300.0;
                offsetX = boxWidth * Math.pow(t, 3);
            }

            int boxX = this.width - boxWidth - 10 + (int) offsetX;

            context.fill(boxX, currentY, boxX + boxWidth, currentY + notificationHeight, 0xDD222222);

            int stripeColor = notif.isError ? 0xFFFF5555 : 0xFF55FF55;
            context.fill(boxX, currentY, boxX + 4, currentY + notificationHeight, stripeColor);

            context.drawBorder(boxX, currentY, boxWidth, notificationHeight, 0xFF555555);

            context.drawTextWithShadow(this.textRenderer, notif.text, boxX + 15, currentY + (notificationHeight - 8) / 2, 0xFFFFFF);

            currentY -= (notificationHeight + spacing);
        }
    }

    private class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerEntry> {
        public PlayerListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
            this.setX((AtmScreen.this.width - (140 + 20 + 180)) / 2);
        }

        public void addPlayerEntry(PlayerEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }

        protected int getScrollbarPositionX() {
            return this.getX() + this.width - 6;
        }
    }

    private class PlayerEntry extends AlwaysSelectedEntryListWidget.Entry<PlayerEntry> {
        private final String name;

        public PlayerEntry(String name) {
            this.name = name;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawTextWithShadow(AtmScreen.this.textRenderer, this.name, x + 5, y + 5, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            AtmScreen.this.playerListWidget.setSelected(this);
            return true;
        }

        @Override
        public Text getNarration() {
            return Text.literal(this.name);
        }
    }

    private static class AtmNotification {
        String              text;
        boolean isError;
        long startTime;

        AtmNotification(String text, boolean isError, long startTime) {
            this.text = text;
            this.isError = isError;
            this.startTime = startTime;
        }
    }
}