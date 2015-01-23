package com.m27315.instatower.items;

import lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInstaGarden extends ItemInstaStructure {
	protected String name = "iteminstagarden";

	public ItemInstaGarden(Logger logger, boolean gardenCraft,
			boolean gardenAnimals) {
		this.gardenAnimals = gardenAnimals;
		this.schematic = "/assets/" + Constants.MODID
				+ "/schematics/instagarden.cfg";
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabFood);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		if (gardenCraft) {
			GameRegistry.addRecipe(new ItemStack(this), "Lsg", "MPw", "cdp",
					'L', Blocks.log, 'g', Blocks.glowstone, 'M',
					Items.melon_seeds, 's', Items.reeds, 'P',
					Items.pumpkin_seeds, 'c', Items.golden_carrot, 'w',
					Items.wheat_seeds, 'd', Blocks.diamond_block, 'p',
					Items.potato);
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		return setStructure(stack, player, world, x, y, z, side, hitX, hitY,
				hitZ, true);
	}
}
