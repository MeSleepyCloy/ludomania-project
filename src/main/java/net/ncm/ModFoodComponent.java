package net.ncm;

import net.minecraft.component.type.FoodComponent;

public class ModFoodComponent {
    public static FoodComponent PEAR = new FoodComponent.Builder().nutrition(3).saturationModifier(0.4f).build();
}
