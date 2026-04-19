package net.ncm.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.ncm.TraderRegistry;
import net.ncm.Ludomania;
import net.ncm.screen.TraderScreenHandler;
import net.ncm.entity.TraderEntity;
import net.ncm.network.SellItemPayload;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class TraderScreen extends HandledScreen<TraderScreenHandler> {
    private static final Identifier BG_TEX = Identifier.of(Ludomania.MOD_ID, "textures/gui/trader_gui.png");
    private static final Identifier BTN_TEX = Identifier.of(Ludomania.MOD_ID, "textures/gui/button.png");
    private static final Identifier BTN_A_TEX = Identifier.of(Ludomania.MOD_ID, "textures/gui/button_a.png");

    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller_disabled");

    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);

    private TraderEntity renderEntity;
    private final TraderRegistry.TraderProfile profile;

    private int selectedOffer = -1;
    private int scrollOffset = 0;
    private boolean scrolling = false;

    public TraderScreen(TraderScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.profile = TraderRegistry.getTrader(handler.traderId);

        this.backgroundWidth = 176;
        this.backgroundHeight = 222;

        this.titleY = 6;
        this.playerInventoryTitleY = this.backgroundHeight - 92;

        if (MinecraftClient.getInstance().world != null) {
            this.renderEntity = Ludomania.TRADER_ENTITY.create(MinecraftClient.getInstance().world, SpawnReason.COMMAND);
            if (this.renderEntity != null) {
                this.renderEntity.setTraderId(profile.id());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int listX = this.x + 8;
        int listY = this.y + 18;
        List<TraderRegistry.TradeOffer> offers = profile.offers();
        for (int i = 0; i < 5; i++) {
            int index = i + scrollOffset;
            if (index >= offers.size()) break;
            int rowY = listY + (i * 22);
            if (mouseX >= listX + 2 && mouseX <= listX + 20 && mouseY >= rowY + 3 && mouseY <= rowY + 21) {
                context.drawItemTooltip(this.textRenderer, offers.get(index).requiredItem(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderLayer::getGuiTextured, BG_TEX, this.x, this.y, 0, 0, 210, this.backgroundHeight, 256, 256);

        if (this.renderEntity != null) {
            int entityX = this.x + 164;
            int entityY = this.y + 160;
            int size = 65;

            float mouseXOffset = (float)entityX - mouseX;
            float mouseYOffset = (float)(entityY - 70) - mouseY;

            Quaternionf quaternionf = new Quaternionf().rotationZ((float) Math.PI);
            Quaternionf quaternionf2 = new Quaternionf().rotationX((float)Math.atan(mouseYOffset / 40.0F) * 20.0F * 0.017453292F);
            quaternionf.mul(quaternionf2);

            this.renderEntity.bodyYaw = 180.0F + (float)Math.atan(mouseXOffset / 40.0F) * 20.0F;
            this.renderEntity.setYaw(180.0F + (float)Math.atan(mouseXOffset / 40.0F) * 40.0F);
            this.renderEntity.setPitch(-(float)Math.atan(mouseYOffset / 40.0F) * 20.0F);
            this.renderEntity.headYaw = this.renderEntity.getYaw();
            this.renderEntity.prevHeadYaw = this.renderEntity.getYaw();

            int boxLeft = this.x + 125;
            int boxTop = this.y + 10;
            int boxRight = this.x + 200;
            int boxBottom = this.y + 115;

            context.enableScissor(boxLeft, boxTop, boxRight, boxBottom);

            InventoryScreen.drawEntity(
                    context, entityX, entityY, size, new Vector3f(0, 0, 0), quaternionf, quaternionf2, this.renderEntity
            );

            context.disableScissor();
        }

        List<TraderRegistry.TradeOffer> offers = profile.offers();
        int listX = this.x + 8;
        int listY = this.y + 18;

        for (int i = 0; i < 5; i++) {
            int index = i + scrollOffset;
            if (index >= offers.size()) break;

            TraderRegistry.TradeOffer offer = offers.get(index);
            int rowY = listY + (i * 22);
            boolean isHovered = mouseX >= listX && mouseX <= listX + 72 && mouseY >= rowY && mouseY <= rowY + 22;

            if (index == selectedOffer) {
                context.fill(listX, rowY, listX + 72, rowY + 22, 0x5555FF55);
                context.drawBorder(listX, rowY, 72, 22, 0xFF55FF55);
            } else if (isHovered) {
                context.fill(listX, rowY, listX + 72, rowY + 22, 0x33FFFFFF);
            }

            context.drawItem(offer.requiredItem(), listX + 2, rowY + 3);
            context.drawStackOverlay(this.textRenderer, offer.requiredItem(), listX + 2, rowY + 3);

            String formattedReward = MoneyHud.formatMoney((long) offer.reward());
            Text rewardText = Text.literal("→ ").formatted(Formatting.GRAY).append(Text.literal(formattedReward).formatted(Formatting.GREEN));

            context.drawTextWithShadow(this.textRenderer, rewardText, listX + 24, rowY + 7, 0xFFFFFF);

            int emeraldX = listX + 54;
            context.drawItem(EMERALD, emeraldX, rowY + 3);
        }

        int maxOffset = Math.max(0, offers.size() - 5);
        int scrollerX = this.x + 81;
        int scrollerY = listY;

        if (maxOffset > 0) {
            scrollerY += (int) ((110 - 15) * ((float) scrollOffset / maxOffset));
            context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_TEXTURE, scrollerX, scrollerY, 8, 15);
        } else {
            context.drawGuiTexture(RenderLayer::getGuiTextured, SCROLLER_DISABLED_TEXTURE, scrollerX, scrollerY, 8, 15);
        }

        int btnX = this.x + 138;
        int btnY = this.y + 105;
        boolean btnHovered = mouseX >= btnX && mouseX <= btnX + 52 && mouseY >= btnY && mouseY <= btnY + 22;
        Identifier currentBtnTex = btnHovered ? BTN_A_TEX : BTN_TEX;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 250.0f);

        context.drawTexture(RenderLayer::getGuiTextured, currentBtnTex, btnX, btnY, 0, 0, 52, 24, 52, 24);
        context.drawCenteredTextWithShadow(this.textRenderer, "Продать", btnX + 26, btnY + 8, 0xFFFFFF);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scrollerX = this.x + 81;
        if (button == 0 && mouseX >= scrollerX && mouseX < scrollerX + 12 && mouseY >= this.y + 18 && mouseY < this.y + 128) {
            this.scrolling = true;
            return true;
        }

        int listX = this.x + 8;
        int listY = this.y + 18;
        for (int i = 0; i < 5; i++) {
            int index = i + scrollOffset;
            if (index >= profile.offers().size()) break;
            int rowY = listY + (i * 22);

            if (mouseX >= listX && mouseX <= listX + 72 && mouseY >= rowY && mouseY <= rowY + 22) {
                selectedOffer = index;
                if (this.client != null && this.client.player != null) {
                    this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                }
                return true;
            }
        }

        int btnX = this.x + 138;
        int btnY = this.y + 105;
        if (selectedOffer != -1 && mouseX >= btnX && mouseX <= btnX + 52 && mouseY >= btnY && mouseY <= btnY + 21) {
            ClientPlayNetworking.send(new SellItemPayload(profile.id(), selectedOffer));
            if (this.client != null && this.client.player != null) {
                this.client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int maxOffset = Math.max(0, profile.offers().size() - 5);
            if (maxOffset > 0) {
                int trackHeight = 110 - 15;
                float scrollRatio = ((float)mouseY - (this.y + 18) - 7.5f) / trackHeight;
                scrollRatio = MathHelper.clamp(scrollRatio, 0.0f, 1.0f);
                this.scrollOffset = (int)(scrollRatio * maxOffset + 0.5f);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0 && scrollOffset > 0) {
            scrollOffset--; return true;
        }
        if (verticalAmount < 0 && scrollOffset < profile.offers().size() - 5) {
            scrollOffset++; return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}