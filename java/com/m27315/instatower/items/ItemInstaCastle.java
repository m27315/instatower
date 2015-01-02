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
import net.minecraftforge.common.util.ForgeDirection;

public class ItemInstaCastle extends ItemInstaStructure {
	private String name = "iteminstacastle";
	private int halfWallLength = 0;
	private int halfWallWidth = 0;

	public ItemInstaCastle(FMLPreInitializationEvent event, Logger logger) {
		halfWallLength = ItemInstaWall.wallLength / 2;
		halfWallWidth = ItemInstaWall.wallWidth / 2;
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		GameRegistry.addRecipe(new ItemStack(this), "TwT", "wGw", "TwT", 'T',
				new ItemStack(ModItems.instaTowerItem), 'w', new ItemStack(
						ModItems.instaWallItem), 'G', new ItemStack(
						ModItems.instaGardenItem));
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		if (!world.isRemote && side == 1
				&& player.canPlayerEdit(x, y + 1, z, side, stack)) {

			// Don't consume item, if in creative mode.
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}

			// Direction: 0=South, 1=West, 2=North, 3=East
			int facingDirection = MathHelper
					.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

			int d1 = halfWallLength;
			int d2 = halfWallLength + halfWallWidth + 1;
			switch (facingDirection) {
			case SOUTH:
				z += halfWallLength + ItemInstaWall.wallWidth;
				break;
			case WEST:
				x += halfWallLength + ItemInstaWall.wallWidth;
				break;
			case NORTH:
				z -= (halfWallLength + ItemInstaWall.wallWidth);
				break;
			case EAST:
				x -= (halfWallLength + ItemInstaWall.wallWidth);
				break;
			}
			// NORTH-WEST tower
			ModItems.instaTowerItem.setStructure(world, x - d2, y, z - d1,
					NORTH, false);
			// NORTH-EAST tower
			ModItems.instaTowerItem.setStructure(world, x + d1, y, z - d2,
					EAST, false);
			// SOUTH-EAST tower
			ModItems.instaTowerItem.setStructure(world, x + d2, y, z + d1,
					SOUTH, false);
			// SOUTH-WEST tower
			ModItems.instaTowerItem.setStructure(world, x - d1, y, z + d2,
					WEST, false);
			// NORTHERN wall
			ModItems.instaWallItem.setWall(world, x + d1, y, z - d2, EAST);
			// EASTERN wall
			ModItems.instaWallItem.setWall(world, x + d2, y, z + d1, SOUTH);
			// SOUTHERN wall
			ModItems.instaWallItem.setWall(world, x - d1, y, z + d2, WEST);
			// WESTERN wall
			ModItems.instaWallItem.setWall(world, x - d2, y, z - d1, NORTH);
			ModItems.instaGardenItem.setStructure(world, x + 21, y, z
					+ halfWallLength - 2, NORTH, true);
			ModItems.instaGardenItem.setStructure(world, x - 3, y, z
					+ halfWallLength - 2, NORTH, true);
			return true;
		}
		return false;
	}
}
