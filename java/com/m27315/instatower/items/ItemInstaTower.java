package com.m27315.instatower.items;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Constants;

import org.apache.http.Consts;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;
import com.m27315.instatower.InstaTower;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemInstaTower extends ItemInstaStructure {
	protected String name = "iteminstatower";

	public ItemInstaTower(FMLPreInitializationEvent event, Logger logger) {
		Configuration config = new Configuration(
				event.getSuggestedConfigurationFile());
		config.load();
		towerCraft = config.getBoolean("towerCraft", "Tower", towerCraft,
				"When enabled, the InstaTower "
						+ " may be crafted using a simple "
						+ "recipe; otherwise, it is only "
						+ "available in Creative Mode.");
		towerAnvil = config.getBoolean("towerAnvil", "Tower", towerAnvil,
				"When enabled, an anvil is created in each tower.");
		towerBeacon = config.getBoolean("towerBeacon", "Tower", towerBeacon,
				"When enabled, creates beacon on top of tower.");
		towerBrewing = config.getBoolean("towerBrewing", "Tower", towerBrewing,
				"When enabled, a brewing stand is created in each tower.");
		towerChests = config.getBoolean("towerChests", "Tower", towerChests,
				"When enabled, all chests in tower"
						+ " are populated with goodies.");
		towerLibrary = config.getBoolean("towerLibrary", "Tower", towerLibrary,
				"When enabled, an enchantment table surrounded "
						+ "by bookshelves is created in tower.");
		towerBasement = config.getBoolean("towerBasement", "Tower",
				towerBasement,
				"When enabled, a basement is created with potential mining "
						+ "tunnel access and possible rail lines.");
		towerRails = config
				.getBoolean("towerRails", "Tower", towerRails,
						"When enabled, a railine is included in each tower and castle.");
		tunnelEnable = config.getBoolean("tunnelEnable", "Tunnel",
				tunnelEnable,
				"When set, tunnels will be dug with lateral mining runs.");
		tunnelDepth = config.getInt("tunnelDepth", "Tunnel", tunnelDepth, 0,
				100, "Determines how deep the mining tunnel will"
						+ " descend until creating lateral runs");
		tunnelLength = config.getInt("tunnelLength", "Tunnel", 501, 6, 100000,
				"Determines length of lateral tunnel runs.");
		tunnelClear = config.getBoolean("tunnelClear", "Tunnel", tunnelClear,
				"When set all ores will be cleared in addition to debris.");
		tunnelRails = config.getBoolean("tunnelRails", "Tunnel", tunnelRails,
				"When enabled, a railine when be included in each tunnel.");
		wallCraft = config.getBoolean("wallCraft", "Wall", wallCraft,
				"When enabled, the InstaWall may be crafted using "
						+ "a simple recipe; otherwise, it is only "
						+ "available in Creative mode.");
		gardenCraft = config.getBoolean("gardenCraft", "Garden", gardenCraft,
				"When enabled, the InstaGarden may be crafted using "
						+ "a simple recipe; otherwise, it is only "
						+ "available in Creative mode.");
		gardenAnimals = config.getBoolean("gardenAnimals", "Garden",
				gardenAnimals, "When set, animals will be spawned just "
						+ "outside the garden, where placed.");
		config.save();
		this.schematic = "/assets/" + Constants.MODID
				+ "/schematics/instatower.cfg";
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		if (towerCraft) {
			GameRegistry.addRecipe(new ItemStack(this), "SWS", "GSG", "dwd",
					'd', new ItemStack(Blocks.dirt), 'w', new ItemStack(
							Items.wheat_seeds), 'G',
					new ItemStack(Blocks.glass), 'S', new ItemStack(
							Blocks.cobblestone), 'W',
					new ItemStack(Items.stick));
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		return setStructure(stack, player, world, x, y, z, side, hitX, hitY,
				hitZ, false);
	}

}
