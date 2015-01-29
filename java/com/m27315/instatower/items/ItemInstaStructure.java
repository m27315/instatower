package com.m27315.instatower.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
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

import org.apache.logging.log4j.Logger;

import com.m27315.instatower.InstaTower;

public class ItemInstaStructure extends Item {
	protected String name;
	protected String schematic;
	protected int groundLevel = 0;
	protected int numberOfBeacons = 0;
	protected int numberOfChests = 0;
	protected int blockUpdateFlag = 3;
	protected Logger logger;
	protected HashMap<String, List<List<Character>>> layerDefs;
	protected List<String> layerStack;

	// Configuration variables.
	protected boolean hutChest = true;
	protected boolean towerAnvil = true;
	protected boolean towerBasement = true;
	protected boolean towerBeacon = true;
	protected boolean towerBrewing = true;
	protected boolean towerChests = true;
	protected boolean towerLibrary = true;
	protected boolean towerRails = true;
	protected boolean tunnelEnable = true;
	protected int tunnelDepth = 6;
	protected int tunnelLength = 501;
	protected boolean tunnelClear = false;
	protected boolean tunnelRails = true;
	protected boolean gardenAnimals = true;

	// CONSTANTS
	protected static final int SOUTH = 0;
	protected static final int WEST = 1;
	protected static final int NORTH = 2;
	protected static final int EAST = 3;

	// CONVENIENCE
	protected static Block air = Blocks.air;
	protected static Block anvil = Blocks.anvil;
	protected static Block beacon = Blocks.beacon;
	protected static Block bed = Blocks.bed;
	protected static Block bedrock = Blocks.bedrock;
	protected static Block brewingStand = Blocks.brewing_stand;
	protected static Block brick = Blocks.stonebrick;
	protected static Block bookshelf = Blocks.bookshelf;
	protected static Block button = Blocks.wooden_button;
	protected static Block carpet = Blocks.carpet;
	protected static Block carrots = Blocks.carrots;
	protected static Block chest = Blocks.chest;
	protected static Block cobblestone = Blocks.cobblestone;
	protected static Block cTable = Blocks.crafting_table;
	protected static Block diamond = Blocks.diamond_block;
	protected static Block dirt = Blocks.dirt;
	protected static Block door = Blocks.iron_door;
	protected static Block wDoor = Blocks.wooden_door;
	protected static Block emerald = Blocks.emerald_block;
	protected static Block eStone = Blocks.end_stone;
	protected static Block eTable = Blocks.enchanting_table;
	protected static Block farmland = Blocks.farmland;
	protected static Block fence = Blocks.fence;
	protected static Block furnace = Blocks.furnace;
	protected static Block gate = Blocks.fence_gate;
	protected static Block glass = Blocks.glass;
	protected static Block glow = Blocks.glowstone;
	protected static Block grass = Blocks.grass;
	protected static Block ladder = Blocks.ladder;
	protected static Block log = Blocks.log;
	protected static Block mStem = Blocks.melon_stem;
	protected static Block nBrick = Blocks.nether_brick;
	protected static Block nFence = Blocks.nether_brick_fence;
	protected static Block obsidian = Blocks.obsidian;
	protected static Block pane = Blocks.glass_pane;
	protected static Block plate = Blocks.wooden_pressure_plate;
	protected static Block potatoes = Blocks.potatoes;
	protected static Block pRail = Blocks.golden_rail;
	protected static Block pStem = Blocks.pumpkin_stem;
	protected static Block rail = Blocks.rail;
	protected static Block redBlock = Blocks.redstone_block;
	protected static Block redTorch = Blocks.redstone_torch;
	protected static Block reeds = Blocks.reeds;
	protected static Block stairs = Blocks.stone_brick_stairs;
	protected static Block stone = Blocks.stone;
	protected static Block torch = Blocks.torch;
	protected static Block water = Blocks.water;
	protected static Block waterlily = Blocks.waterlily;
	protected static Block wheat = Blocks.wheat;
	protected static Block wool = Blocks.wool;

	public boolean setStructure(ItemStack stack, EntityPlayer player,
			World world, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ, boolean setAnimals) {

		if (!world.isRemote && side == 1
				&& player.canPlayerEdit(x, y + 1, z, side, stack)) {

			// Don't consume item, if in creative mode.
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}

			// Direction: 0=South, 1=West, 2=North, 3=East
			int facingDirection = MathHelper
					.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;

			switch (world.provider.dimensionId) {
			case -1:
				// nether
				brick = nBrick;
				fence = nFence;
				pane = Blocks.iron_bars;
				stairs = Blocks.nether_brick_stairs;
				dirt = Blocks.netherrack;
				stone = Blocks.netherrack;
				break;
			case 1:
				// end
				brick = obsidian;
				fence = nFence;
				pane = Blocks.iron_bars;
				stairs = Blocks.nether_brick_stairs;
				dirt = eStone;
				stone = eStone;
				break;
			default:
				// overworld
				brick = Blocks.stonebrick;
				fence = Blocks.fence;
				pane = Blocks.glass_pane;
				stairs = Blocks.stone_brick_stairs;
				dirt = Blocks.dirt;
				stone = Blocks.stone;
			}

			return setStructure(world, x, y, z, facingDirection, setAnimals);
		}
		return false;
	}

	public boolean setStructure(World world, int x, int y, int z,
			int facingDirection, boolean setAnimals) {
		loadConfigFile();
		numberOfBeacons = 0;
		numberOfChests = 0;
		List<List<Character>> prevBlocks = null;

		// Offset structure to find red carpet entrance.
		Vec3 offset = findRedCarpet(facingDirection);
		x += (int) offset.xCoord;
		y -= groundLevel;
		z += (int) offset.zCoord;
		// Prepare site - assume first layer is biggest.
		List<List<Character>> blocks = layerDefs.get(layerStack.get(0));
		// Rotate structure according to direction faced.
		blocks = rotateBlocks(blocks, facingDirection);
		for (int j = 0; j < blocks.size(); j++) {
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				// Build up with dirt between this block down to ground.
				int k = y - 1;
				if (!towerBasement)
					k += groundLevel;
				for (; k >= 0; k--) {
					if (world.isAirBlock(x + i, k, z + j)) {
						setBlock(world, x + i, k, z + j, stone);
					} else {
						break;
					}
				}
				// Clear out blocks between this block and top of structure
				if (world.provider.dimensionId == -1) {
					// in nether, start at lowest block of bedrock.
					for (k = 1; k < 500; k++) {
						Block b = world.getBlock(x + i, y + k, z + j);
						if (b.equals(bedrock)) {
							k--;
							break;
						}
					}
				} else {
					// start at top of tower.
					k = layerStack.size();
				}
				for (; k > 0; k--) {
					if (towerBasement || k >= groundLevel) {
						Block b = world.getBlock(x + i, y + k, z + j);
						if (!(b.equals(air) || b.equals(torch)
								|| b.equals(brick) || b.equals(glow))) {
							world.setBlockToAir(x + i, y + k, z + j);
						}
					}
				}
			}
		}
		// Lay down each layer - basic blocks first!
		for (int n = 0; n < layerStack.size(); n++) {
			if (towerBasement || n >= groundLevel) {
				String layer = layerStack.get(n);
				blocks = rotateBlocks(layerDefs.get(layer), facingDirection);
				setLayer1(world, x, y + n, z, blocks, prevBlocks);
				prevBlocks = blocks;
			}
		}
		// Lay down each layer - dependent blocks next!
		prevBlocks = null;
		for (int n = 0; n < layerStack.size(); n++) {
			if (towerBasement || n >= groundLevel) {
				String layer = layerStack.get(n);
				blocks = rotateBlocks(layerDefs.get(layer), facingDirection);
				setLayer2(world, x, y + n, z, blocks, prevBlocks);
				prevBlocks = blocks;
			}
		}
		if (setAnimals && gardenAnimals) {
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
				spawnEntity(world, new EntityPig(world), x, y, z);
				spawnEntity(world, new EntitySheep(world), x, y, z);
			}
		}
		return true;
	}

	protected List<List<Character>> rotateBlocks(
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

	protected Vec3 findRedCarpet(int facingDirection) {
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

	protected List<Integer> findNeighbors(List<List<Character>> blocks,
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

	protected List<Integer> findOpenSides(List<List<Character>> blocks, int i,
			int j) {
		List<Integer> open = findNeighbors(blocks, ' ', i, j);
		if (!open.isEmpty())
			return open;
		// Consider lily-pads to also be open - just for garden fence gate.
		return findNeighbors(blocks, 'L', i, j);
	}

	protected List<Integer> findOpenSidesWithBlockedBacks(
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

	protected List<Integer> findPreferredOpenSides(
			List<List<Character>> blocks, int i, int j) {
		// Prefer open-sides that have something immediately behind them.
		List<Integer> openSides = findOpenSidesWithBlockedBacks(blocks, i, j);
		// If no preferred open-sides, return any open sides.
		if (openSides.isEmpty())
			return findOpenSides(blocks, i, j);
		// Return list of preferred.
		return openSides;
	}

	protected void setAnvil(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
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

	protected void setBed(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
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

	protected void setButton(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		List<Integer> stones = findNeighbors(blocks, 'S', i, j);
		if (stones.size() > 0) {
			switch (stones.get(0)) {
			case SOUTH:
				setBlock(world, x, y, z, button, 4, blockUpdateFlag);
				break;
			case WEST:
				setBlock(world, x, y, z, button, 1, blockUpdateFlag);
				break;
			case NORTH:
				setBlock(world, x, y, z, button, 3, blockUpdateFlag);
				break;
			case EAST:
				setBlock(world, x, y, z, button, 2, blockUpdateFlag);
				break;
			}
		}
	}

	protected TileEntityChest setChestBlock(World world, int x, int y, int z,
			int i, int j, List<List<Character>> blocks) {
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

	protected void setChest(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		TileEntityChest tec = setChestBlock(world, x, y, z, i, j, blocks);
		++this.numberOfChests;
		if (towerChests) {
			ItemStack stack = null;

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
					stack = new ItemStack(rail, 64);
					break;
				case 10:
					stack = new ItemStack(redTorch, 64);
					break;
				case 11:
					stack = new ItemStack(redBlock, 64);
					break;
				case 12:
					stack = new ItemStack(pRail, 64);
					break;
				case 13:
					stack = new ItemStack(log, 64);
					break;
				case 14:
					stack = new ItemStack(torch, 64);
					break;
				case 15:
					stack = new ItemStack(ladder, 64);
					break;
				}
				if (stack != null) {
					tec.setInventorySlotContents(slot, stack);
				}
			}
			world.notifyBlockChange(x, y, z, chest);
		}
	}

	protected void setDoor(World world, int x, int y, int z, int i, int j,
			Block door, List<List<Character>> blocks) {
		List<Integer> plate = findNeighbors(blocks, 'p', i, j);
		// logger.info("setDoor: (x,y,z)=(" + x + "," + y + "," + z +
		// "), (i,j)=("
		// + i + "," + j + "), " + panel.size() + " wooden-panel sides.");
		if (plate.isEmpty()) {
			// logger.info("Setting basic door with no metadata and unknown orientation.");
			setBlock(world, x, y, z, door, 0, blockUpdateFlag);
		} else {
			switch (plate.get(0)) {
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

	protected void setFenceGate(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		List<Integer> openSides = findPreferredOpenSides(blocks, i, j);
		// logger.info("setgate: (x,y,z)=(" + x + "," + y + "," + z +
		// "), (i,j)=("
		// + i + "," + j + "), " + openSides.size() + " open sides.");
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
				// logger.info("Setting gate open to EAST-WEST");
				setBlock(world, x, y, z, gate, 3, blockUpdateFlag);
				break;
			}
		}
	}

	protected void setFurnace(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {

		List<Integer> openSides = findOpenSides(blocks, i, j);
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

	protected void setLadder(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks, List<List<Character>> prevBlocks) {
		List<Integer> stones = findNeighbors(blocks, 'S', i, j);
		// logger.info("setLadder: (x,y,z)=(" + x + "," + y + "," + z
		// + "), (i,j)=(" + i + "," + j + "), " + stones.size()
		// + " neighboring stones.");
		if (!stones.isEmpty()) {
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
				setBlock(world, x, y, z, ladder, 2, blockUpdateFlag);
				break;
			case WEST:
				// logger.info("Setting ladder with block backing to the WEST");
				setBlock(world, x, y, z, ladder, 5, blockUpdateFlag);
				break;
			case NORTH:
				// logger.info("Setting ladder with block backing to the NORTH");
				setBlock(world, x, y, z, ladder, 3, blockUpdateFlag);
				break;
			case EAST:
				// logger.info("Setting ladder with block backing to the EAST");
				setBlock(world, x, y, z, ladder, 4, blockUpdateFlag);
				break;
			}
		}
	}

	protected void damBlock(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		String name = block.getLocalizedName();
		int yGround = world.getTopSolidOrLiquidBlock(x, z);
		if (!(name.endsWith(" Ore") || (y < yGround && world
				.isAirBlock(x, y, z)))) {
			setBlock(world, x, y, z, brick);
		}
	}

	protected void solidifyBlock(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		String name = block.getLocalizedName();
		if (!name.endsWith(" Ore")) {
			setBlock(world, x, y, z, brick);
		}
	}

	protected void vaporizeBlock(World world, int x, int y, int z) {
		if (tunnelClear) {
			world.setBlockToAir(x, y, z);
		} else {
			Block block = world.getBlock(x, y, z);
			String name = block.getLocalizedName();
			if (!(name.endsWith(" Ore") || world.isAirBlock(x, y, z))) {
				world.setBlockToAir(x, y, z);
			}
		}
	}

	protected void setTunnel(World world, int x, int y, int z, int i, int j,
			List<List<Character>> blocks) {
		List<Integer> openSides = findPreferredOpenSides(blocks, i, j);
		// logger.info("setTunnel: (x,y,z)=(" + x + "," + y + "," + z
		// + "), (i,j)=(" + i + "," + j + "), " + openSides.size()
		// + " open sides.");
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
			if (tunnelRails) {
				setBlock(world, x - 1, y, z - zStep, pRail);
				setBlock(world, x + 1, y, z - zStep, pRail);
				spawnEntity(world, new EntityMinecartEmpty(world), x - 1, y, z);
			}
			for (; y > tunnelDepth; y--) {
				// Place floor with stairs.
				for (int u = 1; u < 3; u++) {
					setBlock(world, x - u, y - 1, z, brick);
					setBlock(world, x + u, y - 1, z, brick);
				}
				setBlock(world, x, y - 1, z, stairs, stepMeta, blockUpdateFlag);
				// Setting walls and clearing tunnel.
				for (int v = 0; v < 4; v++) {
					damBlock(world, x - 3, y + v, z);
					for (int u = -2; u < 3; u++) {
						vaporizeBlock(world, x + u, y + v, z);
					}
					damBlock(world, x + 3, y + v, z);
				}
				// Place ceiling.
				for (int u = -3; u < 4; u++) {
					damBlock(world, x + u, y + 4, z);
				}
				switch (numSteps++ % 5) {
				case 1:
					for (int v = 0; v < 4; v++) {
						setBlock(world, x - 2, y + v, z, nFence);
						setBlock(world, x + 2, y + v, z, nFence);
					}
					for (int u = -2; u < 3; u++) {
						setBlock(world, x + u, y + 4, z, nBrick);
					}
					setBlock(world, x - 2, y + 4, z - zStep, torch, torchMeta,
							blockUpdateFlag);
					setBlock(world, x + 2, y + 4, z - zStep, torch, torchMeta,
							blockUpdateFlag);
				case 3:
					if (tunnelRails) {
						setBlock(world, x - 1, y, z, rail, railMeta,
								blockUpdateFlag);
						setBlock(world, x + 1, y, z, rail, railMeta,
								blockUpdateFlag);
					}
					break;
				case 0:
				case 2:
				case 4:
					if (tunnelRails) {
						if (numSteps > 1) {
							setBlock(world, x - 2, y, z, redTorch,
									redTorchMeta, blockUpdateFlag);
							setBlock(world, x + 2, y, z, redTorch,
									redTorchMeta, blockUpdateFlag);
						}
						setBlock(world, x - 1, y, z, pRail, poweredMeta,
								blockUpdateFlag);
						setBlock(world, x + 1, y, z, pRail, poweredMeta,
								blockUpdateFlag);
					}
					break;
				}
				z += zStep;
			}
			for (int w = 0; w < 5; w++) {
				for (int u = -2; u < 3; u++) {
					setBlock(world, x + u, y - 1, z + w * zStep, brick);
				}
				for (int v = 0; v < 4; v++) {
					damBlock(world, x - 3, y + v, z + w * zStep);
					for (int u = -2; u < 3; u++) {
						vaporizeBlock(world, x + u, y + v, z + w * zStep);
					}
					damBlock(world, x + 3, y + v, z + w * zStep);
				}
				for (int u = -3; u < 4; u++) {
					damBlock(world, x + u, y + 4, z + w * zStep);
				}
				if (tunnelRails) {
					if (w < 2) {
						setBlock(world, x + 1, y, z + w * zStep, rail);
						setBlock(world, x - 1, y, z + w * zStep, rail);
					}
				}
			}
			for (int u = -2; u < 3; u++) {
				for (int v = -1; v < 4; v++) {
					damBlock(world, x + u, y + v, z + 5 * zStep);
				}
			}
			z += 2 * zStep;
			if (direction == SOUTH) {
				for (int u = 3; u < tunnelLength; u++) {
					setTunnelSliceEW(world, x + u, y, z, u);
				}
				if (tunnelRails) {
					setBlock(world, x + 2, y, z - zStep, rail);
					setBlock(world, x + 2, y, z + zStep, rail);
					setBlock(world, x + 1, y, z + zStep, rail);
					setBlock(world, x + 0, y, z + zStep, rail);
					setBlock(world, x - 1, y, z + zStep, rail);
					setBlock(world, x - 1, y, z, rail);
					setBlock(world, x + tunnelLength - 1, y, z, rail);
				}
			} else {
				if (tunnelRails) {
					setBlock(world, x - 2, y, z - zStep, rail);
					setBlock(world, x - 2, y, z + zStep, rail);
					setBlock(world, x - 1, y, z + zStep, rail);
					setBlock(world, x - 0, y, z + zStep, rail);
					setBlock(world, x + 1, y, z + zStep, rail);
					setBlock(world, x + 1, y, z, rail);
					setBlock(world, x - (tunnelLength - 1), y, z, rail);
				}
				for (int u = 3; u < tunnelLength; u++) {
					setTunnelSliceEW(world, x - u, y, z, u);
				}
			}
		} else {
			int xStep = (direction == EAST) ? 1 : -1;
			if (tunnelRails) {
				setBlock(world, x - xStep, y, z - 1, pRail);
				setBlock(world, x - xStep, y, z + 1, pRail);
				spawnEntity(world, new EntityMinecartEmpty(world), x, y, z - 1);
			}
			for (; y > tunnelDepth; y--) {
				for (int w = 1; w < 3; w++) {
					setBlock(world, x, y - 1, z - w, brick);
					setBlock(world, x, y - 1, z + w, brick);
				}
				setBlock(world, x, y - 1, z, stairs, stepMeta, blockUpdateFlag);
				for (int v = 0; v < 4; v++) {
					damBlock(world, x, y + v, z - 3);
					for (int w = -2; w < 3; w++) {
						vaporizeBlock(world, x, y + v, z + w);
					}
					damBlock(world, x, y + v, z + 3);
				}
				for (int w = -3; w < 4; w++) {
					damBlock(world, x, y + 4, z + w);
				}
				switch (numSteps++ % 5) {
				case 1:
					for (int v = 0; v < 4; v++) {
						setBlock(world, x, y + v, z - 2, nFence);
						setBlock(world, x, y + v, z + 2, nFence);
					}
					for (int w = -2; w < 3; w++) {
						setBlock(world, x, y + 4, z + w, nBrick);
					}
					setBlock(world, x - xStep, y + 4, z - 2, torch, torchMeta,
							blockUpdateFlag);
					setBlock(world, x - xStep, y + 4, z + 2, torch, torchMeta,
							blockUpdateFlag);
				case 3:
					if (tunnelRails) {
						setBlock(world, x, y, z - 1, rail, railMeta,
								blockUpdateFlag);
						setBlock(world, x, y, z + 1, rail, railMeta,
								blockUpdateFlag);
					}
					break;
				case 0:
				case 2:
				case 4:
					if (tunnelRails) {
						if (numSteps > 1) {
							setBlock(world, x, y, z - 2, redTorch,
									redTorchMeta, blockUpdateFlag);
							setBlock(world, x, y, z + 2, redTorch,
									redTorchMeta, blockUpdateFlag);
						}
						setBlock(world, x, y, z - 1, pRail, poweredMeta,
								blockUpdateFlag);
						setBlock(world, x, y, z + 1, pRail, poweredMeta,
								blockUpdateFlag);
					}
					break;
				}
				x += xStep;
			}
			for (int u = 0; u < 5; u++) {
				// set floor.
				for (int w = -2; w < 3; w++) {
					solidifyBlock(world, x + u * xStep, y - 1, z + w);
				}
				// set walls and clear central.
				for (int v = 0; v < 4; v++) {
					damBlock(world, x + u * xStep, y + v, z - 3);
					for (int w = -2; w < 3; w++) {
						vaporizeBlock(world, x + u * xStep, y + v, z + w);
					}
					damBlock(world, x + u * xStep, y + v, z + 3);
				}
				if (tunnelRails) {
					if (u < 2) {
						setBlock(world, x + u * xStep, y, z + 1, rail);
						setBlock(world, x + u * xStep, y, z - 1, rail);
					}
				}
				// set ceiling.
				for (int w = -3; w < 4; w++) {
					damBlock(world, x + u * xStep, y + 4, z + w);
				}
			}
			// set far wall.
			for (int w = -2; w < 3; w++) {
				for (int v = -1; v < 4; v++) {
					damBlock(world, x + 5 * xStep, y + v, z + w);
				}
			}
			x += 2 * xStep;
			if (direction == EAST) {
				for (int w = 3; w < tunnelLength; w++) {
					setTunnelSliceNS(world, x, y, z - w, w);
				}
				if (tunnelRails) {
					setBlock(world, x - xStep, y, z - 2, rail);
					setBlock(world, x + xStep, y, z - 2, rail);
					setBlock(world, x + xStep, y, z - 1, rail);
					setBlock(world, x + xStep, y, z - 0, rail);
					setBlock(world, x + xStep, y, z + 1, rail);
					setBlock(world, x, y, z + 1, rail);
					setBlock(world, x, y, z - (tunnelLength - 1), rail);
				}
			} else {
				for (int w = 3; w < tunnelLength; w++) {
					setTunnelSliceNS(world, x, y, z + w, w);
				}
				if (tunnelRails) {
					setBlock(world, x - xStep, y, z + 2, rail);
					setBlock(world, x + xStep, y, z + 2, rail);
					setBlock(world, x + xStep, y, z + 1, rail);
					setBlock(world, x + xStep, y, z + 0, rail);
					setBlock(world, x + xStep, y, z - 1, rail);
					setBlock(world, x, y, z - 1, rail);
					setBlock(world, x, y, z + tunnelLength - 1, rail);
				}
			}
		}
	}

	protected void setTunnelSliceNS(World world, int x, int y, int z, int s) {
		int h = 5;
		// Build floor and ceiling.
		for (int u = -3; u <= 3; u++) {
			solidifyBlock(world, x + u, y - 1, z);
			solidifyBlock(world, x + u, y + h, z);
		}
		// Build walls and clear tunnel.
		for (int v = 0; v < h; v++) {
			damBlock(world, x - 3, y + v, z);
			for (int u = -2; u <= 2; u++) {
				vaporizeBlock(world, x + u, y + v, z);
			}
			damBlock(world, x + 3, y + v, z);
		}
		switch (s % 4) {
		case 3:
			if (tunnelRails) {
				setBlock(world, x - 1, y, z, pRail);
				setBlock(world, x, y, z, redTorch, 5, blockUpdateFlag);
				setBlock(world, x + 1, y, z, pRail);
			}
			setBlock(world, x, y + h - 1, z, glow);
			break;
		case 1:
			setBlock(world, x, y, z, torch, 5, blockUpdateFlag);
			// Include tunnel supports.
			for (int v = 0; v < h - 1; v++) {
				setBlock(world, x - 2, y + v, z, nFence);
				setBlock(world, x + 2, y + v, z, nFence);
			}
			for (int u = -2; u <= 2; u++) {
				setBlock(world, x + u, y + h - 1, z, nBrick);
			}
		default:
			if (tunnelRails) {
				setBlock(world, x - 1, y, z, rail);
				setBlock(world, x + 1, y, z, rail);
			}
		}
	}

	protected void setTunnelSliceEW(World world, int x, int y, int z, int s) {
		int h = 5;
		// Build walls and clear tunnel.
		for (int v = 0; v < h; v++) {
			damBlock(world, x, y + v, z - 3);
			for (int w = -2; w <= 2; w++) {
				vaporizeBlock(world, x, y + v, z + w);
			}
			damBlock(world, x, y + v, z + 3);
		}
		// Build floor and ceiling.
		for (int w = -3; w <= 3; w++) {
			solidifyBlock(world, x, y - 1, z + w);
			solidifyBlock(world, x, y + h, z + w);
		}
		switch (s % 4) {
		case 3:
			if (tunnelRails) {
				setBlock(world, x, y, z - 1, pRail);
				setBlock(world, x, y, z, redTorch, 5, blockUpdateFlag);
				setBlock(world, x, y, z + 1, pRail);
			}
			setBlock(world, x, y + h - 1, z, glow);
			break;
		case 1:
			// Include tunnel supports.
			for (int v = 0; v < h - 1; v++) {
				setBlock(world, x, y + v, z - 2, nFence);
				setBlock(world, x, y + v, z + 2, nFence);
			}
			for (int w = -2; w <= 2; w++) {
				setBlock(world, x, y + h - 1, z + w, nBrick);
			}
			setBlock(world, x, y, z, torch, 5, blockUpdateFlag);
		default:
			if (tunnelRails) {
				setBlock(world, x, y, z - 1, rail);
				setBlock(world, x, y, z + 1, rail);
			}
		}
	}

	protected void setLayer1(World world, int x, int y, int z,
			List<List<Character>> blocks, List<List<Character>> prevBlocks) {

		// place everything but torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				char b = row.get(i);
				switch (b) {
				case 'a':
					if (towerAnvil)
						setAnvil(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'b':
					if (towerBrewing)
						setBlock(world, x + i, y, z + j, brewingStand);
					break;
				case 'B':
					if (towerLibrary)
						setBlock(world, x + i, y, z + j, bookshelf);
					break;
				case 'c':
					// Set on next pass.
					break;
				case 'C':
					setBlock(world, x + i, y, z + j, cTable);
					break;
				case 'd':
					setBlock(world, x + i, y, z + j, dirt);
					break;
				case 'D':
					// Set on next pass.
					break;
				case 'e':
					if (towerBeacon)
						setBlock(world, x + i, y, z + j, emerald);
					break;
				case 'E':
					if (towerLibrary)
						setBlock(world, x + i, y, z + j, eTable);
					break;
				case 'f':
					setBlock(world, x + i, y, z + j, farmland);
					break;
				case 'F':
					// Set on next pass.
					break;
				case 'j':
					setBlock(world, x + i, y, z + j, glow);
					break;
				case 'J':
					setBlock(world, x + i, y, z + j, pStem, 7, blockUpdateFlag);
					break;
				case 'g':
					setBlock(world, x + i, y, z + j, pane);
					break;
				case 'G':
					setBlock(world, x + i, y, z + j, reeds, 0, blockUpdateFlag);
					break;
				case 'H':
					setBed(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'l':
					// Set on next pass.
					break;
				case 'L':
					setBlock(world, x + i, y, z + j, waterlily);
					break;
				case 'M':
					setBlock(world, x + i, y, z + j, mStem, 7, blockUpdateFlag);
					break;
				case 'o':
					setBlock(world, x + i, y, z + j, obsidian);
					break;
				case 'p':
					// Set on next pass.
					break;
				case 'P':
					setBlock(world, x + i, y, z + j, potatoes, 7,
							blockUpdateFlag);
					break;
				case 'q':
					if (towerBeacon)
						setBlock(world, x + i, y, z + j, diamond);
					break;
				case 'Q':
					if (towerBeacon)
						setBlock(world, x + i, y, z + j, beacon);
					break;
				case 'r':
					setBlock(world, x + i, y, z + j, carpet, 14,
							blockUpdateFlag);
					break;
				case 'R':
					setBlock(world, x + i, y, z + j, carrots, 7,
							blockUpdateFlag);
					break;
				case 's':
					setBlock(world, x + i, y, z + j, stone);
					break;
				case 'S':
					setBlock(world, x + i, y, z + j, brick);
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
					setBlock(world, x + i, y, z + j, water, 0, blockUpdateFlag);
					break;
				case 'W':
					setBlock(world, x + i, y, z + j, wheat, 7, blockUpdateFlag);
					break;
				case '*':
					setBlock(world, x + i, y, z + j, grass);
					break;
				case '+':
					setBlock(world, x + i, y, z + j, fence);
					break;
				case '-':
					setFenceGate(world, x + i, y, z + j, i, j, blocks);
					break;
				case '=':
					if (towerRails)
						setBlock(world, x + i, y, z + j, rail);
					break;
				case '.':
					// Do nothing - NO-OP.
					break;
				default:
					Block B = world.getBlock(x + i, y, z + j);
					if (!(B.equals(air) || B.equals(torch) || B.equals(brick))) {
						world.setBlockToAir(x + i, y, z + j);
					}
					break;
				}
			}
		}
	}

	protected void setLayer2(World world, int x, int y, int z,
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
					setDoor(world, x + i, y, z + j, i, j, door, blocks);
					break;
				case 'F':
					setFurnace(world, x + i, y, z + j, i, j, blocks);
					break;
				case 'l':
					if (prevBlocks != null) {
						setLadder(world, x + i, y, z + j, i, j, blocks,
								prevBlocks);
					} else {
						setBlock(world, x + i, y, z + j, brick);
					}
					break;
				case 'p':
					setBlock(world, x + i, y, z + j, plate, 0, blockUpdateFlag);
					break;
				case 't':
					if (world.isSideSolid(x + i, y - 1, z + j,
							ForgeDirection.UP, false)) {
						setBlock(world, x + i, y, z + j, torch);
					} else {
						setBlock(world, x + i, y, z + j, torch);
					}
					break;
				case 'T':
					if (tunnelEnable)
						setTunnel(world, x + i, y, z + j, i, j, blocks);
					break;
				case '[':
					setDoor(world, x + i, y, z + j, i, j, wDoor, blocks);
					break;
				default:
					// Do nothing.
					break;
				}
			}
		}

	}

	protected Entity spawnEntity(World world, Entity entity, int x, int y, int z) {
		entity.setPosition(x, y + 1, z);
		world.spawnEntityInWorld(entity);
		return entity;
	}

	protected TileEntity setBlock(World world, int x, int y, int z, Block block) {
		// logger.info("setBlock: " + x + "," + y + "," + z + " - " +
		// block.getLocalizedName());
		world.setBlock(x, y, z, block);
		world.notifyBlockChange(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	protected TileEntity setBlock(World world, int x, int y, int z,
			Block block, int metadata, int flag) {
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

	protected void loadConfigFile() {

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
			is = InstaTower.class.getResourceAsStream(schematic);
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
