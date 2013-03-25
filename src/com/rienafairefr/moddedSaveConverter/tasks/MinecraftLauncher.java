package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import javax.swing.JFrame;

import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftApp;
import com.sk89q.mclauncher.LaunchTask;
import com.sk89q.mclauncher.Task;
import com.sk89q.mclauncher.TaskWorker;
import com.sk89q.mclauncher.config.Configuration;
import com.sk89q.mclauncher.config.Def;

public class MinecraftLauncher{
	@SuppressWarnings("unchecked")
	public static TreeMap<Object,String[]>[] idmap= new TreeMap[ConverterMain.Ntype];
	public MinecraftApp minecraftapp;
	public MinecraftLauncher(MinecraftApp minecraftapp) {
		this.minecraftapp=minecraftapp;
	}
	public void LaunchMinecraft() throws IOException{
		ConverterMain.options.save();
		
		//TreeMap<Object,String[]>[] idmap=new TreeMap[ConverterMain.Ntype];
		boolean remember = MainFrame.savePassCheck.isSelected();
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			idmap[ntype]=new TreeMap<Object,String[]>();
		}
		
		//		File minecraft=minecraftapp.minecraftdir;
		//	String version=minecraftapp.version;
		if (minecraftapp.version.equals("Unknown")){
			Logger.logln("WARNING Can't launch without a version defined");
			return;
		}

		boolean  test=true;
		boolean fast_test=false;
		boolean autoConnect=false;
		boolean forceUpdateCheck=false;
		//File minecraftjar=new File(minecraft.getCanonicalPath(),"/.minecraft/bin/minecraft.jar");
		Configuration config=new Configuration("config", "minecraft12", minecraftapp.MCdir, null);
		
		File dotminecraft=minecraftapp.MCjar.getParentFile().getParentFile();
		
		String prevname=dotminecraft.getName();
		if (!prevname.equals(".minecraft")){

		    File file2 = new File(minecraftapp.MCdir,".minecraft");

		    if(file2.exists()) throw new IOException("file exists");
			
		    boolean success = dotminecraft.renameTo(file2);
		    if (!success) {
		        // File was not successfully renamed
		    }
		}

		JFrame frame=new JFrame();

		String username=MainFrame.userText.getText();
		String password=MainFrame.passText.getText();
		// Save the identity
		if (!MainFrame.playOfflineCheck.isSelected()) {
			if (remember) {
				ConverterMain.options.saveIdentity(username, password);
				ConverterMain.options.setLastUsername(username);
			} else {
				ConverterMain.options.forgetIdentity(username);
				ConverterMain.options.setLastUsername(null);
			}
		}
		ConverterMain.options.save();

		LaunchTask task = new LaunchTask(frame, config, username, password, minecraftapp.MCjar.getName());
		task.setPlayOffline(MainFrame.playOfflineCheck.isSelected() || (test && fast_test));
		task.setShowConsole(true);
		task.minecraftapp=minecraftapp;

		config.getSettings().set(Def.WINDOW_WIDTH, 300);
		config.getSettings().set(Def.WINDOW_HEIGHT, 200);

		TaskWorker worker = Task.startWorker(frame, task);
		try {
			synchronized(idmap){
				while(idmap==null){
					idmap.wait();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		dotminecraft.renameTo(new File(minecraftapp.MCdir,prevname));
	}
}
