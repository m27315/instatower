package com.m27315.instatower.items;

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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemInstaTower extends Item {
	private String name = "iteminstatower";
	private int numberOfChests = 0;

	public ItemInstaTower() {
		this.setMaxStackSize(64);
		this.setUnlocalizedName(Constants.MODID + '_' + name);
		this.setCreativeTab(CreativeTabs.tabMaterials);
		this.setTextureName(Constants.MODID + ":" + name);
		GameRegistry.registerItem(this, name);
		GameRegistry.addRecipe(new ItemStack(this), "SWS", "GSG", "dwd", 'd',
				new ItemStack(Blocks.dirt), 'w', new ItemStack(
						Items.wheat_seeds), 'G', new ItemStack(Blocks.glass),
				'S', new ItemStack(Blocks.cobblestone), 'W', new ItemStack(
						Items.stick));
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
			int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && side == 1
				&& player.canPlayerEdit(x, y + 1, z, side, stack)
				&& player.canPlayerEdit(x, y + 2, z, side, stack)) {
			InstaTower.logger.debug("onItemUseFirst: stack=" + stack
					+ "\n    x=" + x + ", y=" + y + ", z=" + z + ", side="
					+ side + "\n    hitX=" + hitX + ", hitY=" + hitY
					+ ", hitZ=" + hitZ);
			// reportMetaData(world, -472, 78, 404);
			// Prepare site - assume first layer is biggest.
			Block dirt = Blocks.dirt;
			List<List<Block>> blocks = InstaTower.layerDefs
					.get(InstaTower.layerStack.get(0));
			for (int j = 0; j < blocks.size(); j++) {
				List<Block> row = blocks.get(j);
				for (int i = 0; i < row.size(); i++) {
					// Clear out blocks between this block and top of structure
					for (int k = InstaTower.layerStack.size(); k > 0; k--) {
						world.setBlockToAir(x + i, y + k, z + j);
					}
					// Build up with dirt between this block down to ground.
					for (int k = y - 1; k >= 0; k--) {
						if (world.isAirBlock(x + i, k, z + j)) {
							setBlock(world, x + i, k, z + j, dirt);
						} else {
							break;
						}
					}
				}
			}
			// Lay down each layer
			for (int n = 0; n < InstaTower.layerStack.size(); n++) {
				String layer = InstaTower.layerStack.get(n);
				blocks = InstaTower.layerDefs.get(layer);
				setLayer(world, x, y + n, z, blocks);
			}
			// Have some animals! Yee-haw!
			for (int n = 0; n < 2; n++) {
				spawnEntity(world, new EntityChicken(world), x, y, z);
				spawnEntity(world, new EntityCow(world), x, y, z);
				spawnEntity(world, new EntityHorse(world), x, y, z);
				// spawnEntity(world, new EntityPig(world), x, y, z);
				// spawnEntity(world, new EntitySheep(world), x, y, z);
			}
			// Don't consume item, if in creative mode.
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			return true;
		}
		return false;
	}

	private Entity spawnEntity(World world, Entity entity, int x, int y, int z) {
		entity.setPosition(x + 4, y + 3, z + 4);
		world.spawnEntityInWorld(entity);
		return entity;
	}

	private void setLayer(World world, int x, int y, int z,
			List<List<Block>> blocks) {
		Block anvil = Blocks.anvil;
		Block bed = Blocks.bed;
		Block button = Blocks.wooden_button;
		Block carpet = Blocks.carpet;
		Block carrot = Blocks.carrots;
		Block chest = Blocks.chest;
		Block dirt = Blocks.dirt;
		Block door = Blocks.iron_door;
		Block gate = Blocks.fence_gate;
		Block ladder = Blocks.ladder;
		Block melon = Blocks.melon_stem;
		Block plate = Blocks.wooden_pressure_plate;
		Block potatoe = Blocks.potatoes;
		Block pumpkin = Blocks.pumpkin_stem;
		Block reeds = Blocks.reeds;
		Block torch = Blocks.torch;
		Block water = Blocks.water;
		Block wheat = Blocks.wheat;

		// place everything but torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Block> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				Block b = row.get(i);
				if (b == null) {
					world.setBlockToAir(x + i, y, z + j);
				} else if (b.equals(anvil)) {
					world.setBlock(x + i, y, z + j, b, 1, 3);
				} else if (b.equals(bed)) {
					world.setBlock(x + i, y, z + j, b, 8, 3); // head of bed
					world.setBlock(x + i, y, z + j - 1, b, 0, 3); // foot
				} else if (b.equals(button)) {
					world.setBlock(x + i, y, z + j, b, 1, 3);
				} else if (b.equals(carpet)) {
					world.setBlock(x + i, y, z + j, b, 14, 3);
				} else if (b.equals(gate)) {
					world.setBlock(x + i, y, z + j, b, 3, 3);
				} else if (b.equals(ladder)) {
					world.setBlock(x + i, y, z + j, b, 5, 3);
				} else if (b.equals(water)) {
					world.setBlock(x + i, y, z + j, b, 0, 3);
				} else if (b.equals(wheat) || b.equals(carrot)
						|| b.equals(potatoe) || b.equals(reeds)
						|| b.equals(melon) || b.equals(pumpkin)) {
					world.setBlock(x + i, y, z + j, b, 6, 3);
				} else if (!(b.equals(torch) || b.equals(door) || b
						.equals(plate))) {
					TileEntity te = setBlock(world, x + i, y, z + j, b);

					if (b.equals(chest)) {
						TileEntityChest tec = (TileEntityChest) te;
						ItemStack stack = null;
						switch (++this.numberOfChests % 16) {
						case 0:
							stack = null;
							break;
						case 1:
							stack = new ItemStack(Items.diamond, 64);
							break;
						case 2:
							stack = new ItemStack(Items.iron_ingot, 64);
							break;
						case 3:
							stack = new ItemStack(Items.cooked_beef, 64);
							break;
						case 4:
							stack = new ItemStack(Items.enchanted_book, 64);
							break;
						case 5:
							stack = new ItemStack(Items.coal, 64);
							break;
						case 6:
							stack = new ItemStack(Items.flint, 64);
							break;
						case 7:
							stack = new ItemStack(Items.golden_apple, 64);
							break;
						case 8:
							stack = new ItemStack(Items.arrow, 64);
							break;
						case 9:
							stack = new ItemStack(Blocks.rail, 64);
							break;
						case 10:
							stack = new ItemStack(Blocks.redstone_torch, 64);
							break;
						case 11:
							stack = new ItemStack(Blocks.redstone_block, 64);
							break;
						case 12:
							stack = new ItemStack(Blocks.activator_rail, 64);
							break;
						case 13:
							stack = new ItemStack(Blocks.detector_rail, 64);
							break;
						case 14:
							stack = new ItemStack(Items.stick, 64);
							break;
						case 15:
							stack = new ItemStack(Blocks.ladder, 64);
							break;
						}
						if (stack != null) {
							for (int slot = tec.getSizeInventory() - 1; slot >= 0; slot--) {
								tec.setInventorySlotContents(slot, stack);
							}
						}
					}
				}
				world.notifyBlockChange(x + i, y, z + j, b);
			}
		}
		// place torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Block> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				Block b = row.get(i);
				if (b.equals(torch)) {
					if (world.isSideSolid(x + i, y - 1, z + j,
							ForgeDirection.UP, false)) {
						setBlock(world, x + i, y, z + j, b);
					} else {
						setBlock(world, x + i, y, z + j, b);
					}
				} else if (b.equals(plate)) {
					world.setBlock(x + i, y, z + j, b, 0, 3);
				} else if (b.equals(door)) {
					ItemDoor.placeDoorBlock(world, x + i, y, z + j, 3, b);
					// world.setBlock(x + i, y, z + j, b, 2, 0);
					// world.setBlock(x + i, y + 1, z + j, b, 8, 0);
					// world.notifyBlockChange(x + i, y, z + j, b);
					// world.notifyBlockChange(x + i, y + 1, z + j, b);
				}
			}
		}

	}

	private TileEntity setBlock(World world, int x, int y, int z, Block block) {
		// InstaTower.logger.info("setBlock: " + x + "," + y + "," + z + " - "
		// + block.getLocalizedName());
		world.setBlock(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	private void reportMetaData(World w, int x, int y, int z) {
		InstaTower.logger.info("METADATA: " + x + "," + y + "," + z + ": "
				+ w.getBlockMetadata(x, y, z) + " - "
				+ w.getBlock(x, y, z).getLocalizedName() + " - "
				+ Block.getIdFromBlock(w.getBlock(x, y, z)));
	}
}
