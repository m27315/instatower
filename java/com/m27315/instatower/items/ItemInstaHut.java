package com.m27315.instatower.items;

import lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInstaHut extends ItemInstaStructure {
	protected String name = "iteminstahut";

	public ItemInstaHut(Logger logger, String hutCraft, boolean hutChest) {
		this.hutChest = hutChest;
		this.schematic = "/assets/" + Constants.MODID
				+ "/schematics/instahut.cfg";
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		if (hutCraft.equalsIgnoreCase("hard")) {
			GameRegistry.addRecipe(new ItemStack(this), "tdt", "cfw", "dWd",
					't', Blocks.torch, 'd', Blocks.dirt, 'c', Blocks.chest,
					'f', Blocks.furnace, 'w', Blocks.wool, 'W', Blocks.log);
		} else if (hutCraft.equalsIgnoreCase("easy")) {
			GameRegistry.addRecipe(new ItemStack(this), "dpd", "dCd", "dWd",
					'd', Blocks.dirt, 'W', Blocks.log, 'C', Blocks.cobblestone,
					'p', Blocks.planks);
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		return setStructure(stack, player, world, x, y, z, side, hitX, hitY,
				hitZ, false);
	}

}
