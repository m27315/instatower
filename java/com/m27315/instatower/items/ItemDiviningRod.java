package com.m27315.instatower.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lib.Constants;

import org.apache.http.Consts;

import com.m27315.instatower.InstaTower;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
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

public class ItemDiviningRod extends Item {
	private String name = "itemdiviningrod";

	public ItemDiviningRod() {
		this.setMaxStackSize(1);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabTools);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		GameRegistry.addRecipe(new ItemStack(this), " S ", " S ", "SS ", 'S',
				new ItemStack(Items.stick));
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
			World world, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ) {
		reportMetaData(world, x, y, z);
		return true;
	}

	private void reportMetaData(World w, int x, int y, int z) {
		InstaTower.logger.info("[" + name + "]: Coords (x,y,z)=" + x + "," + y
				+ "," + z + "; Metadata=" + w.getBlockMetadata(x, y, z)
				+ "; Name=" + w.getBlock(x, y, z).getLocalizedName() + "; Id="
				+ Block.getIdFromBlock(w.getBlock(x, y, z)));
	}

}
