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

public class ItemInstaWall extends ItemInstaStructure {
	private static String name = "iteminstawall";
	public static int wallWidth = 11;
	public static int wallLength = 51;
	public static int wallHeight = 30;
	private static Block air = Blocks.air;
	private static Block brick = Blocks.stonebrick;
	private static Block button = Blocks.wooden_button;
	private static Block dirt = Blocks.dirt;
	private static Block door = Blocks.iron_door;
	private static Block fence = Blocks.fence;
	private static Block gate = Blocks.fence_gate;
	private static Block glow = Blocks.glowstone;
	private static Block plate = Blocks.wooden_pressure_plate;
	private static Block pRail = Blocks.golden_rail;
	private static Block rail = Blocks.rail;
	private static Block redTorch = Blocks.redstone_torch;
	private static Block stone = Blocks.stone;
	private static Block torch = Blocks.torch;

	public ItemInstaWall(FMLPreInitializationEvent event, Logger logger) {
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		GameRegistry.addRecipe(new ItemStack(this), "SGS", "SWS", "SIS", 'S',
				new ItemStack(Blocks.stonebrick), 'W', new ItemStack(
						Blocks.wool), 'G', new ItemStack(Blocks.glass), 'I',
				new ItemStack(Items.iron_ingot));
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

			setWall(world, x, y, z, facingDirection);
			return true;
		}
		return false;
	}

	public void setWall(World world, int x, int y, int z, int dir) {
		int height = 5;
		int hw = wallWidth / 2;
		// Prepare and build-up site.
		switch (dir) {
		case SOUTH:
			setBasementSliceNS(world, x, y - 6, z + 1, hw, 6, 3);
			for (int j = 0; j < wallLength; j++) {
				prepareSliceNS(world, x, y, z - j, hw, j);
				setBasementSliceNS(world, x, y - 6, z - j, hw, 6, j);
				setWallSliceNS(world, x, y, z - j, hw, height, j);
			}
			setBasementSliceNS(world, x, y - 6, z - wallLength, hw, 6,
					wallLength);
			setDoorNS(world, x, y, z - wallLength / 2, hw);
			break;
		case WEST:
			setBasementSliceEW(world, x - 1, y - 6, z, hw, 6, 3);
			for (int i = 0; i < wallLength; i++) {
				prepareSliceEW(world, x + i, y, z, hw, i);
				setBasementSliceEW(world, x + i, y - 6, z, hw, 6, i);
				setWallSliceEW(world, x + i, y, z, hw, height, i);
			}
			setBasementSliceEW(world, x + wallLength, y - 6, z, hw, 6,
					wallLength);
			setDoorEW(world, x + wallLength / 2, y, z, hw);
			break;
		case NORTH:
			setBasementSliceNS(world, x, y - 6, z - 1, hw, 6, 3);
			for (int j = 0; j < wallLength; j++) {
				prepareSliceNS(world, x, y, z + j, hw, j);
				setBasementSliceNS(world, x, y - 6, z + j, hw, 6, j);
				setWallSliceNS(world, x, y, z + j, hw, height, j);
			}
			setBasementSliceNS(world, x, y - 6, z + wallLength, hw, 6,
					wallLength);
			setDoorNS(world, x, y, z + wallLength / 2, hw);
			break;
		case EAST:
			setBasementSliceEW(world, x + 1, y - 6, z, hw, 6, 3);
			for (int i = 0; i < wallLength; i++) {
				prepareSliceEW(world, x - i, y, z, hw, i);
				setBasementSliceEW(world, x - i, y - 6, z, hw, 6, i);
				setWallSliceEW(world, x - i, y, z, hw, height, i);
			}
			setBasementSliceEW(world, x - wallLength, y - 6, z, hw, 6,
					wallLength);
			setDoorEW(world, x - wallLength / 2, y, z, hw);
			break;
		}
	}

	private void prepareSliceNS(World world, int x, int y, int z, int hw, int s) {
		for (int i = -hw - 3; i <= hw + 3; i++) {
			// Build up with dirt between this block down to ground.
			for (int k = y - 1; k >= 0; k--) {
				if (world.isAirBlock(x + i, k, z)) {
					setBlock(world, x + i, k, z, stone);
				} else {
					break;
				}
			}
			// Clear out trees and hills.
			if (s > 2 && s < wallLength - 2) {
				for (int k = 1; k < wallHeight; k++) {
					Block b = world.getBlock(x + i, y + k, z);
					if (!(b.equals(air) || b.equals(torch) || b.equals(brick) || b
							.equals(glow))) {
						world.setBlockToAir(x + i, y + k, z);
					}
				}
			}
		}
		for (int i = -hw; i <= hw; i++) {
			setBlock(world, x + i, y, z, brick);
		}
		for (int i = 1; i <= 3; i++) {
			setBlock(world, x + hw + i, y, z, dirt);
			setBlock(world, x - hw - i, y, z, dirt);
		}
		if (s > 4 && s < wallLength - 4) {
			setBlock(world, x - hw - 3, y + 1, z, fence);
			setBlock(world, x + hw + 3, y + 1, z, fence);
		}
	}

	private void prepareSliceEW(World world, int x, int y, int z, int hw, int s) {
		for (int j = -hw - 3; j <= hw + 3; j++) {
			// Build up with dirt between this block down to ground.
			for (int k = y - 1; k >= 0; k--) {
				if (world.isAirBlock(x, k, z + j)) {
					setBlock(world, x, k, z + j, stone);
				} else {
					break;
				}
			}
			// Clear out trees and hills.
			if (s > 2 && s < wallLength - 2) {
				for (int k = 1; k < wallHeight; k++) {
					Block b = world.getBlock(x, y + k, z + j);
					if (!(b.equals(air) || b.equals(torch) || b.equals(brick) || b
							.equals(glow))) {
						world.setBlockToAir(x, y + k, z + j);
					}
				}
			}
		}
		for (int j = -hw; j <= hw; j++) {
			setBlock(world, x, y, z + j, brick);
		}
		for (int j = 1; j <= 3; j++) {
			setBlock(world, x, y, z + hw + j, dirt);
			setBlock(world, x, y, z - hw - j, dirt);
		}
		if (s > 4 && s < wallLength - 4) {
			setBlock(world, x, y + 1, z - hw - 3, fence);
			setBlock(world, x, y + 1, z + hw + 3, fence);
		}
	}

	private void setBasementSliceNS(World world, int x, int y, int z, int hw,
			int h, int s) {
		for (int i = -hw; i <= hw; i++) {
			setBlock(world, x + i, y, z, brick);
		}
		for (int k = 1; k < h; k++) {
			setBlock(world, x - hw, y + k, z, brick);
			setBlock(world, x + hw, y + k, z, brick);
			for (int i = -hw + 1; i < hw; i++) {
				world.setBlockToAir(x + i, y + k, z);
			}
		}
		switch (s % 4) {
		case 0:
			setBlock(world, x, y + 1, z, pRail);
			setBlock(world, x - 1, y + 1, z, redTorch, 5, blockUpdateFlag);
			setBlock(world, x, y + h - 1, z, glow);
			break;
		case 2:
			setBlock(world, x - hw + 1, y + h - 1, z, torch);
			setBlock(world, x + hw - 1, y + h - 1, z, torch);
		default:
			setBlock(world, x, y + 1, z, rail);
			break;
		}
	}

	private void setBasementSliceEW(World world, int x, int y, int z, int hw,
			int h, int s) {
		for (int j = -hw; j <= hw; j++) {
			setBlock(world, x, y, z + j, brick);
		}
		for (int k = 1; k < h; k++) {
			setBlock(world, x, y + k, z - hw, brick);
			setBlock(world, x, y + k, z + hw, brick);
			for (int j = -hw + 1; j < hw; j++) {
				world.setBlockToAir(x, y + k, z + j);
			}
		}
		switch (s % 4) {
		case 0:
			setBlock(world, x, y + 1, z, pRail);
			setBlock(world, x, y + 1, z - 1, redTorch, 5, blockUpdateFlag);
			setBlock(world, x, y + h - 1, z, glow);
			break;
		case 2:
			setBlock(world, x, y + h - 1, z - hw + 1, torch, 3, blockUpdateFlag);
			setBlock(world, x, y + h - 1, z + hw - 1, torch, 4, blockUpdateFlag);
		default:
			setBlock(world, x, y + 1, z, rail);
			break;
		}
	}

	private void setWallSliceNS(World world, int x, int y, int z, int hw,
			int h, int s) {
		for (int k = 1; k < h; k++) {
			setBlock(world, x - hw, y + k, z, brick);
			setBlock(world, x + hw, y + k, z, brick);
			for (int i = -hw + 1; i < hw; i++) {
				Block b = world.getBlock(x + i, y + k, z);
				if (!(b.equals(air) || b.equals(torch) || b.equals(button))) {
					world.setBlockToAir(x + i, y + k, z);
				}
			}
		}
		setBlock(world, x, y + 1, z, Blocks.carpet, 14, blockUpdateFlag);
		y += h;
		for (int i = -hw; i <= hw; i++) {
			setBlock(world, x + i, y, z, brick);
		}
		y++;
		for (int i = 1 - hw; i < hw; i++) {
			world.setBlockToAir(x + i, y, z);
			world.setBlockToAir(x + i, y + 1, z);
		}
		if (s > 1 && s < wallLength - 2) {
			setBlock(world, x - hw, y, z, brick);
			setBlock(world, x + hw, y, z, brick);
			switch (s % 4) {
			case 0:
				setBlock(world, x - hw + 1, y - 2, z, torch);
				setBlock(world, x + hw - 1, y - 2, z, torch);
			case 2:
				setBlock(world, x - hw, y + 1, z, brick);
				setBlock(world, x + hw, y + 1, z, brick);
				setBlock(world, x - hw - 1, y + 1, z, torch);
				setBlock(world, x + hw + 1, y + 1, z, torch);
				setBlock(world, x, y - 2, z, glow);
				break;
			}
		}
	}

	private void setWallSliceEW(World world, int x, int y, int z, int hw,
			int h, int s) {
		for (int k = 1; k < h; k++) {
			setBlock(world, x, y + k, z - hw, brick);
			setBlock(world, x, y + k, z + hw, brick);
			for (int j = -hw + 1; j < hw; j++) {
				Block b = world.getBlock(x, y + k, z + j);
				if (!(b.equals(air) || b.equals(torch) || b.equals(button))) {
					world.setBlockToAir(x, y + k, z + j);
				}
			}
		}
		setBlock(world, x, y + 1, z, Blocks.carpet, 14, blockUpdateFlag);
		y += h;
		for (int j = -hw; j <= hw; j++) {
			setBlock(world, x, y, z + j, brick);
		}
		y++;
		for (int j = 1 - hw; j < hw; j++) {
			world.setBlockToAir(x, y, z + j);
			world.setBlockToAir(x, y + 1, z + j);
		}
		if (s > 1 && s < wallLength - 2) {
			setBlock(world, x, y, z - hw, brick);
			setBlock(world, x, y, z + hw, brick);
			switch (s % 4) {
			case 0:
				setBlock(world, x, y - 2, z - hw + 1, torch);
				setBlock(world, x, y - 2, z + hw - 1, torch);
			case 2:
				setBlock(world, x, y + 1, z - hw, brick);
				setBlock(world, x, y + 1, z + hw, brick);
				setBlock(world, x, y + 1, z - hw - 1, torch);
				setBlock(world, x, y + 1, z + hw + 1, torch);
				setBlock(world, x, y - 2, z, glow);
				break;
			}
		}
	}

	private void setDoorNS(World world, int x, int y, int z, int hw) {
		// EAST facing door
		setBlock(world, x - hw - 3, y + 1, z, gate, 3, blockUpdateFlag);
		ItemDoor.placeDoorBlock(world, x - hw, y + 1, z, 0, door);
		setBlock(world, x - hw + 1, y + 1, z, plate);
		setBlock(world, x - hw - 1, y + 3, z, button, 2, blockUpdateFlag);
		setBlock(world, x - hw - 1, y + 3, z - 1, torch);
		setBlock(world, x - hw - 1, y + 3, z + 1, torch);
		setBlock(world, x - hw + 1, y + 3, z - 1, torch);
		setBlock(world, x - hw + 1, y + 3, z + 1, torch);
		// WEST facing door
		setBlock(world, x + hw + 3, y + 1, z, gate, 3, blockUpdateFlag);
		ItemDoor.placeDoorBlock(world, x + hw, y + 1, z, 2, door);
		setBlock(world, x + hw - 1, y + 1, z, plate);
		setBlock(world, x + hw + 1, y + 3, z, button, 1, blockUpdateFlag);
		setBlock(world, x + hw + 1, y + 3, z + 1, torch);
		setBlock(world, x + hw + 1, y + 3, z - 1, torch);
		setBlock(world, x + hw - 1, y + 3, z + 1, torch);
		setBlock(world, x + hw - 1, y + 3, z - 1, torch);
	}

	private void setDoorEW(World world, int x, int y, int z, int hw) {
		// NORTH facing door
		setBlock(world, x, y + 1, z - hw - 3, gate, 2, blockUpdateFlag);
		ItemDoor.placeDoorBlock(world, x, y + 1, z - hw, 1, door);
		setBlock(world, x, y + 1, z - hw + 1, plate);
		setBlock(world, x, y + 3, z - hw - 1, button, 4, blockUpdateFlag);
		setBlock(world, x - 1, y + 3, z - hw - 1, torch);
		setBlock(world, x + 1, y + 3, z - hw - 1, torch);
		setBlock(world, x - 1, y + 3, z - hw + 1, torch);
		setBlock(world, x + 1, y + 3, z - hw + 1, torch);
		// SOUTH facing door
		setBlock(world, x, y + 1, z + hw + 3, gate, 2, blockUpdateFlag);
		ItemDoor.placeDoorBlock(world, x, y + 1, z + hw, 3, door);
		setBlock(world, x, y + 1, z + hw - 1, plate);
		setBlock(world, x, y + 3, z + hw + 1, button, 3, blockUpdateFlag);
		setBlock(world, x + 1, y + 3, z + hw + 1, torch);
		setBlock(world, x - 1, y + 3, z + hw + 1, torch);
		setBlock(world, x + 1, y + 3, z + hw - 1, torch);
		setBlock(world, x - 1, y + 3, z + hw - 1, torch);
	}

}
