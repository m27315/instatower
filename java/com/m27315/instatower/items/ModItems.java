package com.m27315.instatower.items;

import net.minecraft.item.Item;

public final class ModItems {
	public static Item instaTowerItem;

	public static void init() {
		instaTowerItem = new ItemInstaTower();
	}
}
