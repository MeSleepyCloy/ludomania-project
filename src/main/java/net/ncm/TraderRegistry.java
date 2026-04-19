package net.ncm;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.ncm.item.ModItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraderRegistry {
    public record TradeOffer(ItemStack requiredItem, long reward) {}
    public record TraderProfile(String id, Identifier texture, List<TradeOffer> offers, boolean isSlim) {}
    private static final Map<String, TraderProfile> TRADERS = new HashMap<>();

    public static long getItemSellPrice(Item item) {
        for (TraderProfile profile : TRADERS.values()) {
            for (TradeOffer offer : profile.offers()) {
                if (offer.requiredItem().isOf(item)) {
                    return offer.reward() / offer.requiredItem().getCount();
                }
            }
        }
        return 0;
    }

    static {
        TRADERS.put("trader1", new TraderProfile(
                "trader1",
                Identifier.of(Ludomania.MOD_ID, "textures/entity/trader1.png"),
                List.of(
                        new TradeOffer(new ItemStack(Items.IRON_INGOT, 5), 500),
                        new TradeOffer(new ItemStack(Items.DIAMOND, 1), 2000),
                        new TradeOffer(new ItemStack(ModItems.PEAR, 1), 150),
                        new TradeOffer(new ItemStack(ModItems.DALIT_AXE, 1), 2501),
                        new TradeOffer(new ItemStack(Items.STONE_AXE, 1), 240)
                ),
                false
        ));

        TRADERS.put("trader2", new TraderProfile(
                "trader2",
                Identifier.of(Ludomania.MOD_ID, "textures/entity/trader2.png"),
                List.of(
                        new TradeOffer(new ItemStack(Items.IRON_INGOT, 5), 500),
                        new TradeOffer(new ItemStack(Items.GOLD_INGOT, 2), 800),
                        new TradeOffer(new ItemStack(Items.DIAMOND, 1), 2000),
                        new TradeOffer(new ItemStack(Items.EMERALD, 10), 5000),
                        new TradeOffer(new ItemStack(Items.NETHERITE_HOE, 10), 5000),
                        new TradeOffer(new ItemStack(Items.BAKED_POTATO, 10), 5000),
                        new TradeOffer(new ItemStack(Items.GOLDEN_CARROT, 10), 5000)
                ),
                false
        ));

        TRADERS.put("trader3", new TraderProfile(
                "trader3",
                Identifier.of(Ludomania.MOD_ID, "textures/entity/trader3.png"),
                List.of(
                        new TradeOffer(new ItemStack(Items.IRON_INGOT, 5), 500),
                        new TradeOffer(new ItemStack(Items.GOLD_INGOT, 2), 800),
                        new TradeOffer(new ItemStack(Items.DIAMOND, 1), 2000),
                        new TradeOffer(new ItemStack(Items.EMERALD, 10), 5000),
                        new TradeOffer(new ItemStack(Items.NETHERITE_HOE, 10), 5000),
                        new TradeOffer(new ItemStack(Items.BAKED_POTATO, 10), 5000),
                        new TradeOffer(new ItemStack(Items.GOLDEN_CARROT, 10), 5000)
                ),
                false
        ));

        TRADERS.put("trader4", new TraderProfile(
                "trader4",
                Identifier.of(Ludomania.MOD_ID, "textures/entity/trader4.png"),
                List.of(
                        new TradeOffer(new ItemStack(Items.IRON_INGOT, 5), 500),
                        new TradeOffer(new ItemStack(Items.GOLD_INGOT, 2), 800),
                        new TradeOffer(new ItemStack(Items.DIAMOND, 1), 2000),
                        new TradeOffer(new ItemStack(Items.EMERALD, 10), 5000),
                        new TradeOffer(new ItemStack(Items.NETHERITE_HOE, 10), 5000),
                        new TradeOffer(new ItemStack(Items.BAKED_POTATO, 10), 5000),
                        new TradeOffer(new ItemStack(Items.GOLDEN_CARROT, 10), 5000)
                ),
                true
        ));

        TRADERS.put("trader5", new TraderProfile(
                "trader5",
                Identifier.of(Ludomania.MOD_ID, "textures/entity/trader5.png"),
                List.of(
                        new TradeOffer(new ItemStack(Items.IRON_INGOT, 5), 500),
                        new TradeOffer(new ItemStack(Items.GOLD_INGOT, 2), 800),
                        new TradeOffer(new ItemStack(Items.DIAMOND, 1), 2000),
                        new TradeOffer(new ItemStack(Items.EMERALD, 10), 5000),
                        new TradeOffer(new ItemStack(Items.NETHERITE_HOE, 10), 5000),
                        new TradeOffer(new ItemStack(Items.BAKED_POTATO, 10), 5000),
                        new TradeOffer(new ItemStack(Items.GOLDEN_CARROT, 10), 5000)
                ),
                false
        ));
    }

    public static TraderProfile getTrader(String id) {
        return TRADERS.getOrDefault(id, TRADERS.get("trader1"));
    }
}