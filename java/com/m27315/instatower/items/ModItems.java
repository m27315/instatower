package com.m27315.instatower.items;

import net.minecraft.item.Item;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

public final class ModItems {
	public static Item instaTowerItem;
	public static Item diviningRodItem;
	public static Item instaGardenItem;

	public static void init(FMLPreInitializationEvent event, Logger logger) {
		instaTowerItem = new ItemInstaTower(event, logger);
		instaGardenItem = new ItemInstaGarden(event, logger);
		diviningRodItem = new ItemDiviningRod(event, logger);
	}
}
