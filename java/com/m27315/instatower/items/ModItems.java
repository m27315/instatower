package com.m27315.instatower.items;

import net.minecraft.item.Item;

public final class ModItems {
	public static Item instaTowerItem;
	public static Item diviningRodItem;

	public static void init() {
		instaTowerItem = new ItemInstaTower();
		diviningRodItem = new ItemDiviningRod();
	}
}
