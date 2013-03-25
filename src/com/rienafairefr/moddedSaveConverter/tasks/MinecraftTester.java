package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;

import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftApp;

public class MinecraftTester extends CustomTask{
	public MinecraftApp minecraftapp;
	
	public MinecraftTester(MinecraftApp minecraftapp) {
		super();
		this.minecraftapp = minecraftapp;
	}

	public Object TestMinecraft(){
		ConverterMain.isdone.put("testmine"+minecraftapp.index, false);
		ConverterMain.options.save();
		// minecrafts array is kinda 1-indexed
		File minecraft=minecraftapp.MCdir;

		System.out.println("Testing Minecraft"+minecraftapp.index);
		File minecraftjar;
		URL[] jarURLs1;
		try {
			minecraftjar = minecraftapp.MCjar;

			File nativelibs= new File(minecraftapp.MCdir,"bin/natives");
			File binfolder= minecraftapp.MCjar.getParentFile();

			jarURLs1 = new URL[]{
					minecraftapp.MCdir.toURI().toURL()
					,new File(binfolder,"/lwjgl.jar").toURI().toURL()
					,new File(binfolder,"/jinput.jar").toURI().toURL()
					,new File(binfolder,"/lwjgl_util.jar").toURI().toURL()
					,minecraftjar.toURI().toURL()
			};
		} catch (IOException e2) {
			Logger.logln("ERROR IO when trying to test Minecraft"+minecraftapp.index);
			return -1;
		}

		String pathToJar=minecraftjar.getAbsolutePath();
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(pathToJar);
		} catch (IOException e2) {
			Logger.logln("ERROR IO when locating the jar file for Minecraft"+minecraftapp.index);
			return -2;
		}
		Enumeration<?> e = jarFile.entries();

		//URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };

		URLClassLoader cl = URLClassLoader.newInstance(jarURLs1, MinecraftTester.class.getClassLoader());

		Class<?> ML = null;
		try {
			ML=cl.loadClass("ModLoader");
			//Logger.log("INFO OK Found ModLoader in Minecraft"+minecraftapp.index);
		}catch(ClassNotFoundException ex){
			Logger.logln("WARNING Couldn't find ModLoader in  Minecraft"+minecraftapp.index+", expect loss of blocks/items");
		}

		String version="Unknown";
		Class<?> minecraftclass=null;
		try {
			minecraftclass=cl.loadClass("net.minecraft.client.Minecraft");
			for(Method m : minecraftclass.getMethods()) {
				Class<?>[] parameterTypes = m.getParameterTypes();
				Class<?> returnType = m.getReturnType();
				//System.out.println(returnType.getCanonicalName()+" "+parameterTypes.length+" "+Modifier.isStatic(m.getModifiers()));
				if (returnType.getCanonicalName()=="java.lang.String" && parameterTypes.length==0){
					try {
						version=(String) m.invoke(minecraftclass);
						if (MainFrame.models[minecraftapp.index].getIndexOf(version)==-1){
							version="Unsupported";
							Logger.logln("Found an unsupported version. Please slap the developper so the converter supports it");
						}

					} catch (IllegalAccessException  e1) {
					} catch (IllegalArgumentException e1) {
					} catch ( InvocationTargetException e1) {
					}
					break;
				}
			}
		}catch(Exception ex){
			Logger.logln("WARNING Couldn't find the version in  Minecraft"+minecraftapp.index+", expect loss of blocks/items");
		}

		if (version.matches("Unknown|Unsupported") && !ConverterMain.options.getminecraftversion(minecraftapp.index).matches("Unknown|Unsupported")){
			version=ConverterMain.options.getminecraftversion(minecraftapp.index);
		}else{
			ConverterMain.options.setminecraftversion(minecraftapp.index, version);
		}
		Logger.logln("INFO Apparently the Minecraft"+minecraftapp.index+" version is "+version);
		try {
			jarFile.close();
		} catch (IOException e1) {
			Logger.logln("ERROR IO when closing the jar file for Minecraft"+minecraftapp.index);
			return -3;
		}
		ConverterMain.isdone.put("testmine"+minecraftapp.index, true);
		MainFrame.updateframe();
		try {
			cl.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return 0;
	}

	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Testing Minecraft App....");
		Logger.level=1;
		TestMinecraft();
		Logger.level=0;
		return;
	}

}
