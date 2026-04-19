package net.ncm.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;

public class ModItems {
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Ludomania.MOD_ID, name), item);
    }

    public static void registerModItems() {
    }
}
