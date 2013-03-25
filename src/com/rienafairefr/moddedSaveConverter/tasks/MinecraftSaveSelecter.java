package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.File;

import com.rienafairefr.moddedSaveConverter.BadMinecraftSaveException;
import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftSave;

public class MinecraftSaveSelecter extends CustomTask{
	private File file;
	private int type;
	private MinecraftSave minecraftsave;
	public void selectSave(){
		if (minecraftsave!=null){
			ConverterMain.isdone.put("choosesave"+type, false);
			Logger.logln("Selected save"+type+" folder: "+minecraftsave.getRootfolderpath());
			ConverterMain.options.setsave(minecraftsave, type);
			ConverterMain.isdone.put("choosesave"+type, true);
			ConverterMain.options.save();
			MainFrame.updateframe();
		}
		return;
	}

	public MinecraftSaveSelecter(File save, int b) throws BadMinecraftSaveException {
		super();
		if (save==null) {
			Logger.logln("No save file");
			return;
		}
		if (save.exists() && !save.isDirectory()){
			Logger.logln("Not a directory");
			return;
		}
		if (b==0){
			File leveldat=new File(save,"level.dat");
			if (!leveldat.exists()){
				Logger.logln("No level.dat file");
				return;	
			}
			File regions=new File(save,"region");
			if (!regions.exists()){
				Logger.logln("No regions");
				return;	
			}

			File rootfolderorig=save.getParentFile();
			if (ConverterMain.options.getminecraft(1)!=null){
				File rootfolderdest=ConverterMain.options.getsavedir(1);
				if (rootfolderdest.exists()){
					File rootfolderdestnew=new File(rootfolderdest,rootfolderorig.getName()+"_convertedto_"+ConverterMain.options.getminecraftversion(1));
					rootfolderdestnew.mkdirs();
					if (ConverterMain.options.getsave(1)==null){
						ConverterMain.options.setsave(new MinecraftSave(rootfolderdestnew,1),1);
					}
				}
			}
		}else if (b==1){
			File leveldat=new File(save,"level.dat");
			if (leveldat.exists()){
				Logger.logln("There's a save in that folder");	
			}
		}
		this.file = save;
		this.type = b;
		minecraftsave=new MinecraftSave(this.file,this.type);
	}

	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Selecting Save....");
		Logger.level=1;
		selectSave();
		Logger.level=0;
		return;
	}

}
