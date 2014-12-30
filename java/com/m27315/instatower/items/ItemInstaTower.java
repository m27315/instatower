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

public class ItemInstaTower extends Item {
	private String name = "iteminstatower";
	private int groundLevel = 0;
	private int numberOfBeacons = 0;
	private int numberOfChests = 0;
	private int blockUpdateFlag = 3;
	public static Logger logger;
	public static HashMap<String, List<List<Character>>> layerDefs;
	public static List<String> layerStack;

	// CONSTANTS
	private final int SOUTH = 0;
	private final int WEST = 1;
	private final int NORTH = 2;
	private final int EAST = 3;

	public ItemInstaTower(FMLPreInitializationEvent event, Logger logger) {
		this.logger = logger;
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
				&& player.canPlayerEdit(x, y + 1, z, side, stack)) {

			// Don't consume item, if in creative mode.
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}

			loadConfigFile();
			numberOfBeacons = 0;
			numberOfChests = 0;
			List<List<Character>> prevBlocks = null;

			// Direction: 0=South, 1=West, 2=North, 3=East
			int facingDirection = MathHelper
					.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;
			// InstaTower.logger.info("onItemUseFirst: stack=" + stack
			// + "\n    x=" + x + ", y=" + y + ", z=" + z + ", side="
			// + side + "\n    hitX=" + hitX + ", hitY=" + hitY
			// + ", hitZ=" + hitZ);
			// InstaTower.logger.info("Look Vector=" + player.getLookVec()
			// + ", CameraPitch=" + player.cameraPitch + ", CameraYaw="
			// + player.cameraYaw + ", rotationPitch="
			// + player.rotationPitch + ", rotationYaw="
			// + player.rotationYaw + ", rotationHeadYaw="
			// + player.rotationYawHead + ", facing=" + facingDirection);
			// Offset structure to find red carpet entrance.
			Vec3 offset = findRedCarpet(facingDirection);
			x += (int) offset.xCoord;
			y -= groundLevel;
			z += (int) offset.zCoord;
			// Prepare site - assume first layer is biggest.
			Block stone = Blocks.stone;
			List<List<Character>> blocks = layerDefs.get(layerStack.get(0));
			// Rotate structure according to direction faced.
			blocks = rotateBlocks(blocks, facingDirection);
			for (int j = 0; j < blocks.size(); j++) {
				List<Character> row = blocks.get(j);
				for (int i = 0; i < row.size(); i++) {
					// Clear out blocks between this block and top of structure
					for (int k = layerStack.size(); k > 0; k--) {
						world.setBlockToAir(x + i, y + k, z + j);
					}
					// Build up with dirt between this block down to ground.
					for (int k = y - 1; k >= 0; k--) {
						if (world.isAirBlock(x + i, k, z + j)) {
							setBlock(world, x + i, k, z + j, stone);
						} else {
							break;
						}
					}
				}
			}
			// Lay down each layer - basic blocks first!
			for (int n = 0; n < layerStack.size(); n++) {
				String layer = layerStack.get(n);
				blocks = rotateBlocks(layerDefs.get(layer), facingDirection);
				setLayer1(world, x, y + n, z, blocks, prevBlocks);
				prevBlocks = blocks;
			}
			// Lay down each layer - dependent blocks next!
			prevBlocks = null;
			for (int n = 0; n < layerStack.size(); n++) {
				String layer = layerStack.get(n);
				blocks = rotateBlocks(layerDefs.get(layer), facingDirection);
				setLayer2(world, x, y + n, z, blocks, prevBlocks);
				prevBlocks = blocks;
			}
			// Have some animals! Yee-haw!
			x -= (int) offset.xCoord;
			y += groundLevel;
			z -= (int) offset.zCoord;
			switch (facingDirection) {
			case SOUTH:
				z -= 3;
				break;
			case WEST:
				x += 3;
				break;
			case NORTH:
				z += 3;
				break;
			case EAST:
				x -= 3;
				break;
			}
			for (int n = 0; n < 2; n++) {
				spawnEntity(world, new EntityChicken(world), x, y, z);
				spawnEntity(world, new EntityCow(world), x, y, z);
				spawnEntity(world, new EntityHorse(world), x, y, z);
				// spawnEntity(world, new EntityPig(world), x, y, z);
				// spawnEntity(world, new EntitySheep(world), x, y, z);
			}
			return true;
		}
		return false;
	}

	private List<List<Character>> rotateBlocks(
			final List<List<Character>> inBlocks, int facingDirection) {

		List<List<Character>> outBlocks = new ArrayList<List<Character>>();
		switch (facingDirection) {
		case SOUTH:
			// Facing SOUTH
			for (int i = inBlocks.get(0).size() - 1; i >= 0; i--) {
				List<Character> outRow = new ArrayList<Character>();
				for (int j = 0; j < inBlocks.size(); j++) {
					outRow.add(inBlocks.get(j).get(i));
				}
				outBlocks.add(outRow);
			}
			break;
		case WEST:
			// FACING WEST
			outBlocks = inBlocks;
			break;
		case NORTH:
			// FACING NORTH
			for (int i = 0; i < inBlocks.get(0).size(); i++) {
				List<Character> outRow = new ArrayList<Character>();
				for (int j = inBlocks.size() - 1; j >= 0; j--) {
					outRow.add(inBlocks.get(j).get(i));
				}
				outBlocks.add(outRow);
			}
			break;
		default:
			// FACING EAST
			for (int j = inBlocks.size() - 1; j >= 0; j--) {
				List<Character> outRow = new ArrayList<Character>();
				List<Character> inRow = inBlocks.get(j);
				for (int i = inRow.size() - 1; i >= 0; i--) {
					outRow.add(inRow.get(i));
				}
				outBlocks.add(outRow);
			}
			break;
		}
		return outBlocks;
	}

	private Vec3 findRedCarpet(int facingDirection) {
		Vec3 offset = Vec3.createVectorHelper(0.0, 0.0, 0.0);
		offset.xCoord = offset.yCoord = offset.zCoord = (double) 0;
		for (int n = 0; n < layerStack.size(); n++) {
			String layer = layerStack.get(n);
			List<List<Character>> blocks = layerDefs.get(layer);
			for (int j = 0; j < blocks.size(); j++) {
				List<Character> row = blocks.get(j);
				for (int i = 0; i < row.size(); i++) {
					char b = row.get(i);
					if ('r' == b) {
						switch (facingDirection) {
						case SOUTH:
							// Facing SOUTH
							offset.xCoord = (double) -j;
							offset.zCoord = (double) i - row.size() + 1.0;
							return offset;
						case WEST:
							// FACING WEST
							offset.xCoord = (double) -i;
							offset.zCoord = (double) -j;
							return offset;
						case NORTH:
							// FACING NORTH
							offset.xCoord = (double) j - blocks.size() + 1.0;
							offset.zCoord = (double) -i;
							return offset;
						default:
							// FACING EAST
							offset.xCoord = (double) i - row.size() + 1.0;
							offset.zCoord = (double) j - blocks.size() + 1.0;
							return offset;
						}
					}
				}
			}
		}
		return offset;
	}

	private List<Integer> findNeighbors(List<List<Character>> blocks,
			char neighbor, int i, int j) {
		List<Integer> neighbors = new ArrayList<Integer>();
		// logger.info("Finding " + neighbor + "-neighbors of for (i,j) = (" + i
		// + "," + j + ") which is a: " + blocks.get(j).get(i));
		// SOUTH
		// logger.info("Checking south (j+1=" + (j + 1) + "): "
		// + blocks.get(j + 1).get(i));
		if (j < blocks.size() - 1 && blocks.get(j + 1).get(i) == neighbor) {
			// logger.info("Added SOUTH");
			neighbors.add(SOUTH);
		}
		// WEST
		// logger.info("Checking west (i-1=" + (i - 1) + "): "
		// + blocks.get(j).get(i - 1));
		if (i > 0 && blocks.get(j).get(i - 1) == neighbor) {
			// logger.info("Added WEST");
			neighbors.add(WEST);
		}
		// NORTH
		// logger.info("Checking north (j-1=" + (j - 1) + "): "
		// + blocks.get(j - 1).get(i));
		if (j > 0 && blocks.get(j - 1).get(i) == neighbor) {
			// logger.info("Added NORTH");
			neighbors.add(NORTH);
		}
		// EAST
		// logger.info("Checking east (i+1=" + (i + 1) + "): "
		// + blocks.get(j).get(i + 1));
		if (i < blocks.get(j).size() - 1
				&& blocks.get(j).get(i + 1) == neighbor) {
			// logger.info("Added EAST");
			neighbors.add(EAST);
		}
		return neighbors;
	}

	private List<Integer> findOpenSides(List<List<Character>> blocks, int i,
			int j) {
		return findNeighbors(blocks, ' ', i, j);
	}

	private List<Integer> findOpenSidesWithBlockedBacks(
			List<List<Character>> blocks, int i, int j) {
		List<Integer> openWithBlockedBacks = new ArrayList<Integer>();
		List<Integer> openSides = findOpenSides(blocks, i, j);
		for (int side : openSides) {
			switch (side) {
			case SOUTH:
				if (!openSides.contains(NORTH))
					openWithBlockedBacks.add(side);
				break;
			case WEST:
				if (!openSides.contains(EAST))
					openWithBlockedBacks.add(side);
				break;
			case NORTH:
				if (!openSides.contains(SOUTH))
					openWithBlockedBacks.add(side);
				break;
			case EAST:
				if (!openSides.contains(WEST))
					openWithBlockedBacks.add(side);
				break;
			}
		}
		return openWithBlockedBacks;
	}

	private List<Integer> findPreferredOpenSides(List<List<Character>> blocks,
			int i, int j) {
		// Prefer open-sides that have something immediately behind them.
		List<Integer> openSides = findOpenSidesWithBlockedBacks(blocks, i, j);
		// If no preferred open-sides, return any open sides.
		if (openSides.isEmpty())
			return findOpenSides(blocks, i, j);
		// Return list of preferred.
		return openSides;
	}

	private void setAnvil(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		Block anvil = Blocks.anvil;
		List<Integer> stones = findNeighbors(blocks, 'S', i, j);
		if (stones.isEmpty()) {
			List<Integer> openSides = findOpenSides(blocks, i, j);
			if (openSides.isEmpty()) {
				// logger.info("Setting default anvil - no metadata.");
				setBlock(world, x, y, z, anvil);
			} else {
				switch (openSides.get(0)) {
				case SOUTH:
					// logger.info("Setting anvil open to SOUTH");
					setBlock(world, x, y, z, anvil, 1, blockUpdateFlag);
					break;
				case WEST:
					// logger.info("Setting anvil open to WEST");
					setBlock(world, x, y, z, anvil, 2, blockUpdateFlag);
					break;
				case NORTH:
					// logger.info("Setting anvil open to NORTH");
					setBlock(world, x, y, z, anvil, 2, blockUpdateFlag);
					break;
				case EAST:
					// logger.info("Setting anvil open to EAST");
					setBlock(world, x, y, z, anvil, 0, blockUpdateFlag);
					break;
				}
			}
		} else {
			switch (stones.get(0)) {
			case SOUTH:
				setBlock(world, x, y, z, anvil, 3, blockUpdateFlag);
				break;
			case WEST:
				setBlock(world, x, y, z, anvil, 0, blockUpdateFlag);
				break;
			case NORTH:
				setBlock(world, x, y, z, anvil, 1, blockUpdateFlag);
				break;
			case EAST:
				setBlock(world, x, y, z, anvil, 2, blockUpdateFlag);
				break;
			}
		}
	}

	private void setBed(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		Block bed = Blocks.bed;
		// logger.info("Entering setBed for (x,y,z)=(" + x + "," + y + "," + z
		// + ") and (i,j)=(" + i + "," + j + ")");
		List<Integer> openSidesThisPiece = findOpenSides(blocks, i, j);
		List<Integer> otherBedPiece = findNeighbors(blocks, 'H', i, j);
		// logger.info(openSidesThisPiece.size()
		// + " open sides for this block, and " + otherBedPiece.size()
		// + " bed piece ...");
		if (otherBedPiece.size() > 0) {
			// Contains specific location for other half.
			switch (otherBedPiece.get(0)) {
			case SOUTH:
				// SOUTH of this bed piece
				if (openSidesThisPiece.contains(NORTH)) {
					// logger.info("This is a foot open to the NORTH with head to the SOUTH.");
					// Assume this piece is foot, since it is open to NORTH
					setBlock(world, x, y, z, bed, 0, blockUpdateFlag);
					setBlock(world, x, y, z + 1, bed, 8, blockUpdateFlag);
				} else {
					// logger.info("This is a head blocked to the NORTH with foot to the SOUTH.");
					// Assume this piece is head, since it is blocked to NORTH
					setBlock(world, x, y, z, bed, 10, blockUpdateFlag);
					setBlock(world, x, y, z + 1, bed, 2, blockUpdateFlag);
				}
				break;
			case WEST:
				// WEST of this bed piece
				if (openSidesThisPiece.contains(EAST)) {
					// logger.info("This is a foot open to the EAST with head to the WEST.");
					// Assume this piece is foot, since it is open to WEST
					setBlock(world, x, y, z, bed, 1, blockUpdateFlag);
					setBlock(world, x - 1, y, z, bed, 9, blockUpdateFlag);
				} else {
					// logger.info("This is a head blocked to the EAST with foot to the WEST.");
					// Assume this piece is head, since it is blocked to WEST
					setBlock(world, x, y, z, bed, 11, blockUpdateFlag);
					setBlock(world, x - 1, y, z, bed, 3, blockUpdateFlag);
				}
				break;
			case NORTH:
			case EAST:
				// Only place entire bed from southernmost and westernmost
				// halves.
				break;
			default:
				logger.error("Received unexpected direction for other bed piece, "
						+ otherBedPiece.get(0) + ".");
				break;
			}
		}
	}

	private void setButton(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		List<Integer> stones = findNeighbors(blocks, 'S', i, j);
		if (stones.size() > 0) {
			Block btn = Blocks.wooden_button;
			switch (stones.get(0)) {
			case SOUTH:
				setBlock(world, x, y, z, btn, 4, blockUpdateFlag);
				break;
			case WEST:
				setBlock(world, x, y, z, btn, 1, blockUpdateFlag);
				break;
			case NORTH:
				setBlock(world, x, y, z, btn, 3, blockUpdateFlag);
				break;
			case EAST:
				setBlock(world, x, y, z, btn, 2, blockUpdateFlag);
				break;
			}
		}
	}

	private TileEntityChest setChestBlock(World world, int x, int y, int z,
			int i, int j, List<List<Character>> blocks) {
		Block chest = Blocks.chest;
		List<Integer> otherPiece = findNeighbors(blocks, 'c', i, j);
		List<Integer> openSides = findPreferredOpenSides(blocks, i, j);
		// Is this a lone chest, not a double chest?
		if (otherPiece.isEmpty()) {
			// Yes, this is a lone chest. Check for faces with walls opposite.
			if (openSides.isEmpty()) {
				// Set chest with default (i.e., no) metadata.
				return (TileEntityChest) setBlock(world, x, y, z, chest);
			}
			switch (openSides.get(0)) {
			case SOUTH:
				// logger.info("Setting chest open to SOUTH");
				return (TileEntityChest) setBlock(world, x, y, z, chest, 3,
						blockUpdateFlag);
			case WEST:
				// logger.info("Setting chest open to WEST");
				return (TileEntityChest) setBlock(world, x, y, z, chest, 4,
						blockUpdateFlag);
			case NORTH:
				// logger.info("Setting chest open to NORTH");
				return (TileEntityChest) setBlock(world, x, y, z, chest, 2,
						blockUpdateFlag);
			case EAST:
				// logger.info("Setting chest open to EAST");
				return (TileEntityChest) setBlock(world, x, y, z, chest, 5,
						blockUpdateFlag);
			}
		} else {
			int x2 = x;
			int z2 = z;
			int i2 = i;
			int j2 = j;
			List<Integer> openSides2 = null;
			switch (otherPiece.get(0)) {
			case SOUTH:
				// logger.info("Other chest half to SOUTH");
			case NORTH:
				// logger.info("Other chest half to NORTH");
				if (openSides.contains(WEST)) {
					// logger.info("Setting chest open to WEST");
					return (TileEntityChest) setBlock(world, x, y, z, chest, 4,
							blockUpdateFlag);
				} else {
					// logger.info("Setting chest open to EAST");
					return (TileEntityChest) setBlock(world, x, y, z, chest, 5,
							blockUpdateFlag);
				}
			case WEST:
				// logger.info("Other chest half to WEST");
			case EAST:
				// logger.info("Other chest half to EAST");
				if (openSides.contains(SOUTH)) {
					// logger.info("Setting chest open to SOUTH");
					return (TileEntityChest) setBlock(world, x, y, z, chest, 3,
							blockUpdateFlag);
				} else {
					// logger.info("Setting chest open to NORTH");
					return (TileEntityChest) setBlock(world, x, y, z, chest, 2,
							blockUpdateFlag);
				}
			}
		}
		// Should not get here.
		return (TileEntityChest) setBlock(world, x, y, z, chest);
	}

	private void setChest(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		TileEntityChest tec = setChestBlock(world, x, y, z, i, j, blocks);
		ItemStack stack = null;
		++this.numberOfChests;

		for (int slot = tec.getSizeInventory() - 1; slot >= 0; slot--) {
			switch (this.numberOfChests % 16) {
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
				stack = new ItemStack(Blocks.golden_rail, 64);
				break;
			case 13:
				stack = new ItemStack(Blocks.log, 64);
				break;
			case 14:
				stack = new ItemStack(Blocks.torch, 64);
				break;
			case 15:
				stack = new ItemStack(Blocks.ladder, 64);
				break;
			}
			if (stack != null) {
				tec.setInventorySlotContents(slot, stack);
			}
		}
		world.notifyBlockChange(x, y, z, Blocks.chest);
	}

	private void setDoor(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		Block door = Blocks.iron_door;
		List<Integer> panel = findNeighbors(blocks, 'p', i, j);
		// logger.info("setDoor: (x,y,z)=(" + x + "," + y + "," + z +
		// "), (i,j)=("
		// + i + "," + j + "), " + panel.size() + " wooden-panel sides.");
		if (panel.isEmpty()) {
			// logger.info("Setting basic door with no metadata and unknown orientation.");
			setBlock(world, x, y, z, door, 0, blockUpdateFlag);
		} else {
			switch (panel.get(0)) {
			case SOUTH:
				// logger.info("Setting door open to SOUTH");
				ItemDoor.placeDoorBlock(world, x, y, z, 1, door);
				break;
			case WEST:
				// logger.info("Setting door open to WEST");
				ItemDoor.placeDoorBlock(world, x, y, z, 2, door);
				break;
			case NORTH:
				// logger.info("Setting door open to NORTH");
				ItemDoor.placeDoorBlock(world, x, y, z, 3, door);
				break;
			case EAST:
				// logger.info("Setting door open to EAST");
				ItemDoor.placeDoorBlock(world, x, y, z, 0, door);
				break;
			}
		}
	}

	private void setFenceGate(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		List<Integer> openSides = findPreferredOpenSides(blocks, i, j);
		Block gate = Blocks.fence_gate;
		// logger.info("setgate: (x,y,z)=(" + x + "," + y + "," + z
		// + "), (i,j)=(" + i + "," + j + "), " + openSides.size()
		// + " open sides.");
		if (openSides.isEmpty()) {
			// logger.info("Setting default gate - no metadata.");
			setBlock(world, x, y, z, gate);
		} else {
			switch (openSides.get(0)) {
			case SOUTH:
			case NORTH:
				// logger.info("Setting gate open to NORTH-SOUTH");
				setBlock(world, x, y, z, gate, 2, blockUpdateFlag);
				break;
			case EAST:
			case WEST:
				// logger.info("Setting gate open to WEST");
				setBlock(world, x, y, z, gate, 3, blockUpdateFlag);
				break;
			}
		}
	}

	private void setFurnace(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		List<Integer> openSides = findOpenSides(blocks, i, j);
		Block furnace = Blocks.furnace;
		// logger.info("setFurnace: (x,y,z)=(" + x + "," + y + "," + z
		// + "), (i,j)=(" + i + "," + j + "), " + openSides.size()
		// + " open sides.");
		if (openSides.isEmpty()) {
			// logger.info("Setting default furnace - no metadata.");
			setBlock(world, x, y, z, furnace);
		} else {
			switch (openSides.get(0)) {
			case SOUTH:
				// logger.info("Setting furnace open to SOUTH");
				setBlock(world, x, y, z, furnace, 3, blockUpdateFlag);
				break;
			case WEST:
				// logger.info("Setting furnace open to WEST");
				setBlock(world, x, y, z, furnace, 4, blockUpdateFlag);
				break;
			case NORTH:
				// logger.info("Setting furnace open to NORTH");
				setBlock(world, x, y, z, furnace, 2, blockUpdateFlag);
				break;
			case EAST:
				// logger.info("Setting furnace open to EAST");
				setBlock(world, x, y, z, furnace, 5, blockUpdateFlag);
				break;
			}
		}
	}

	private void setLadder(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks, List<List<Character>> prevBlocks) {
		List<Integer> stones = findNeighbors(blocks, 'S', i, j);
		// logger.info("setLadder: (x,y,z)=(" + x + "," + y + "," + z
		// + "), (i,j)=(" + i + "," + j + "), " + stones.size()
		// + " neighboring stones.");
		if (!stones.isEmpty()) {
			Block ldr = Blocks.ladder;
			if (stones.size() == 4) {
				stones = findNeighbors(prevBlocks, 'S', i, j);
				// logger.info("setLadder: surrouned by stones. Checking previous layer. Found "
				// + stones.size()
				// + " neighboring stones on the layer below.");
				if (stones.isEmpty()) {
					return;
				}
			}
			switch (stones.get(0)) {
			case SOUTH:
				// logger.info("Setting ladder with block backing to the SOUTH");
				setBlock(world, x, y, z, ldr, 2, blockUpdateFlag);
				break;
			case WEST:
				// logger.info("Setting ladder with block backing to the WEST");
				setBlock(world, x, y, z, ldr, 5, blockUpdateFlag);
				break;
			case NORTH:
				// logger.info("Setting ladder with block backing to the NORTH");
				setBlock(world, x, y, z, ldr, 3, blockUpdateFlag);
				break;
			case EAST:
				// logger.info("Setting ladder with block backing to the EAST");
				setBlock(world, x, y, z, ldr, 4, blockUpdateFlag);
				break;
			}
		}
	}

	private void damBlock(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block.equals(Blocks.water) || block.equals(Blocks.lava)
				|| block.equals(Blocks.gravel) || block.equals(Blocks.sand)
				|| block.equals(Blocks.dirt)) {
			setBlock(world, x, y, z, Blocks.stone);
		}
	}

	private void setTunnel(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		List<Integer> openSides = findPreferredOpenSides(blocks, i, j);
		logger.info("setTunnel: (x,y,z)=(" + x + "," + y + "," + z
				+ "), (i,j)=(" + i + "," + j + "), " + openSides.size()
				+ " open sides.");
		int direction = EAST;
		int stepMeta = 1;
		int railMeta = 3;
		int poweredMeta = 11;
		int torchMeta = 1;
		int redTorchMeta = 1;
		if (!openSides.isEmpty()) {
			switch (openSides.get(0)) {
			case SOUTH:
				direction = NORTH;
				stepMeta = 2;
				railMeta = 5;
				poweredMeta = 13;
				torchMeta = 3;
				redTorchMeta = 4;
				break;
			case WEST:
				direction = EAST;
				stepMeta = 1;
				railMeta = 3;
				poweredMeta = 11;
				torchMeta = 2;
				redTorchMeta = 1;
				break;
			case NORTH:
				direction = SOUTH;
				stepMeta = 3;
				railMeta = 4;
				poweredMeta = 12;
				torchMeta = 4;
				redTorchMeta = 3;
				break;
			case EAST:
				direction = WEST;
				stepMeta = 0;
				railMeta = 2;
				poweredMeta = 10;
				torchMeta = 1;
				redTorchMeta = 2;
				break;
			}
		}
		int numSteps = 0;
		if ((direction == NORTH) || (direction == SOUTH)) {
			int zStep = (direction == SOUTH) ? 1 : -1;
			setBlock(world, x - 1, y, z - zStep, Blocks.golden_rail);
			setBlock(world, x + 1, y, z - zStep, Blocks.golden_rail);
			spawnEntity(world, new EntityMinecartEmpty(world), x - 1, y, z);
			spawnEntity(world, new EntityMinecartEmpty(world), x + 1, y, z);
			for (; y > 10; y--) {
				for (int u = 1; u < 3; u++) {
					setBlock(world, x - u, y - 1, z, Blocks.stonebrick);
					setBlock(world, x + u, y - 1, z, Blocks.stonebrick);
				}
				setBlock(world, x, y - 1, z, Blocks.stone_brick_stairs,
						stepMeta, blockUpdateFlag);
				for (int v = 0; v < 4; v++) {
					damBlock(world, x - 3, y + v, z);
					for (int u = -2; u < 3; u++) {
						world.setBlockToAir(x + u, y + v, z);
					}
					damBlock(world, x + 3, y + v, z);
				}
				for (int u = -3; u < 4; u++) {
					damBlock(world, x + u, y + 4, z);
				}
				switch (numSteps++ % 5) {
				case 1:
					for (int v = 0; v < 4; v++) {
						setBlock(world, x - 2, y + v, z,
								Blocks.nether_brick_fence);
						setBlock(world, x + 2, y + v, z,
								Blocks.nether_brick_fence);
					}
					for (int u = -2; u < 3; u++) {
						setBlock(world, x + u, y + 4, z, Blocks.nether_brick);
					}
					setBlock(world, x - 2, y + 4, z - zStep, Blocks.torch,
							torchMeta, blockUpdateFlag);
					setBlock(world, x + 2, y + 4, z - zStep, Blocks.torch,
							torchMeta, blockUpdateFlag);
				case 3:
					setBlock(world, x - 1, y, z, Blocks.rail, railMeta,
							blockUpdateFlag);
					setBlock(world, x + 1, y, z, Blocks.rail, railMeta,
							blockUpdateFlag);
					break;
				case 0:
				case 2:
				case 4:
					if (numSteps > 1) {
						setBlock(world, x - 2, y, z, Blocks.redstone_torch,
								redTorchMeta, blockUpdateFlag);
						setBlock(world, x + 2, y, z, Blocks.redstone_torch,
								redTorchMeta, blockUpdateFlag);
					}
					setBlock(world, x - 1, y, z, Blocks.golden_rail,
							poweredMeta, blockUpdateFlag);
					setBlock(world, x + 1, y, z, Blocks.golden_rail,
							poweredMeta, blockUpdateFlag);
					break;
				}
				z += zStep;
			}
		} else {
			int xStep = (direction == EAST) ? 1 : -1;
			setBlock(world, x - xStep, y, z - 1, Blocks.golden_rail);
			setBlock(world, x - xStep, y, z + 1, Blocks.golden_rail);
			spawnEntity(world, new EntityMinecartEmpty(world), x, y, z - 1);
			spawnEntity(world, new EntityMinecartEmpty(world), x, y, z + 1);
			for (; y > 10; y--) {
				for (int w = 1; w < 3; w++) {
					setBlock(world, x, y - 1, z - w, Blocks.stonebrick);
					setBlock(world, x, y - 1, z + w, Blocks.stonebrick);
				}
				setBlock(world, x, y - 1, z, Blocks.stone_brick_stairs,
						stepMeta, blockUpdateFlag);
				for (int v = 0; v < 4; v++) {
					damBlock(world, x, y + v, z - 3);
					for (int w = -2; w < 3; w++) {
						world.setBlockToAir(x, y + v, z + w);
					}
					damBlock(world, x, y + v, z + 3);
				}
				for (int w = -3; w < 4; w++) {
					damBlock(world, x, y + 4, z + w);
				}
				switch (numSteps++ % 5) {
				case 1:
					for (int v = 0; v < 4; v++) {
						setBlock(world, x, y + v, z - 2,
								Blocks.nether_brick_fence);
						setBlock(world, x, y + v, z + 2,
								Blocks.nether_brick_fence);
					}
					for (int w = -2; w < 3; w++) {
						setBlock(world, x, y + 4, z + w, Blocks.nether_brick);
					}
					setBlock(world, x - xStep, y + 4, z - 2, Blocks.torch,
							torchMeta, blockUpdateFlag);
					setBlock(world, x - xStep, y + 4, z + 2, Blocks.torch,
							torchMeta, blockUpdateFlag);
				case 3:
					setBlock(world, x, y, z - 1, Blocks.rail, railMeta,
							blockUpdateFlag);
					setBlock(world, x, y, z + 1, Blocks.rail, railMeta,
							blockUpdateFlag);
					break;
				case 0:
				case 2:
				case 4:
					if (numSteps > 1) {
						setBlock(world, x, y, z - 2, Blocks.redstone_torch,
								redTorchMeta, blockUpdateFlag);
						setBlock(world, x, y, z + 2, Blocks.redstone_torch,
								redTorchMeta, blockUpdateFlag);
					}
					setBlock(world, x, y, z - 1, Blocks.golden_rail,
							poweredMeta, blockUpdateFlag);
					setBlock(world, x, y, z + 1, Blocks.golden_rail,
							poweredMeta, blockUpdateFlag);
					break;
				}
				x += xStep;
			}
		}
	}

	private void setLayer1(World world, int x, int y, int z,
			List<List<Character>> blocks, List<List<Character>> prevBlocks) {

		// place everything but torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				char b = row.get(i);
				switch (b) {
				case 'a':
					setAnvil(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'b':
					setBlock(world, x + i, y, z + j, Blocks.brewing_stand);
					break;
				case 'B':
					setBlock(world, x + i, y, z + j, Blocks.bookshelf);
					break;
				case 'c':
					// Set on next pass.
					break;
				case 'C':
					setBlock(world, x + i, y, z + j, Blocks.crafting_table);
					break;
				case 'd':
					setBlock(world, x + i, y, z + j, Blocks.dirt);
					break;
				case 'D':
					// Set on next pass.
					break;
				case 'e':
					setBlock(world, x + i, y, z + j, Blocks.emerald_block);
					break;
				case 'E':
					setBlock(world, x + i, y, z + j, Blocks.enchanting_table);
					break;
				case 'f':
					setBlock(world, x + i, y, z + j, Blocks.farmland);
					break;
				case 'F':
					// Set on next pass.
					break;
				case 'j':
					setBlock(world, x + i, y, z + j, Blocks.glowstone);
					break;
				case 'J':
					setBlock(world, x + i, y, z + j, Blocks.pumpkin_stem, 7,
							blockUpdateFlag);
					break;
				case 'g':
					setBlock(world, x + i, y, z + j, Blocks.glass_pane);
					break;
				case 'G':
					setBlock(world, x + i, y, z + j, Blocks.reeds, 0,
							blockUpdateFlag);
					break;
				case 'H':
					setBed(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'l':
					// Set on next pass.
					break;
				case 'L':
					setBlock(world, x + i, y, z + j, Blocks.waterlily);
					break;
				case 'M':
					setBlock(world, x + i, y, z + j, Blocks.melon_stem, 7,
							blockUpdateFlag);
					break;
				case 'o':
					setBlock(world, x + i, y, z + j, Blocks.obsidian);
					break;
				case 'p':
					// Set on next pass.
					break;
				case 'P':
					setBlock(world, x + i, y, z + j, Blocks.potatoes, 7,
							blockUpdateFlag);
					break;
				case 'q':
					setBlock(world, x + i, y, z + j, Blocks.diamond_block);
					break;
				case 'Q':
					setBlock(world, x + i, y, z + j, Blocks.beacon);
					break;
				case 'r':
					setBlock(world, x + i, y, z + j, Blocks.carpet, 14,
							blockUpdateFlag);
					break;
				case 'R':
					setBlock(world, x + i, y, z + j, Blocks.carrots, 7,
							blockUpdateFlag);
					break;
				case 's':
					setBlock(world, x + i, y, z + j, Blocks.stone);
					break;
				case 'S':
					setBlock(world, x + i, y, z + j, Blocks.stonebrick);
					break;
				case 't':
					// Set on next pass.
					break;
				case 'T':
					// Set on next pass.
					break;
				case 'u':
					setButton(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'w':
					setBlock(world, x + i, y, z + j, Blocks.water, 0,
							blockUpdateFlag);
					break;
				case 'W':
					setBlock(world, x + i, y, z + j, Blocks.wheat, 7,
							blockUpdateFlag);
					break;
				case '+':
					setBlock(world, x + i, y, z + j, Blocks.fence);
					break;
				case '=':
					setFenceGate(world, x + i, y, z + j, i, j, blocks);
					break;
				case '.':
					// Do nothing - NO-OP.
					break;
				default:
					world.setBlockToAir(x + i, y, z + j);
					break;
				}
			}
		}
	}

	private void setLayer2(World world, int x, int y, int z,
			List<List<Character>> blocks, List<List<Character>> prevBlocks) {

		// place torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				char b = row.get(i);
				switch (b) {
				case 'c':
					setChest(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'D':
					setDoor(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'F':
					setFurnace(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'l':
					setLadder(world, x + i, y, z + j, i, j, blocks, prevBlocks);
					break;
				case 'p':
					setBlock(world, x + i, y, z + j,
							Blocks.wooden_pressure_plate, 0, blockUpdateFlag);
					break;
				case 't':
					if (world.isSideSolid(x + i, y - 1, z + j,
							ForgeDirection.UP, false)) {
						setBlock(world, x + i, y, z + j, Blocks.torch);
					} else {
						setBlock(world, x + i, y, z + j, Blocks.torch);
					}
					break;
				case 'T':
					setTunnel(world, x + i, y, z + j, i, j, blocks);
					break;
				default:
					// Do nothing.
					break;
				}
			}
		}

	}

	private Entity spawnEntity(World world, Entity entity, int x, int y, int z) {
		entity.setPosition(x, y + 1, z);
		world.spawnEntityInWorld(entity);
		return entity;
	}

	private TileEntity setBlock(World world, int x, int y, int z, Block block) {
		// logger.info("setBlock: " + x + "," + y + "," + z + " - " +
		// block.getLocalizedName());
		world.setBlock(x, y, z, block);
		world.notifyBlockChange(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	private TileEntity setBlock(World world, int x, int y, int z, Block block,
			int metadata, int flag) {
		// logger.info("setBlock: " + x + "," + y + "," + z + " - " +
		// block.getLocalizedName());

		// Flag values:
		// 1 - Cause a block update.
		// 2 - Send the change to clients (you almost always want this).
		// 4 - Prevents the block from being re-rendered, if this is a client
		// world.
		// Flags can be added together.
		world.setBlock(x, y, z, block, metadata, flag);
		world.notifyBlockChange(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	private void loadConfigFile() {

		InputStream is = null;
		BufferedReader in = null;
		Pattern isComment = Pattern.compile("^\\s*#");
		Pattern isLayer = Pattern.compile("^\\s*Layer\\s*(.*?):?$");
		Matcher m = null;
		List<List<Character>> rows = null;
		List<Character> row = null;
		int x, y, layerNumber;
		String line, layer = null;
		boolean foundGroundLevel = false;

		layerDefs = new HashMap<String, List<List<Character>>>();
		layerStack = new ArrayList<String>();

		try {
			is = InstaTower.class.getResourceAsStream("/assets/"
					+ Constants.MODID + "/schematics/instatower.cfg");
			in = new BufferedReader(new InputStreamReader(is));

			layerNumber = 0;
			while ((line = in.readLine()) != null) {
				if (!isComment.matcher(line).matches()) {
					m = isLayer.matcher(line);
					if (m.matches()) {
						layerNumber++;
						layer = m.group(1);
						layerStack.add(layer);
						if (!layerDefs.containsKey(layer)) {
							// (Re)defining new layer.
							rows = new ArrayList<List<Character>>();
							layerDefs.put(layer, rows);
						}
					} else if (layer != null) {
						row = new ArrayList<Character>();
						for (char c : line.toCharArray()) {
							row.add(c);
							if (c == 'd')
								groundLevel = layerNumber;
						}
						rows.add(row);
					}

				}
			}
			// Find maximum length of all rows for all layers
			int maxRowLength = 0;
			int maxNumberOfRows = 0;
			for (List<List<Character>> rs : layerDefs.values()) {
				for (List<Character> r : rs) {
					int sz = r.size();
					if (sz > maxRowLength)
						maxRowLength = sz;
				}
				int sz = rs.size();
				if (sz > maxNumberOfRows)
					maxNumberOfRows = sz;
			}
			// Normalize: Pad all rows for all layers with AIR (or STONE beneath
			// ground level) to maximum row length.
			layerNumber = 0;
			for (String layerName : layerStack) {
				layerNumber++;
				List<List<Character>> rs = layerDefs.get(layerName);
				for (int j = rs.size(); j < maxNumberOfRows; j++) {
					rs.add((List) new ArrayList<Blocks>());
				}
				for (List<Character> r : rs) {
					for (int i = r.size(); i < maxRowLength; i++) {
						if (layerNumber < groundLevel) {
							r.add('s');
						} else {
							r.add(' ');
						}
					}
				}
			}
			if (groundLevel > 0)
				groundLevel--;
		} catch (IOException e) {
			logger.catching(e);
			logger.error(e.getStackTrace());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
