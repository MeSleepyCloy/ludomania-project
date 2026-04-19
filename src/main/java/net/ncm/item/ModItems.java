package net.ncm.item;

import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.ncm.Ludomania;
import net.ncm.ModFoodComponent;

public class ModItems {

    public static final RegistryKey<Item> PEAR_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Ludomania.MOD_ID, "pear"));
    public static final RegistryKey<Item> DALIT_AXE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Ludomania.MOD_ID, "dalit_axe"));

    public static final Item PEAR = registerItem("pear",
            new Item(new Item.Settings().registryKey(PEAR_KEY).food(ModFoodComponent.PEAR)));

    public static final Item DALIT_AXE = registerItem("dalit_axe",
            new AxeItem(ToolMaterial.IRON, 6.0F, -3.1F, new Item.Settings().registryKey(DALIT_AXE_KEY)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Ludomania.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Ludomania.LOGGER.info("Registering Mod Items for " + Ludomania.MOD_ID);
    }
}