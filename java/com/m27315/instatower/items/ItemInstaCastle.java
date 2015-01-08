package com.m27315.instatower.items;

import lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInstaCastle extends ItemInstaStructure {
	private String name = "iteminstacastle";
	private int wallLength = 0;
	private int halfWallLength = 0;
	private int halfWallWidth = 0;

	public ItemInstaCastle(Logger logger, int wallLength) {
		this.wallLength = wallLength;
		halfWallLength = wallLength / 2;
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
			// Only place gardens if enough room.
			if (wallLength >= 51) {
				// EASTERN garden
				ModItems.instaGardenItem.setStructure(world, x + 21, y, z
						+ halfWallLength - 2, NORTH, true);
				// WESTERN garden
				ModItems.instaGardenItem.setStructure(world, x - 3, y, z
						+ halfWallLength - 2, NORTH, true);
			}
			return true;
		}
		return false;
	}
}
