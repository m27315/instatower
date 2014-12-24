package com.m27315.instatower;

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

import com.google.common.io.Files;
import com.m27315.instatower.items.ModItems;

import lib.Constants;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.VERSION)
public class InstaTower {

	@Instance(Constants.MODID)
	public static InstaTower instance;

	public static Logger logger;
	private static File configFile;
	public static HashMap<String, List<List<Block>>> layerDefs;
	public static List<String> layerStack;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		layerDefs = new HashMap<String, List<List<Block>>>();
		layerStack = new ArrayList<String>();
		configFile = event.getSuggestedConfigurationFile();

		loadConfigFile();

		ModItems.init();
	}

	public void init(FMLInitializationEvent event) {

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	private void loadConfigFile() {
		logger.info("Config File = " + configFile.getPath());

		BufferedReader in = null;
		Pattern isComment = Pattern.compile("^\\s*#");
		Pattern isLayer = Pattern.compile("^\\s*Layer\\s*(.*?):?$");
		Matcher m = null;
		List<List<Block>> rows = null;
		List<Block> row = null;
		int x, y;
		String line, layer = null;

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
							rows = new ArrayList<List<Block>>();
							layerDefs.put(layer, rows);
						}
					} else if (layer != null) {
						logger.debug("Config: " + line);
						row = new ArrayList<Block>();
						for (char c : line.toCharArray()) {
							switch (c) {
							case 'a':
								row.add(Blocks.anvil);
								break;
							case 'b':
								row.add(Blocks.brewing_stand);
								break;
							case 'B':
								row.add(Blocks.bookshelf);
								break;
							case 'c':
								row.add(Blocks.chest);
								break;
							case 'C':
								row.add(Blocks.crafting_table);
								break;
							case 'd':
								row.add(Blocks.dirt);
								break;
							case 'D':
								row.add(Blocks.iron_door);
								break;
							case 'e':
								row.add(Blocks.emerald_block);
								break;
							case 'E':
								row.add(Blocks.enchanting_table);
								break;
							case 'f':
								row.add(Blocks.farmland);
								break;
							case 'F':
								row.add(Blocks.furnace);
								break;
							case 'j':
								row.add(Blocks.glowstone);
								break;
							case 'J':
								row.add(Blocks.pumpkin_stem);
								break;
							case 'g':
								row.add(Blocks.glass_pane);
								break;
							case 'G':
								row.add(Blocks.reeds);
								break;
							case 'H':
								row.add(Blocks.bed);
								break;
							case 'l':
								row.add(Blocks.ladder);
								break;
							case 'L':
								row.add(Blocks.waterlily);
								break;
							case 'M':
								row.add(Blocks.melon_stem);
								break;
							case 'o':
								row.add(Blocks.obsidian);
								break;
							case 'p':
								row.add(Blocks.wooden_pressure_plate);
								break;
							case 'P':
								row.add(Blocks.potatoes);
								break;
							case 'q':
								row.add(Blocks.diamond_block);
								break;
							case 'Q':
								row.add(Blocks.beacon);
								break;
							case 'r':
								row.add(Blocks.carpet);
								break;
							case 'R':
								row.add(Blocks.carrots);
								break;
							case 's':
								row.add(Blocks.stone);
								break;
							case 'S':
								row.add(Blocks.stonebrick);
								break;
							case 't':
								row.add(Blocks.torch);
								break;
							case 'u':
								row.add(Blocks.wooden_button);
								break;
							case 'w':
								row.add(Blocks.water);
								break;
							case 'W':
								row.add(Blocks.wheat);
								break;
							case '+':
								row.add(Blocks.fence);
								break;
							case '=':
								row.add(Blocks.fence_gate);
								break;
							default:
								row.add(Blocks.air);
								break;
							}
						}
						rows.add(row);
					}

				}
			}
			// Find maximum length of all rows for all layers
			int maxRowLength = 0;
			for (List<List<Block>> rs : InstaTower.layerDefs.values()) {
				for (List<Block> r : rs) {
					int sz = r.size();
					if (sz > maxRowLength) maxRowLength = sz;
				}
			}
			// Normalize: Pad all rows for all layers with AIR to maximum row length.
			for (List<List<Block>> rs : InstaTower.layerDefs.values()) {
				for (List<Block> r : rs) {
					for (int i=r.size(); i < maxRowLength; i++) {
						r.add(Blocks.air);
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
