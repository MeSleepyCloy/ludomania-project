package net.ncm;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseRegistry {

    public enum Rarity {
        COMMON(0xE6E6E6),    /// Обычная - Серый (как ты просил)
        RARE(0xFF55FF55),      /// Редкая - Зеленый
        EPIC(0xFFAA00AA),      /// Эпическая - Фиолетовый
        MYTHIC(0xFFFF5555), /// Мифический - Красная
        LEGENDARY(0xFFFFFF55); /// Легендарная - Жёлтая


        private final int color;
        Rarity(int color) { this.color = color; }
        public int getColor() { return color; }
    }

    public record LootEntry(ItemStack item, int weight, Rarity rarity) {}

    public record CaseData(long price, List<LootEntry> pool) {}

    private static final Map<String, CaseData> CASES = new HashMap<>();

    static {
        CASES.put("basic", new CaseData(500, List.of(
                new LootEntry(new ItemStack(Items.DIRT), 60, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.IRON_INGOT), 30, Rarity.RARE),
                new LootEntry(new ItemStack(Items.DIAMOND), 10, Rarity.EPIC)
        )));

        CASES.put("premium", new CaseData(2500, List.of(
                new LootEntry(new ItemStack(Items.GOLD_INGOT), 50, Rarity.RARE),
                new LootEntry(new ItemStack(Items.DIAMOND), 40, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.NETHERITE_INGOT), 10, Rarity.LEGENDARY)
        )));

        CASES.put("case1", new CaseData(250, List.of(
                new LootEntry(new ItemStack(Items.DIRT), 23, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.SAND), 20, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.GRAVEL), 17, Rarity.COMMON),
                new LootEntry(new ItemStack(Items.GRASS_BLOCK), 13, Rarity.RARE),
                new LootEntry(new ItemStack(Items.RED_SAND), 13, Rarity.RARE),
                new LootEntry(new ItemStack(Items.OAK_PLANKS), 10, Rarity.RARE),
                new LootEntry(new ItemStack(Items.BLUE_ICE), 7, Rarity.EPIC),
                new LootEntry(new ItemStack(Items.SCULK), 5, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.OBSIDIAN), 3, Rarity.MYTHIC),
                new LootEntry(new ItemStack(Items.END_STONE), 2, Rarity.LEGENDARY)
        )));

        CASES.put("case2", new CaseData(250, List.of(
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
        CASES.put("case3", new CaseData(250, List.of(
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
        CASES.put("case4", new CaseData(250, List.of(
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
        )));CASES.put("case5", new CaseData(250, List.of(
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