package net.ncm.creativeTab;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;


public class ModTab {
    public static final ItemGroup LUDOMANIA_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Ludomania.MOD_ID, "ludomania_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Ludomania.CASE_1_ITEM))
                    .displayName(Text.translatable("itemgroup.ludomania.Ludomania_items"))
                    .entries((displayContext, entries) -> {

                        entries.add(Ludomania.ATM_BLOCK);
                        entries.add(Ludomania.MINESWEEPER_BLOCK);
                        entries.add(Ludomania.PEDESTAL_BLOCK);
                        entries.add(Ludomania.CASE_1_ITEM);
                        entries.add(Ludomania.CASE_2_ITEM);
                        entries.add(Ludomania.CASE_3_ITEM);
                        entries.add(Ludomania.CASE_4_ITEM);
                        entries.add(Ludomania.CASE_5_ITEM);
                        entries.add(Ludomania.CASE_6_ITEM);
                        entries.add(Ludomania.CASE_7_ITEM);
                        entries.add(Ludomania.CASE_8_ITEM);
                    }).build());

    public static void registerItemGroups() {
    }
}