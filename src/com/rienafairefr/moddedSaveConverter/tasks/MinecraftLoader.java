package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.File;

import com.rienafairefr.moddedSaveConverter.BadMinecraftAppException;
import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftApp;

public class MinecraftLoader extends CustomTask{
	private String minejar;
	private String jarFile;
	private int index;
	
	public MinecraftLoader(String minejar, String jarFile, String vers,
			int index) {
		super();
		this.minejar = minejar;
		this.jarFile = jarFile;
		this.index = index;
	}
	public void loadingMinecraft(){
		//File minejar=new File(minedir,"/.minecraft/bin/"+jarFile);
		if ((index==0 || index==1)&& (new File(minejar)).exists()){
			//String minedir, String jar, String vers, int index
			MinecraftApp minecraftnew;
			try {
				minecraftnew = new MinecraftApp(new File(minejar),jarFile,index);
			} catch (BadMinecraftAppException e) {
				return;
			}
			Logger.logln("Selected minecraft"+index+" jar:"+minecraftnew);
			ConverterMain.addworker(new MinecraftTester(ConverterMain.options.getminecraft(index)));
			ConverterMain.addworker(new MinecraftIDnameMapper(ConverterMain.options.getminecraft(index)));
			ConverterMain.isdone.put("choosemine"+index, true);
			MainFrame.updateframe();
		}
		return;
	}
	public static void LoadMinecraftApp(MinecraftApp minecraftapp){
		if (minecraftapp.index==0|| minecraftapp.index==1){
			ConverterMain.options.setminecraft(minecraftapp,minecraftapp.index);
			ConverterMain.isdone.put("choosemine"+minecraftapp.index, true);
		}
	}
	public static void LoadMinecraftApp(File minejar,int index){
		if (minejar!=null && (index==0|| index==1)){
			if (minejar.exists()){
				//String minedir, String jar, String vers, int index
				//ou File jar, String vers, int index

				MinecraftApp minecraftappnew;
				try {
					minecraftappnew = new MinecraftApp(minejar, "Unknown",  index);
				} catch (BadMinecraftAppException e) {
					Logger.logln("Bad Minecraft App, try again.");
					return;
				}
				ConverterMain.isdone.put("choosemine"+index, false);
				LoadMinecraftApp(minecraftappnew);
				ConverterMain.addworker(new MinecraftTester(minecraftappnew));
				ConverterMain.addworker(new MinecraftIDnameMapper(minecraftappnew));
			}
		}
	}
	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Loading Minecraft App....");
		Logger.level=1;
		loadingMinecraft();
		Logger.level=0;
		return;
	}
}