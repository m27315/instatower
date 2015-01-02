package com.m27315.instatower.items;

import net.minecraft.item.Item;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

public final class ModItems {
	public static Item diviningRodItem;
	public static ItemInstaTower instaTowerItem;
	public static ItemInstaGarden instaGardenItem;
	public static ItemInstaWall instaWallItem;
	public static ItemInstaCastle instaCastleItem;

	public static void init(FMLPreInitializationEvent event, Logger logger) {
		diviningRodItem = new ItemDiviningRod(event, logger);
		instaTowerItem = new ItemInstaTower(event, logger);
		instaGardenItem = new ItemInstaGarden(event, logger);
		instaWallItem = new ItemInstaWall(event, logger);
		instaCastleItem = new ItemInstaCastle(event, logger);
	}
}
