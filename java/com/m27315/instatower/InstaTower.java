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

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		ModItems.init(event, logger);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
	}

}
