package com.m27315.instatower.items;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public final class ModItems {
	public static Item diviningRodItem;
	public static ItemInstaTower instaTowerItem;
	public static ItemInstaGarden instaGardenItem;
	public static ItemInstaWall instaWallItem;
	public static ItemInstaCastle instaCastleItem;

	public static void init(FMLPreInitializationEvent event, Logger logger) {
		Configuration config = new Configuration(
				event.getSuggestedConfigurationFile());
		config.load();
		boolean towerCraft = config.getBoolean("towerCraft", "Tower", true,
				"When enabled, the InstaTower "
						+ " may be crafted using a simple "
						+ "recipe; otherwise, it is only "
						+ "available in Creative Mode.");
		boolean towerAnvil = config.getBoolean("towerAnvil", "Tower", true,
				"When enabled, an anvil is created in each tower.");
		boolean towerBeacon = config.getBoolean("towerBeacon", "Tower", true,
				"When enabled, creates beacon on top of tower.");
		boolean towerBrewing = config.getBoolean("towerBrewing", "Tower", true,
				"When enabled, a brewing stand is created in each tower.");
		boolean towerChests = config.getBoolean("towerChests", "Tower", true,
				"When enabled, all chests in tower"
						+ " are populated with goodies.");
		boolean towerLibrary = config.getBoolean("towerLibrary", "Tower", true,
				"When enabled, an enchantment table surrounded "
						+ "by bookshelves is created in tower.");
		boolean towerBasement = config.getBoolean("towerBasement", "Tower",
				true,
				"When enabled, a basement is created with potential mining "
						+ "tunnel access and possible rail lines.");
		boolean towerRails = config
				.getBoolean("towerRails", "Tower", true,
						"When enabled, a railine is included in each tower and castle.");
		boolean tunnelEnable = config
				.getBoolean("tunnelEnable", "Tunnel", true,
						"When set, tunnels will be dug with lateral mining runs.");
		int tunnelDepth = config.getInt("tunnelDepth", "Tunnel", 6, 0, 100,
				"Determines how deep the mining tunnel will"
						+ " descend until creating lateral runs");
		int tunnelLength = config.getInt("tunnelLength", "Tunnel", 501, 6,
				100000, "Determines length of lateral tunnel runs.");
		boolean tunnelClear = config.getBoolean("tunnelClear", "Tunnel", false,
				"When set all ores will be cleared in addition to debris.");
		boolean tunnelRails = config.getBoolean("tunnelRails", "Tunnel", true,
				"When enabled, a railine when be included in each tunnel.");
		boolean wallCraft = config.getBoolean("wallCraft", "Wall", true,
				"When enabled, the InstaWall may be crafted using "
						+ "a simple recipe; otherwise, it is only "
						+ "available in Creative mode.");
		int wallLength = config.getInt("wallLength", "Wall", 51, 13, 100001,
				"The length of a single wall segment for "
						+ "both castles and individual walls.");
		boolean gardenCraft = config.getBoolean("gardenCraft", "Garden", true,
				"When enabled, the InstaGarden may be crafted using "
						+ "a simple recipe; otherwise, it is only "
						+ "available in Creative mode.");
		boolean gardenAnimals = config.getBoolean("gardenAnimals", "Garden",
				true, "When set, animals will be spawned just "
						+ "outside the garden, where placed.");
		config.save();

		diviningRodItem = new ItemDiviningRod(event, logger);
		instaTowerItem = new ItemInstaTower(logger, towerCraft, towerAnvil,
				towerBeacon, towerBrewing, towerChests, towerLibrary,
				towerBasement, towerRails, tunnelEnable, tunnelClear,
				tunnelDepth, tunnelLength, tunnelRails);
		instaGardenItem = new ItemInstaGarden(logger, gardenCraft,
				gardenAnimals);
		instaWallItem = new ItemInstaWall(logger, wallCraft, wallLength,
				towerBasement, towerRails);
		instaCastleItem = new ItemInstaCastle(logger, wallLength);
	}
}
