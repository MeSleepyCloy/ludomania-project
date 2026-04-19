package net.ncm;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.ncm.item.ModItems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.literal;


public class CaseRegistry {

    public enum Rarity {
        COMMON(0xFFCCCCCC),    // Светло-серый (добавлено FF для полной непрозрачности)
        RARE(0xFF55FF55),      // Редкая - Зеленый
        EPIC(0xFFAA00AA),      // Эпическая - Фиолетовый
        MYTHIC(0xFFFF5555),    // Мифический - Красная
        LEGENDARY(0xFFFFFF55); // Легендарная - Жёлтая

        private final int color;
        Rarity(int color) { this.color = color; }
        public int getColor() { return color; }
    }

    public record LootEntry(ItemStack item, int weight, Rarity rarity) {
        public LootEntry {
            item = item.copy();

            // 1. Красим название в цвет редкости
           Text currentName = item.contains(DataComponentTypes.CUSTOM_NAME)
                    ? item.get(DataComponentTypes.CUSTOM_NAME)
                    : Text.translatable(item.getItem().getTranslationKey());

           Text coloredName = currentName.copy()
                    .setStyle(net.minecraft.text.Style.EMPTY.withColor(rarity.getColor()).withItalic(false));

            item.set(DataComponentTypes.CUSTOM_NAME, coloredName);

            long sellPrice = TraderRegistry.getItemSellPrice(item.getItem());
            if (sellPrice > 0) {
                List<net.minecraft.text.Text> lore = new ArrayList<>();

                LoreComponent existingLore = item.get(net.minecraft.component.DataComponentTypes.LORE);
                if (existingLore != null) {
                    lore.addAll(existingLore.lines());
                }

                lore.add(empty());
                lore.add(literal("Цена при продаже: " + sellPrice + " Изумрудов").formatted(Formatting.GOLD));

                item.set(DataComponentTypes.LORE, new LoreComponent(lore));
            }
        }
    }

    public record CaseData(long price, List<LootEntry> pool) {}

    private static final Map<String, CaseData> CASES = new HashMap<>();

    public static ItemStack createCaseItem(Item item, int count, List<String> loreLines, Block... canBreakBlocks) {
        ItemStack stack = new ItemStack(item, count);

        List<Text> textLore = new ArrayList<>();
        for (String line : loreLines) {
            textLore.add(literal(line).formatted(Formatting.GRAY));
        }

        if (!textLore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(textLore));
        }

        if (canBreakBlocks.length > 0) {
            net.minecraft.predicate.BlockPredicate predicate = net.minecraft.predicate.BlockPredicate.Builder.create()
                    .blocks(net.minecraft.registry.Registries.BLOCK, canBreakBlocks)
                    .build();
            stack.set(DataComponentTypes.CAN_BREAK, new net.minecraft.item.BlockPredicatesChecker(List.of(predicate)));
        }
        return stack;
    }

    static {
        CASES.put("basic", new CaseData(500, List.of(
                new LootEntry(new ItemStack(Items.DIRT), 60, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.IRON_INGOT), 30, Rarity.RARE),
                new LootEntry(new ItemStack(Items.DIAMOND), 10, Rarity.EPIC)
        )));
        CASES.put("case2", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.APPLE), 25, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.CARROT), 15, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.BREAD), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.MELON_SLICE), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BAKED_POTATO), 11, Rarity.RARE),
                new LootEntry(new ItemStack(Items.COOKED_CHICKEN), 8, Rarity.EPIC),
                new LootEntry(new ItemStack(ModItems.PEAR), 9, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.GOLDEN_CARROT), 4, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.GOLDEN_APPLE), 3, Rarity.LEGENDARY),
                new LootEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, Rarity.LEGENDARY)
        )));

        CASES.put("case3", new CaseData(500, List.of(
                new LootEntry(new ItemStack(Items.WOODEN_AXE), 40, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.STONE_AXE), 30, Rarity.COMMON),
                new LootEntry(createCaseItem(
                        ModItems.DALIT_AXE,
                        1,
                        List.of("Легендарный топор дровосека", "Идеален для добычи дуба!"),
                        Blocks.OAK_LOG, Blocks.OAK_PLANKS), 10, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.DIAMOND_AXE), 5, Rarity.MYTHIC)
        )));
        CASES.put("case5", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.APPLE), 25, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.CARROT), 15, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.BREAD), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.MELON_SLICE), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BAKED_POTATO), 11, Rarity.RARE),
                new LootEntry(new ItemStack(Items.COOKED_CHICKEN), 8, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.COOKIE), 9, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.GOLDEN_CARROT), 4, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.GOLDEN_APPLE), 3, Rarity.LEGENDARY),
                new LootEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, Rarity.LEGENDARY)
        )));
        CASES.put("case6", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.APPLE), 25, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.CARROT), 15, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.BREAD), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.MELON_SLICE), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BAKED_POTATO), 11, Rarity.RARE),
                new LootEntry(new ItemStack(Items.COOKED_CHICKEN), 8, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.COOKIE), 9, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.GOLDEN_CARROT), 4, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.GOLDEN_APPLE), 3, Rarity.LEGENDARY),
                new LootEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, Rarity.LEGENDARY)
        )));
        CASES.put("case7", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.APPLE), 25, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.CARROT), 15, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.BREAD), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.MELON_SLICE), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BAKED_POTATO), 11, Rarity.RARE),
                new LootEntry(new ItemStack(Items.COOKED_CHICKEN), 8, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.COOKIE), 9, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.GOLDEN_CARROT), 4, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.GOLDEN_APPLE), 3, Rarity.LEGENDARY),
                new LootEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, Rarity.LEGENDARY)
        )));
        CASES.put("case8", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.APPLE), 25, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.CARROT), 15, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.BREAD), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.MELON_SLICE), 15, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BAKED_POTATO), 11, Rarity.RARE),
                new LootEntry(new ItemStack(Items.COOKED_CHICKEN), 8, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.COOKIE), 9, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.GOLDEN_CARROT), 4, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.GOLDEN_APPLE), 3, Rarity.LEGENDARY),
                new LootEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), 2, Rarity.LEGENDARY)
        )));
    }

    public static CaseData getCase(String caseId) {
        return CASES.getOrDefault(caseId, CASES.get("basic"));
    }
}