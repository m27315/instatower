package com.m27315.instatower.items;

import lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemInstaTower extends ItemInstaStructure {
	protected String name = "iteminstatower";

	public ItemInstaTower(Logger logger, boolean towerCraft,
			boolean towerAnvil, boolean towerBeacon, boolean towerBrewing,
			boolean towerChests, boolean towerLibrary, boolean towerBasement,
			boolean towerRails, boolean tunnelEnable, boolean tunnelClear,
			int tunnelDepth, int tunnelLength, boolean tunnelRails) {
		this.towerAnvil = towerAnvil;
		this.towerBeacon = towerBeacon;
		this.towerBrewing = towerBrewing;
		this.towerChests = towerChests;
		this.towerLibrary = towerLibrary;
		this.towerBasement = towerBasement;
		this.towerRails = towerRails;
		this.tunnelEnable = tunnelEnable;
		this.tunnelClear = tunnelClear;
		this.tunnelDepth = tunnelDepth;
		this.tunnelLength = tunnelLength;
		this.tunnelRails = tunnelRails;
		this.schematic = "/assets/" + Constants.MODID
				+ "/schematics/instatower.cfg";
		this.logger = logger;
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		if (towerCraft) {
			GameRegistry
					.addRecipe(new ItemStack(this), "SWS", "GSG", "dwd", 'd',
							new ItemStack(dirt), 'w', new ItemStack(
									Items.wheat_seeds), 'G', new ItemStack(
									glass), 'S', new ItemStack(cobblestone),
							'W', new ItemStack(Items.stick));
		}
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		return setStructure(stack, player, world, x, y, z, side, hitX, hitY,
				hitZ, false);
	}

}
