package net.ncm;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ncm.block.AtmBlock;
import net.ncm.block.MinesweeperBlock;
import net.ncm.block.ModBlocks;
import net.ncm.block.PedestalBlock;
import net.ncm.block.entity.PedestalBlockEntity;
import net.ncm.command.AtmCommand;
import net.ncm.command.MoneyCommand;
import net.ncm.command.OpenCaseCommand;
import net.ncm.creativeTab.ModTab;
import net.ncm.entity.TraderEntity;
import net.ncm.item.CaseItem;
import net.ncm.item.ModItems;
import net.ncm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.ncm.command.QuestCommand;
import net.ncm.network.SyncQuestPayload;
import net.ncm.command.MinesweeperCommand;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.screen.ScreenHandlerType;
import net.ncm.screen.TraderScreenHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ludomania implements ModInitializer {
    public static final String MOD_ID = "ludomania";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static final ScreenHandlerType<TraderScreenHandler> TRADER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "trader"),
                    new ExtendedScreenHandlerType<>(
                            (syncId, inv, payload) -> new TraderScreenHandler(syncId, inv, payload.traderId()),
                            net.ncm.network.OpenTraderPayload.CODEC
                    )
            );

    public static final RegistryKey<EntityType<?>> TRADER_ENTITY_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "trader"));

    public static final EntityType<TraderEntity> TRADER_ENTITY = Registry.register(
           Registries.ENTITY_TYPE,
            TRADER_ENTITY_KEY,
            EntityType.Builder.create(TraderEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6F, 1.95F)
                    .makeFireImmune() // Не горит
                    .trackingTickInterval(3) // Как часто обновлять на клиенте
                    .maxTrackingRange(10) // Дальность прорисовки (чанков)
                    .build(TRADER_ENTITY_KEY)
    );
    /// кейсы

    public static final RegistryKey<Block> MINESWEEPER_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "minesweeper"));
    public static final RegistryKey<Item> MINESWEEPER_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "minesweeper"));
    public static final Block MINESWEEPER_BLOCK = new MinesweeperBlock(AbstractBlock.Settings.create().registryKey(MINESWEEPER_BLOCK_KEY).strength(2.0f).nonOpaque());
    public static final Item MINESWEEPER_ITEM = new BlockItem(MINESWEEPER_BLOCK, new Item.Settings().registryKey(MINESWEEPER_ITEM_KEY));

    public static final RegistryKey<Item> CASE_BASIC_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case_basic"));
    public static final Item CASE_BASIC_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_BASIC_KEY), "basic");

    ///  блоки
    public static final RegistryKey<Item> CASE_1_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case1"));
    public static final Item CASE_1_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_1_KEY), "case1");

    /// еда
    public static final RegistryKey<Item> CASE_2_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case2"));
    public static final Item CASE_2_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_2_KEY), "case2");

    /// топоры
    public static final RegistryKey<Item> CASE_3_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case3"));
    public static final Item CASE_3_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_3_KEY), "case3");

    /// драгоценности
    public static final RegistryKey<Item> CASE_4_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case4"));
    public static final Item CASE_4_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_4_KEY), "case4");

    /// мечи
    public static final RegistryKey<Item> CASE_5_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case5"));
    public static final Item CASE_5_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_5_KEY), "case5");

    /// зелья
    public static final RegistryKey<Item> CASE_6_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case6"));
    public static final Item CASE_6_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_6_KEY), "case6");

    ///  артефакты
    public static final RegistryKey<Item> CASE_7_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case7"));
    public static final Item CASE_7_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_7_KEY), "case7");

    /// фигурки
    public static final RegistryKey<Item> CASE_8_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "case8"));
    public static final Item CASE_8_ITEM = new CaseItem(new Item.Settings().registryKey(CASE_8_KEY), "case8");

    public static final RegistryKey<Block> ATM_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "atm"));
    public static final RegistryKey<Item> ATM_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "atm"));

    public static final RegistryKey<Block> PEDESTAL_BLOCK_KEY = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "pedestal"));
    public static final RegistryKey<Item> PEDESTAL_ITEM_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "pedestal"));

    public static final Block PEDESTAL_BLOCK = new PedestalBlock(AbstractBlock.Settings.create().registryKey(PEDESTAL_BLOCK_KEY).strength(-1.0F, 3600000.0F).nonOpaque().noBlockBreakParticles());
    public static final Item PEDESTAL_ITEM = new BlockItem(PEDESTAL_BLOCK, new Item.Settings().registryKey(PEDESTAL_ITEM_KEY));

    public static BlockEntityType<PedestalBlockEntity> PEDESTAL_BLOCK_ENTITY;

    public static final Block ATM_BLOCK = new AtmBlock(AbstractBlock.Settings.create().registryKey(ATM_BLOCK_KEY).strength(-1.0F, 3600000.0F).nonOpaque().noBlockBreakParticles());
    public static final Item ATM_ITEM = new BlockItem(ATM_BLOCK, new Item.Settings().registryKey(ATM_ITEM_KEY));

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Ludomania Mod...");

        ///  НПС ТРЕЙДЕР

        FabricDefaultAttributeRegistry.register(TRADER_ENTITY, TraderEntity.createTraderAttributes());

        // Пакеты
        PayloadTypeRegistry.playS2C().register(OpenTraderPayload.ID, OpenTraderPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SellItemPayload.ID, SellItemPayload.CODEC);

        // ОБРАБОТКА ПРОДАЖИ (Сервер)
        ServerPlayNetworking.registerGlobalReceiver(SellItemPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                TraderRegistry.TraderProfile profile = TraderRegistry.getTrader(payload.traderId());
                if (profile == null || payload.offerIndex() >= profile.offers().size()) return;

                TraderRegistry.TradeOffer offer = profile.offers().get(payload.offerIndex());
                ItemStack required = offer.requiredItem();

                int count = 0;
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack stack = player.getInventory().getStack(i);
                    if (ItemStack.areItemsEqual(stack, required)) {
                        count += stack.getCount();
                    }
                }

                if (count >= required.getCount()) {
                    int needed = required.getCount();
                    for (int i = 0; i < player.getInventory().size() && needed > 0; i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (ItemStack.areItemsEqual(stack, required)) {
                            int toTake = Math.min(needed, stack.getCount());
                            stack.decrement(toTake);
                            needed -= toTake;
                        }
                    }

                    MoneyState.addBalance(context.server(), player.getUuid(), offer.reward());
                    ServerPlayNetworking.send(player, new SyncMoneyPayload(MoneyState.getBalance(context.server(), player.getUuid())));
                    player.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);
                } else {
                    player.sendMessage(Text.literal("§cНе хватает предметов для продажи!"), true);
                }
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("spawn_trader")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "id");
                                TraderEntity trader = TRADER_ENTITY.create(
                                        ctx.getSource().getWorld(),
                                        net.minecraft.entity.SpawnReason.COMMAND
                                );
                                if (trader != null) {
                                    trader.refreshPositionAndAngles(ctx.getSource().getPosition().x, ctx.getSource().getPosition().y, ctx.getSource().getPosition().z, 0, 0);
                                    trader.setTraderId(id);
                                    ctx.getSource().getWorld().spawnEntity(trader);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Торговец заспавнен!"), false);
                                }
                                return 1;
                            })));
        });



        ModBlocks.registerModBlocks();
        ModTab.registerItemGroups();
        ModItems.registerModItems();

        Registry.register(Registries.BLOCK, PEDESTAL_BLOCK_KEY, PEDESTAL_BLOCK);
        Registry.register(Registries.ITEM, PEDESTAL_ITEM_KEY, PEDESTAL_ITEM);

        PEDESTAL_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(MOD_ID, "pedestal_be"),
                FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, PEDESTAL_BLOCK).build()
        );

        Registry.register(Registries.BLOCK, ATM_BLOCK_KEY, ATM_BLOCK);
        Registry.register(Registries.ITEM, ATM_ITEM_KEY, ATM_ITEM);

        Registry.register(Registries.BLOCK, MINESWEEPER_BLOCK_KEY, MINESWEEPER_BLOCK);
        Registry.register(Registries.ITEM, MINESWEEPER_ITEM_KEY, MINESWEEPER_ITEM);

        /// Кейсы
        Registry.register(Registries.ITEM, CASE_BASIC_KEY, CASE_BASIC_ITEM);
        Registry.register(Registries.ITEM, CASE_1_KEY, CASE_1_ITEM);
        Registry.register(Registries.ITEM, CASE_2_KEY, CASE_2_ITEM);
        Registry.register(Registries.ITEM, CASE_3_KEY, CASE_3_ITEM);
        Registry.register(Registries.ITEM, CASE_4_KEY, CASE_4_ITEM);
        Registry.register(Registries.ITEM, CASE_5_KEY, CASE_5_ITEM);
        Registry.register(Registries.ITEM, CASE_6_KEY, CASE_6_ITEM);
        Registry.register(Registries.ITEM, CASE_7_KEY, CASE_7_ITEM);
        Registry.register(Registries.ITEM, CASE_8_KEY, CASE_8_ITEM);

        PayloadTypeRegistry.playS2C().register(SyncQuestPayload.ID, SyncQuestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenCasePayload.ID, OpenCasePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncMoneyPayload.ID, SyncMoneyPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AtmPlayersPayload.ID, AtmPlayersPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpinResultPayload.ID, SpinResultPayload.CODEC);


        PayloadTypeRegistry.playC2S().register(MinesweeperActionPayload.ID, MinesweeperActionPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(OpenMinesweeperPayload.ID, OpenMinesweeperPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MinesweeperResponsePayload.ID, MinesweeperResponsePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(AtmTransferPayload.ID, AtmTransferPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PayForCasePayload.ID, PayForCasePayload.CODEC);


        /// 3.3 Сапёр (Ставка и Выигрыш)
        ServerPlayNetworking.registerGlobalReceiver(MinesweeperActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player == null) return;

                if (payload.action() == 0) { // Старт игры (списание ставки)
                    long bet = payload.amount();
                    if (bet > 0 && MoneyState.getBalance(context.server(), player.getUuid()) >= bet) {
                        MoneyState.addBalance(context.server(), player.getUuid(), -bet);
                        ServerPlayNetworking.send(player, new SyncMoneyPayload(MoneyState.getBalance(context.server(), player.getUuid())));
                        ServerPlayNetworking.send(player, new MinesweeperResponsePayload(true));
                    } else {
                        ServerPlayNetworking.send(player, new MinesweeperResponsePayload(false));
                    }
                } else if (payload.action() == 1) { // Забрать выигрыш
                    long win = payload.amount();
                    if (win > 0) {
                        MoneyState.addBalance(context.server(), player.getUuid(), win);
                        ServerPlayNetworking.send(player, new SyncMoneyPayload(MoneyState.getBalance(context.server(), player.getUuid())));
                    }
                }
            });
        });

        /// Обработка оплаты кейса
        ServerPlayNetworking.registerGlobalReceiver(PayForCasePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player == null) return;

                CaseRegistry.CaseData caseData = CaseRegistry.getCase(payload.caseId());
                long totalCost = caseData.price() * payload.amount();

                if (MoneyState.getBalance(context.server(), player.getUuid()) >= totalCost) {

                    MoneyState.addBalance(context.server(), player.getUuid(), -totalCost);
                    ServerPlayNetworking.send(player, new SyncMoneyPayload(MoneyState.getBalance(context.server(), player.getUuid())));

                    List<ItemStack> wonItems = new ArrayList<>();
                    for (int i = 0; i < payload.amount(); i++) {
                        int totalWeight = caseData.pool().stream().mapToInt(CaseRegistry.LootEntry::weight).sum();
                        int randomVal = new Random().nextInt(totalWeight);

                        ItemStack wonItem = caseData.pool().get(0).item().copy();
                        for (CaseRegistry.LootEntry entry : caseData.pool()) {
                            randomVal -= entry.weight();
                            if (randomVal < 0) {
                                wonItem = entry.item().copy();
                                break;
                            }
                        }
                        wonItems.add(wonItem);

                        player.getInventory().insertStack(wonItem.copy());
                    }

                    ServerPlayNetworking.send(player, new SpinResultPayload(wonItems));

                    /// ЛОГИКА КВЕСТОВ ПРИ ОТКРЫТИИ КЕЙСОВ
                    String currentQuest = QuestState.getActiveQuest(context.server(), player.getUuid());

                    if ("start".equals(currentQuest) && "basic".equals(payload.caseId())) {
                        QuestState.completeQuest(context.server(), player, "start");
                    }

                    if ("rich".equals(currentQuest) && "premium".equals(payload.caseId())) {
                        int progress = QuestState.getQuestProgress(context.server(), player.getUuid());
                        int newProgress = progress + payload.amount();

                        QuestRegistry.QuestData qData = QuestRegistry.getQuest("rich");
                        int required = (qData != null) ? qData.maxProgress() : 10;

                        if (newProgress >= required) {
                            QuestState.completeQuest(context.server(), player, "rich");
                        } else {
                            QuestState.setQuestProgress(context.server(), player, newProgress);
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(AtmTransferPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity sender = context.player();
                if (sender == null) return;

                ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.targetName());
                long amount = payload.amount();

                if (target == null) {
                    sender.sendMessage(Text.literal("§cИгрок не найден онлайн!"), false);
                    return;
                }
                if (amount <= 0) {
                    sender.sendMessage(Text.literal("§cСумма должна быть больше нуля!"), false);
                    return;
                }

                long senderBalance = MoneyState.getBalance(context.server(), sender.getUuid());
                if (senderBalance < amount) {
                    sender.sendMessage(Text.literal("§cНедостаточно средств!"), false);
                    return;
                }

                long fee = (long) (amount * 0.02);
                long amountToReceive = amount - fee;

                MoneyState.addBalance(context.server(), sender.getUuid(), -amount);
                MoneyState.addBalance(context.server(), target.getUuid(), amountToReceive);

                ServerPlayNetworking.send(sender, new SyncMoneyPayload(MoneyState.getBalance(context.server(), sender.getUuid())));
                ServerPlayNetworking.send(target, new SyncMoneyPayload(MoneyState.getBalance(context.server(), target.getUuid())));

                sender.sendMessage(Text.literal("§aПеревод успешен! Отправлено: " + amountToReceive + " (Комиссия 2%: " + fee + ")"), false);
                target.sendMessage(Text.literal("§aВам пришел перевод от " + sender.getName().getString() + " на сумму " + amountToReceive + " изумрудов!"), false);
            });
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            OpenCaseCommand.register(dispatcher);
            MoneyCommand.register(dispatcher);
            AtmCommand.register(dispatcher);
            QuestCommand.register(dispatcher);
            MinesweeperCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            long balance = MoneyState.getBalance(server, handler.player.getUuid());
            ServerPlayNetworking.send(handler.player, new SyncMoneyPayload(balance));

            String rawQuest = QuestState.getRawQuest(server, handler.player.getUuid());
            ServerPlayNetworking.send(handler.player, new SyncQuestPayload(rawQuest, true));
        });
    }
}