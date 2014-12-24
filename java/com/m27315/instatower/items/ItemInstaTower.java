package com.m27315.instatower.items;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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
	private int numberOfChests = 0;
	public static Logger logger;
	private static File configFile;
	public static HashMap<String, List<List<Character>>> layerDefs;
	public static List<String> layerStack;

	public ItemInstaTower(FMLPreInitializationEvent event, Logger logger) {
		this.logger = logger;
		configFile = event.getSuggestedConfigurationFile();
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

			loadConfigFile();

			// Direction: 0=South, 1=West, 2=North, 3=East
			int facingDirection = MathHelper
					.floor_double((double) ((player.rotationYaw * 4F) / 360F) + 0.5D) & 3;
			InstaTower.logger.info("onItemUseFirst: stack=" + stack
					+ "\n    x=" + x + ", y=" + y + ", z=" + z + ", side="
					+ side + "\n    hitX=" + hitX + ", hitY=" + hitY
					+ ", hitZ=" + hitZ);
			// reportMetaData(world, -472, 78, 404);
			InstaTower.logger.info("Look Vector=" + player.getLookVec()
					+ ", CameraPitch=" + player.cameraPitch + ", CameraYaw="
					+ player.cameraYaw + ", rotationPitch="
					+ player.rotationPitch + ", rotationYaw="
					+ player.rotationYaw + ", rotationHeadYaw="
					+ player.rotationYawHead + ", facing=" + facingDirection);
			// Offset structure to find red carpet entrance.
			Vec3 offset = findRedCarpet(facingDirection);
			x += (int) offset.xCoord;
			z += (int) offset.zCoord;
			// Prepare site - assume first layer is biggest.
			Block dirt = Blocks.dirt;
			List<List<Character>> blocks = layerDefs
					.get(layerStack.get(0));
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
							setBlock(world, x + i, k, z + j, dirt);
						} else {
							break;
						}
					}
				}
			}
			// Lay down each layer
			for (int n = 0; n < layerStack.size(); n++) {
				String layer = layerStack.get(n);
				blocks = rotateBlocks(layerDefs.get(layer), facingDirection);
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

	private List<List<Character>> rotateBlocks(final List<List<Character>> inBlocks,
			int facingDirection) {

		List<List<Character>> outBlocks = new ArrayList<List<Character>>();
		switch (facingDirection) {
		case 0:
			// Facing SOUTH
			for (int i = inBlocks.get(0).size()-1; i >= 0; i--) {
				List<Character> outRow = new ArrayList<Character>();
				for (int j = 0; j < inBlocks.size(); j++) {
					outRow.add(inBlocks.get(j).get(i));
				}
				outBlocks.add(outRow);
			}
			break;
		case 1:
			// FACING WEST
			outBlocks = inBlocks;
			break;
		case 2:
			// FACING NORTH
			for (int i = 0; i < inBlocks.get(0).size(); i++) {
				List<Character> outRow = new ArrayList<Character>();
				for (int j = inBlocks.size()-1; j >= 0; j--) {
					outRow.add(inBlocks.get(j).get(i));
				}
				outBlocks.add(outRow);
			}
			break;
		default:
			// FACING EAST
			for (int j = inBlocks.size()-1; j >= 0; j--) {
				List<Character> outRow = new ArrayList<Character>();
				List<Character> inRow = inBlocks.get(j);
				for (int i = inRow.size()-1; i >=0; i--) {
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
						case 0:
							// Facing SOUTH
							offset.xCoord = (double) -j;
							offset.zCoord = (double) i-row.size()+1.0;
							return offset;
						case 1:
							// FACING WEST
							offset.xCoord = (double) -i;
							offset.zCoord = (double) -j;
							return offset;
						case 2:
							// FACING NORTH
							offset.xCoord = (double) j-blocks.size()+1.0;
							offset.zCoord = (double) -i;
							return offset;
						default:
							// FACING EAST
							offset.xCoord = (double) i-row.size()+1.0;
							offset.zCoord = (double) j-blocks.size()+1.0;
							return offset;
						}
					}
				}
			}
		}
		return offset;
	}

	private Entity spawnEntity(World world, Entity entity, int x, int y, int z) {
		entity.setPosition(x + 4, y + 3, z + 4);
		world.spawnEntityInWorld(entity);
		return entity;
	}

	private void setLayer(World world, int x, int y, int z,
			List<List<Character>> blocks) {
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
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				char b = row.get(i);
				switch (b) {
				case 'a':
					world.setBlock(x + i, y, z + j, Blocks.anvil, 1, 3);
					break;
				case 'b':
					setBlock(world, x + i, y, z + j, Blocks.brewing_stand);
					break;
				case 'B':
					setBlock(world, x + i, y, z + j, Blocks.bookshelf);
					break;
				case 'c':
					TileEntityChest tec = (TileEntityChest) setBlock(world, x + i, y, z + j, Blocks.chest);
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
						for (int slot = tec.getSizeInventory() - 1; slot >= 0; slot--) {
							tec.setInventorySlotContents(slot, stack);
						}
					}
					break;
				case 'C':
					setBlock(world, x + i, y, z + j, Blocks.crafting_table);
					break;
				case 'd':
					setBlock(world, x + i, y, z + j, Blocks.dirt);
					break;
				case 'D':
					// Set on next pass - setBlock(world, x + i, y, z + j, Blocks.iron_door);
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
					setBlock(world, x + i, y, z + j, Blocks.furnace);
					break;
				case 'j':
					setBlock(world, x + i, y, z + j, Blocks.glowstone);
					break;
				case 'J':
					setBlock(world, x + i, y, z + j, Blocks.pumpkin_stem, 6, 3);
					break;
				case 'g':
					setBlock(world, x + i, y, z + j, Blocks.glass_pane);
					break;
				case 'G':
					setBlock(world, x + i, y, z + j, Blocks.reeds, 6, 3);
					break;
				case 'H':
					setBlock(world, x + i, y, z + j, Blocks.bed, 8, 3); // head of bed
					setBlock(world, x + i, y, z + j - 1, Blocks.bed, 0, 3); // foot
					break;
				case 'l':
					setBlock(world, x + i, y, z + j, Blocks.ladder, 5, 3);
					break;
				case 'L':
					setBlock(world, x + i, y, z + j, Blocks.waterlily);
					break;
				case 'M':
					setBlock(world, x + i, y, z + j, Blocks.melon_stem, 6, 3);
					break;
				case 'o':
					setBlock(world, x + i, y, z + j, Blocks.obsidian);
					break;
				case 'p':
					// Set on next pass: setBlock(world, x + i, y, z + j, Blocks.wooden_pressure_plate);
					break;
				case 'P':
					setBlock(world, x + i, y, z + j, Blocks.potatoes, 6, 3);
					break;
				case 'q':
					setBlock(world, x + i, y, z + j, Blocks.diamond_block);
					break;
				case 'Q':
					setBlock(world, x + i, y, z + j, Blocks.beacon);
					break;
				case 'r':
					setBlock(world, x + i, y, z + j, Blocks.carpet, 14, 3);
					break;
				case 'R':
					setBlock(world, x + i, y, z + j, Blocks.carrots, 6, 3);
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
				case 'u':
					setBlock(world, x + i, y, z + j, Blocks.wooden_button, 1, 3);
					break;
				case 'w':
					setBlock(world, x + i, y, z + j, Blocks.water, 0, 3);
					break;
				case 'W':
					setBlock(world, x + i, y, z + j, Blocks.wheat, 6, 3);
					break;
				case '+':
					setBlock(world, x + i, y, z + j, Blocks.fence);
					break;
				case '=':
					setBlock(world, x + i, y, z + j, Blocks.fence_gate, 3, 3);
					break;
				default:
					world.setBlockToAir(x + i, y, z + j);
					break;
				}
			}
		}
		// place torches and doors
		for (int j = 0; j < blocks.size(); j++) {
			List<Character> row = blocks.get(j);
			for (int i = 0; i < row.size(); i++) {
				char b = row.get(i);
				switch (b) {
				case 't':
					if (world.isSideSolid(x + i, y - 1, z + j,
							ForgeDirection.UP, false)) {
						setBlock(world, x + i, y, z + j, Blocks.torch);
					} else {
						setBlock(world, x + i, y, z + j, Blocks.torch);
					}
					break;
				case 'p':
					setBlock(world, x + i, y, z + j, Blocks.wooden_pressure_plate, 0, 3);
					break;
				case 'D':
					ItemDoor.placeDoorBlock(world, x + i, y, z + j, 3, Blocks.iron_door);
					// setBlock(world, x + i, y, z + j, b, 2, 0);
					// setBlock(world, x + i, y + 1, z + j, b, 8, 0);
					break;
				default:
					// Do nothing.
					break;
				}
			}
		}

	}

	private TileEntity setBlock(World world, int x, int y, int z, Block block) {
		// logger.info("setBlock: " + x + "," + y + "," + z + " - " + block.getLocalizedName());
		world.setBlock(x, y, z, block);
		world.notifyBlockChange(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	private TileEntity setBlock(World world, int x, int y, int z, Block block, int metadata, int flag) {
		// logger.info("setBlock: " + x + "," + y + "," + z + " - " + block.getLocalizedName());

		// Flag values:
		// 1 - Cause a block update.
		// 2 - Send the change to clients (you almost always want this).
		// 4 - Prevents the block from being re-rendered, if this is a client world.
		// Flags can be added together.
		world.setBlock(x, y, z, block, metadata, flag);
		world.notifyBlockChange(x, y, z, block);
		return world.getTileEntity(x, y, z);
	}

	private void reportMetaData(World w, int x, int y, int z) {
		InstaTower.logger.info("METADATA: " + x + "," + y + "," + z + ": "
				+ w.getBlockMetadata(x, y, z) + " - "
				+ w.getBlock(x, y, z).getLocalizedName() + " - "
				+ Block.getIdFromBlock(w.getBlock(x, y, z)));
	}

	private void loadConfigFile() {
		logger.info("Config File = " + configFile.getPath());

		BufferedReader in = null;
		Pattern isComment = Pattern.compile("^\\s*#");
		Pattern isLayer = Pattern.compile("^\\s*Layer\\s*(.*?):?$");
		Matcher m = null;
		List<List<Character>> rows = null;
		List<Character> row = null;
		int x, y;
		String line, layer = null;

		layerDefs = new HashMap<String, List<List<Character>>>();
		layerStack = new ArrayList<String>();

		try {
			in = new BufferedReader(new FileReader(configFile));
		} catch (FileNotFoundException e) {
			logger.warn("(W) Config file did not exist, "
					+ configFile.getPath());
			logger.warn("(W) Setting config file to default!");
			InputStream is = InstaTower.class.getResourceAsStream("/assets/"
					+ Constants.MODID + "/schematics/instatower.cfg");
			try {
				Files.asByteSink(configFile).writeFrom(is);
				is.close();
				try {
					in = new BufferedReader(new FileReader(configFile));
				} catch (FileNotFoundException e1) {
					logger.error("(E) Unable to open config file after resetting, "
							+ configFile.getPath());
					logger.catching(e1);
					logger.error(e1.getStackTrace());
				}
			} catch (IOException e1) {
				logger.error("(E) Unable to reset config, "
						+ configFile.getPath() + ", with default.");
				logger.catching(e);
				logger.error(e.getStackTrace());
			}
		}

		try {
			while ((line = in.readLine()) != null) {
				if (!isComment.matcher(line).matches()) {
					m = isLayer.matcher(line);
					if (m.matches()) {
						layer = m.group(1);
						layerStack.add(layer);
						logger.debug("Layer: " + layer);
						if (!layerDefs.containsKey(layer)) {
							// (Re)defining new layer.
							rows = new ArrayList<List<Character>>();
							layerDefs.put(layer, rows);
						}
					} else if (layer != null) {
						logger.debug("Config: " + line);
						row = new ArrayList<Character>();
						for (char c : line.toCharArray()) row.add(c);
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
					if (sz > maxRowLength) maxRowLength = sz;
				}
				int sz = rs.size();
				if (sz > maxNumberOfRows) maxNumberOfRows = sz;
			}
			// Normalize: Pad all rows for all layers with AIR to maximum row length.
			for (List<List<Character>> rs : layerDefs.values()) {
				for (int j=rs.size(); j < maxNumberOfRows; j++) {
					rs.add((List) new ArrayList<Blocks>());
				}
				for (List<Character> r : rs) {
					for (int i=r.size(); i < maxRowLength; i++) {
						r.add(' ');
					}
				}
			}
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
			}
		}

	}
}
